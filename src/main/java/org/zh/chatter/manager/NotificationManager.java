package org.zh.chatter.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.vo.NotificationVO;

@Getter
@Component
public class NotificationManager {
    private final ObservableList<NotificationVO> notificationList;

    public NotificationManager() {
        this.notificationList = FXCollections.observableArrayList();
    }

    public void addNotification(NotificationVO notificationVO) {
        notificationList.add(notificationVO);
    }

    public void clearAllNotification() {
        notificationList.clear();
    }

}
