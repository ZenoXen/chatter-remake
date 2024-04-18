package org.zh.chatter.component;

import cn.hutool.core.date.LocalDateTimeUtil;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import org.zh.chatter.model.vo.ChatMessageVO;

public class ChatMessageCell extends ListCell<ChatMessageVO> {

    private static final String CHAT_MESSAGE_TEMPLATE = "%s     %s\n%s";

    @Override
    protected void updateItem(ChatMessageVO chatMessageVO, boolean empty) {
        super.updateItem(chatMessageVO, empty);
        if (chatMessageVO == null || empty) {
            this.setText(null);
            this.setGraphic(null);
        } else {
            Text messageText = new Text(String.format(CHAT_MESSAGE_TEMPLATE, chatMessageVO.getSenderName(), LocalDateTimeUtil.formatNormal(chatMessageVO.getSendTime()), chatMessageVO.getMessage()));
            this.setGraphic(messageText);
        }
    }
}
