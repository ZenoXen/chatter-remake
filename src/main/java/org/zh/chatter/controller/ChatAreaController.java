package org.zh.chatter.controller;

import jakarta.annotation.Resource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Controller;
import org.zh.chatter.component.ChatMessageCell;
import org.zh.chatter.component.UserListDialog;
import org.zh.chatter.manager.NodeManager;
import org.zh.chatter.model.vo.ChatMessageVO;

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
    private ObservableList<ChatMessageVO> chatMessageList;
    @Resource
    private NodeManager nodeManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chatMessageList = FXCollections.observableArrayList();
        messageArea.setItems(chatMessageList);
        messageArea.setCellFactory(c -> new ChatMessageCell());
    }

    public void handleMessageSend() {
        //清除输入框文本，并发送文本
        String text = inputArea.getText();
        if (Strings.isEmpty(text)) {
            return;
        }
        inputArea.clear();
        chatMessageList.add(new ChatMessageVO(null, "test", text, LocalDateTime.now()));
        //todo 广播消息
    }

    public void handleShortcutSend(KeyEvent keyEvent) {
        if (SEND_MESSAGE_SHORTCUT.match(keyEvent)) {
            this.handleMessageSend();
        }
    }

    public void handleClearMessage(MouseEvent mouseEvent) {
        this.chatMessageList.clear();
    }

    public void showUserList(MouseEvent mouseEvent) {
        UserListDialog dialog = new UserListDialog(nodeManager.getUserList());
        dialog.showAndWait();
    }
}
