package org.zh.chatter.controller;

import jakarta.annotation.Resource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Controller;
import org.zh.chatter.component.ChatMessageCell;
import org.zh.chatter.component.UserListDialog;
import org.zh.chatter.manager.ChatMessageManager;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.NodeManager;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.vo.ChatMessageVO;
import org.zh.chatter.network.UdpServer;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

//todo 帮助文档
@Controller
public class ChatAreaController implements Initializable {
    private static final KeyCodeCombination SEND_MESSAGE_SHORTCUT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
    @FXML
    private TextArea inputArea;
    @FXML
    private ListView<ChatMessageVO> messageArea;
    @Resource
    private NodeManager nodeManager;
    @Resource
    private UdpServer udpServer;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Resource
    private ChatMessageManager chatMessageManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messageArea.setItems(chatMessageManager.getChatMessageList());
        messageArea.setCellFactory(c -> new ChatMessageCell());
    }

    public void handleMessageSend() throws Exception {
        //清除输入框文本，并发送文本
        String text = inputArea.getText();
        if (Strings.isEmpty(text)) {
            return;
        }
        inputArea.clear();
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        this.showChatMessage(text, currentUser.getId(), currentUser.getUsername());
        udpServer.sendChatMessage(text);
    }

    public void showChatMessage(String text, String userId, String username) {
        chatMessageManager.addChatMessage(new ChatMessageVO(userId, username, text, LocalDateTime.now()));
    }

    public void handleShortcutSend(KeyEvent keyEvent) throws Exception {
        if (SEND_MESSAGE_SHORTCUT.match(keyEvent)) {
            this.handleMessageSend();
        }
    }

    public void handleClearMessage(MouseEvent mouseEvent) {
        chatMessageManager.clearChatMessage();
    }

    public void showUserList(MouseEvent mouseEvent) {
        UserListDialog dialog = new UserListDialog(nodeManager.getUserList());
        dialog.showAndWait();
    }
}
