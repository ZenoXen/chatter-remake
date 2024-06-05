package org.zh.chatter.controller;

import jakarta.annotation.Resource;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Controller;
import org.zh.chatter.component.ChatMessageCell;
import org.zh.chatter.component.FileTaskButtonActions;
import org.zh.chatter.component.FileTransferDialog;
import org.zh.chatter.component.UserListDialog;
import org.zh.chatter.manager.*;
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
    public static final int FILE_TRANSFER_DIALOG_WIDTH = 300;
    public static final int FILE_TRANSFER_DIALOG_HEIGHT = 400;
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
    private FileTaskManager fileTaskManager;
    @Resource
    private FileTaskButtonActions fileTaskButtonActions;
    @Resource
    private PrivateChatTabManager privateChatTabManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        groupMessageArea.setItems(chatMessageManager.getGroupChatMessageList());
        groupMessageArea.setCellFactory(c -> new ChatMessageCell());
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
        if (SEND_MESSAGE_SHORTCUT.match(keyEvent)) {
            this.handleGroupMessageSend();
        }
    }

    public void handleClearGroupMessage(MouseEvent mouseEvent) {
        chatMessageManager.clearGroupChatMessage();
    }

    public void showUserList(MouseEvent mouseEvent) {
        UserListDialog dialog = new UserListDialog(nodeManager.getUserList(), fileTaskButtonActions, chatArea);
        dialog.show();
    }

    public void openFileTransferDialog(MouseEvent mouseEvent) {
        FileTransferDialog dialog = new FileTransferDialog(FILE_TRANSFER_DIALOG_WIDTH, FILE_TRANSFER_DIALOG_HEIGHT, fileTaskButtonActions, fileTaskManager.getInactiveTasks(), fileTaskManager.getOngoingTasks());
        dialog.show();
    }

    public void handleClosePrivateChatTab(Event event) {
        Tab tab = (Tab) event.getTarget();
        privateChatTabManager.removeTab(tab.getId());
    }
}
