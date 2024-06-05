package org.zh.chatter.manager;

import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.zh.chatter.component.ChatMessageCell;
import org.zh.chatter.component.PrivateChatButtonActions;
import org.zh.chatter.controller.ChatAreaController;
import org.zh.chatter.model.vo.ChatMessageVO;
import org.zh.chatter.util.Constants;
import org.zh.chatter.util.NodeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PrivateChatTabManager {
    private final Map<String, Tab> tabMap;
    private static final String PRIVATE_CHAT_TAB_FXML_PATH = "fxml/private-chat-tab.fxml";
    private static final String TAB_ID_PREFIX = "private_chat_";
    private static final String CLEAR_BTN_CLASS = "clear-btn";
    private static final String SEND_BTN_CLASS = "send-btn";
    private static final String SEND_FILE_BTN_CLASS = "send-file-btn";
    private static final String FILE_LIST_BTN_CLASS = "file-list-btn";
    private final ApplicationContext applicationContext;
    private final ChatMessageManager chatMessageManager;
    private final PrivateChatButtonActions privateChatButtonActions;
    private final ChatAreaController chatAreaController;

    public PrivateChatTabManager(ApplicationContext applicationContext,
                                 ChatMessageManager chatMessageManager,
                                 PrivateChatButtonActions privateChatButtonActions, ChatAreaController chatAreaController) {
        this.tabMap = new HashMap<>();
        this.applicationContext = applicationContext;
        this.chatMessageManager = chatMessageManager;
        this.privateChatButtonActions = privateChatButtonActions;
        this.chatAreaController = chatAreaController;
    }

    private String buildTabId(String targetUserId) {
        return TAB_ID_PREFIX.concat(targetUserId);
    }

    public Tab addTab(String targetUserId, String targetUsername) throws Exception {
        FXMLLoader loader = new FXMLLoader(new ClassPathResource(PRIVATE_CHAT_TAB_FXML_PATH).getURL());
        loader.setControllerFactory(applicationContext::getBean);
        Tab newTab = loader.load();
        newTab.setText(targetUsername);
        String tabId = this.buildTabId(targetUserId);
        tabMap.put(tabId, newTab);
        VBox vbox = (VBox) newTab.getContent();
        List<Node> nodes = NodeUtil.paneNodes(vbox);
        for (Node node : nodes) {
            if (node instanceof ListView<?>) {
                //将ObservableList关联到ListView上
                ListView<ChatMessageVO> listView = (ListView<ChatMessageVO>) node;
                listView.setItems(chatMessageManager.initPrivateChatMessageList(tabId));
                listView.setCellFactory(c -> new ChatMessageCell());
            } else if (node instanceof Button button) {
                //获取内部的Button，关联上所有操作
                ObservableMap<Object, Object> properties = button.getProperties();
                properties.put(Constants.TAB_ID, tabId);
                properties.put(Constants.TARGET_USER_ID, targetUserId);
                properties.put(Constants.USER_VO, newTab.getProperties().get(Constants.USER_VO));
                switch (Objects.requireNonNull(button.getStyleClass().stream().findFirst().orElse(null))) {
                    case CLEAR_BTN_CLASS ->
                            button.setOnMouseClicked(privateChatButtonActions.getOnClearButtonClicked());
                    case SEND_BTN_CLASS -> {
                        //todo 发送消息按钮操作
                    }
                    case SEND_FILE_BTN_CLASS ->
                            button.setOnMouseClicked(privateChatButtonActions.getOnSendFileButtonClicked());
                    case FILE_LIST_BTN_CLASS -> button.setOnMouseClicked(chatAreaController::openFileTransferDialog);
                }
            } else if (node instanceof TextArea textArea) {
                //todo 将操作关联到TextArea上
            }
        }
        return newTab;
    }

    public Tab getTab(String targetUserId) {
        return tabMap.get(this.buildTabId(targetUserId));
    }

    public void removeTab(String tabId) {
        tabMap.remove(tabId);
    }
}
