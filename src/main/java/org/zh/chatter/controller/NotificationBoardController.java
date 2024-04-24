package org.zh.chatter.controller;

import jakarta.annotation.Resource;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import org.springframework.stereotype.Controller;
import org.zh.chatter.component.NotificationCell;
import org.zh.chatter.manager.NotificationManager;
import org.zh.chatter.model.vo.NotificationVO;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class NotificationBoardController implements Initializable {

    @Resource
    private NotificationManager notificationManager;
    @FXML
    private ListView<NotificationVO> notificationBoard;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        notificationBoard.setItems(notificationManager.getNotificationList());
        notificationBoard.setCellFactory(c -> new NotificationCell());
    }

    public void addNotification(NotificationVO notificationVO) {
        notificationManager.addNotification(notificationVO);
    }

    public void clearAllNotification() {
        notificationManager.clearAllNotification();
    }
}
