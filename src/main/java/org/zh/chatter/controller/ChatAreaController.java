package org.zh.chatter.controller;

import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.zh.chatter.component.ChatMessageCell;
import org.zh.chatter.component.FileTaskButtonActions;
import org.zh.chatter.component.PrivateChatButtonActions;
import org.zh.chatter.component.UserListDialog;
import org.zh.chatter.manager.*;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.vo.ChatMessageVO;
import org.zh.chatter.network.UdpServer;
import org.zh.chatter.util.Constants;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

//todo 帮助文档
@Controller
public class ChatAreaController implements Initializable {
    @FXML
    private TextArea groupInputArea;
    @FXML
    private ListView<ChatMessageVO> groupMessageArea;
    @FXML
    private TabPane chatArea;
    @Resource
    private NodeManager nodeManager;
    @Resource
    private UdpServer udpServer;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Resource
    private ChatMessageManager chatMessageManager;
    @Resource
    private FileTaskButtonActions fileTaskButtonActions;
    @Resource
    private PrivateChatTabManager privateChatTabManager;
    @Resource
    private PrivateChatButtonActions privateChatButtonActions;
    @Autowired
    private TcpConnectionManager tcpConnectionManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        groupMessageArea.setItems(chatMessageManager.getGroupChatMessageList());
        groupMessageArea.setCellFactory(c -> new ChatMessageCell());
        privateChatTabManager.setChatArea(chatArea);
    }

    public void handleGroupMessageSend() throws Exception {
        //清除输入框文本，并发送文本
        String text = groupInputArea.getText();
        if (Strings.isEmpty(text)) {
            return;
        }
        groupInputArea.clear();
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        udpServer.sendChatMessage(text);
        this.showChatMessage(text, currentUser.getId(), currentUser.getUsername());
    }

    public void showChatMessage(String text, String userId, String username) {
        chatMessageManager.addGroupChatMessage(new ChatMessageVO(userId, username, text, LocalDateTime.now()));
    }

    public void handleShortcutSend(KeyEvent keyEvent) throws Exception {
        if (Constants.SEND_MESSAGE_SHORTCUT.match(keyEvent)) {
            this.handleGroupMessageSend();
        }
    }

    public void handleClearGroupMessage(MouseEvent mouseEvent) {
        chatMessageManager.clearGroupChatMessage();
    }

    public void showUserList(MouseEvent mouseEvent) {
        UserListDialog dialog = new UserListDialog(nodeManager.getUserList(), fileTaskButtonActions, privateChatButtonActions, chatArea);
        dialog.show();
    }

    public void openFileTransferDialog(MouseEvent mouseEvent) {
        fileTaskButtonActions.openFileTransferDialog(mouseEvent);
    }

    public void handleClosePrivateChatTab(Event event) {
        Tab tab = (Tab) event.getSource();
        if ((Boolean) tab.getProperties().getOrDefault(Constants.IS_CLIENT_TAB, false)) {
            tcpConnectionManager.deductReferenceCount((Channel) tab.getProperties().get(Constants.CHANNEL));
        }
        privateChatTabManager.removeTab(tab.getId());
    }
}
