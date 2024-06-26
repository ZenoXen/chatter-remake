package org.zh.chatter.component;

import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zh.chatter.manager.ChatMessageManager;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.PrivateChatTabManager;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.vo.ChatMessageVO;
import org.zh.chatter.model.vo.UserVO;
import org.zh.chatter.network.TcpClient;
import org.zh.chatter.util.Constants;
import org.zh.chatter.util.NodeUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;


@Component
public class PrivateChatButtonActions {
    @Resource
    private ChatMessageManager chatMessageManager;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Resource
    private TcpClient tcpClient;
    @Resource
    private PrivateChatTabManager privateChatTabManager;
    @Resource
    private FileTaskManager fileTaskManager;
    @Resource
    private FileTaskButtonActions fileTaskButtonActions;
    @Value("${app.port.tcp}")
    private Integer tcpPort;

    private static final String CLEAR_BTN_CLASS = "clear-btn";
    private static final String SEND_BTN_CLASS = "send-btn";
    private static final String SEND_FILE_BTN_CLASS = "send-file-btn";
    private static final String FILE_LIST_BTN_CLASS = "file-list-btn";
    private static final String CHAT_INPUT_TEXT_AREA_CLASS = "chat-input-text-area";
    private static final String MESSAGE_LIST_VIEW_CLASS = "message-list-view";

    @Getter
    private EventHandler<MouseEvent> onClearButtonClicked = e -> {
        Button button = (Button) e.getSource();
        String tabId = (String) button.getProperties().get(Constants.TAB_ID);
        chatMessageManager.clearPrivateChatMessage(tabId);
    };

    private EventHandler<MouseEvent> getOnSendFileButtonClicked(Channel channel) {
        return e -> {
            Button button = (Button) e.getSource();
            //弹出windows文件选框
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(Constants.SELECT_FILE_TITLE);
            File chosenFile = fileChooser.showOpenDialog(button.getScene().getWindow());
            if (chosenFile != null) {
                tcpClient.sendFileTransferRequest(channel, chosenFile);
            }
        };
    }

    public EventHandler<MouseEvent> getOnSendButtonClicked(TextArea textArea) {
        return e -> {
            Button button = (Button) e.getSource();
            UserVO userVO = (UserVO) button.getProperties().get(Constants.USER_VO);
            try {
                this.handlePrivateMessageSend(textArea, userVO);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public EventHandler<KeyEvent> getOnShortcutKeyPressed() {
        return e -> {
            TextArea textArea = (TextArea) e.getSource();
            UserVO userVO = (UserVO) textArea.getProperties().get(Constants.USER_VO);
            if (Constants.SEND_MESSAGE_SHORTCUT.match(e)) {
                try {
                    this.handlePrivateMessageSend(textArea, userVO);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    public void handlePrivateMessageSend(TextArea textArea, UserVO userVO) throws Exception {
        //清除输入框文本，并发送文本
        String text = textArea.getText();
        String tabId = textArea.getProperties().get(Constants.TAB_ID).toString();
        if (Strings.isEmpty(text)) {
            return;
        }
        textArea.clear();
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        Tab tab = privateChatTabManager.getTabByTabId(tabId);
        Channel channel = (Channel) tab.getProperties().get(Constants.CHANNEL);
        String sessionId = tab.getProperties().get(Constants.SESSION_ID).toString();
        tcpClient.sendPrivateChatMessage(channel, sessionId, text);
        this.showChatMessage(tabId, text, currentUser.getId(), currentUser.getUsername());
    }

    public void showChatMessage(String tabId, String text, String userId, String username) {
        chatMessageManager.addPrivateChatMessage(tabId, new ChatMessageVO(userId, username, text, LocalDateTime.now()));
    }

    public BiFunction<UserVO, Button, UserVO> getPrivateChatButtonAction(TabPane tabPane) {
        return (userVO, button) -> {
            tcpClient.sendRemotePrivateChatUserInfoExchangeRequest(userVO.getAddress(), tcpPort);
            return userVO;
        };
    }

    public Tab getOrInitPrivateChatTab(TabPane tabPane, UserVO userVO, Channel channel, String sessionId) {
        Tab tab = privateChatTabManager.getTabByTargetUserId(userVO.getId());
        if (tab == null) {
            try {
                //将新tab添加到tab条上
                tab = privateChatTabManager.addTab(userVO.getId(), userVO.getUsername());
                ObservableMap<Object, Object> properties = tab.getProperties();
                properties.put(Constants.USER_VO, userVO);
                properties.put(Constants.CHANNEL, channel);
                properties.put(Constants.SESSION_ID, sessionId);
                this.setChildrenPropertiesAndActions(tab, channel);
                Tab finalTab = tab;
                Platform.runLater(() -> tabPane.getTabs().add(finalTab));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return tab;
    }

    private void setChildrenPropertiesAndActions(Tab newTab, Channel channel) {
        String tabId = newTab.getId();
        UserVO userVO = (UserVO) newTab.getProperties().get(Constants.USER_VO);
        VBox vbox = (VBox) newTab.getContent();
        List<Node> nodes = NodeUtil.getNestedNodesByStyleClass(vbox);
        nodes.forEach(n -> this.doSetNodeProperties(n, tabId, userVO));
        //将ObservableList关联到ListView上
        ListView<ChatMessageVO> listView = (ListView<ChatMessageVO>) NodeUtil.findFirstNodeByStyleClass(nodes, MESSAGE_LIST_VIEW_CLASS, Node.class);
        listView.setItems(chatMessageManager.initPrivateChatMessageList(tabId));
        listView.setCellFactory(c -> new ChatMessageCell());
        //将操作关联到TextArea上
        TextArea textArea = NodeUtil.findFirstNodeByStyleClass(nodes, CHAT_INPUT_TEXT_AREA_CLASS, TextArea.class);
        textArea.setOnKeyPressed(this.getOnShortcutKeyPressed());
        //获取内部的Button，关联上所有操作
        Button clearButton = NodeUtil.findFirstNodeByStyleClass(nodes, CLEAR_BTN_CLASS, Button.class);
        clearButton.setOnMouseClicked(this.getOnClearButtonClicked());
        Button sendButton = NodeUtil.findFirstNodeByStyleClass(nodes, SEND_BTN_CLASS, Button.class);
        sendButton.setOnMouseClicked(this.getOnSendButtonClicked(textArea));
        Button sendFileButton = NodeUtil.findFirstNodeByStyleClass(nodes, SEND_FILE_BTN_CLASS, Button.class);
        sendFileButton.setOnMouseClicked(this.getOnSendFileButtonClicked(channel));
        Button fileListButton = NodeUtil.findFirstNodeByStyleClass(nodes, FILE_LIST_BTN_CLASS, Button.class);
        fileListButton.setOnMouseClicked(this::openFileTransferDialog);
    }

    private void doSetNodeProperties(Node node, String tabId, UserVO userVO) {
        node.getProperties().put(Constants.TAB_ID, tabId);
        node.getProperties().put(Constants.USER_VO, userVO);
    }

    public void openFileTransferDialog(MouseEvent mouseEvent) {
        FileTransferDialog dialog = new FileTransferDialog(Constants.FILE_TRANSFER_DIALOG_WIDTH, Constants.FILE_TRANSFER_DIALOG_HEIGHT, fileTaskButtonActions, fileTaskManager.getInactiveTasks(), fileTaskManager.getOngoingTasks());
        dialog.show();
    }
}
