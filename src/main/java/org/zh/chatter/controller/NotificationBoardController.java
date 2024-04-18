package org.zh.chatter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import org.springframework.stereotype.Controller;
import org.zh.chatter.component.NotificationCell;
import org.zh.chatter.model.vo.NotificationVO;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class NotificationBoardController implements Initializable {

    @FXML
    private ListView<NotificationVO> notificationBoard;

    private ObservableList<NotificationVO> notificationList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        notificationList = FXCollections.observableArrayList();
        notificationBoard.setItems(notificationList);
        notificationBoard.setCellFactory(c -> new NotificationCell());
    }

    public void addNotification(NotificationVO notificationVO) {
        notificationList.add(notificationVO);
    }

    public void clearAllNotification() {
        notificationList.clear();
    }
}
