package org.zh.chatter.component;

import cn.hutool.core.date.LocalDateTimeUtil;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import org.zh.chatter.model.vo.NotificationVO;

public class NotificationCell extends ListCell<NotificationVO> {

    private static final String NOTIFICATION_TEMPLATE = "%s\n%s     %s";

    @Override
    protected void updateItem(NotificationVO notificationVO, boolean empty) {
        super.updateItem(notificationVO, empty);
        if (notificationVO == null || empty) {
            this.setText(null);
            this.setGraphic(null);
        } else {
            Text notificationText = new Text(String.format(NOTIFICATION_TEMPLATE, LocalDateTimeUtil.formatNormal(notificationVO.getTime()), notificationVO.getType().getDesc(), notificationVO.getContent()));
            this.setGraphic(notificationText);
        }
    }
}
