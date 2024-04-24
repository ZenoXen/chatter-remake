package org.zh.chatter.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.vo.ChatMessageVO;

@Getter
@Component
public class ChatMessageManager {
    private final ObservableList<ChatMessageVO> chatMessageList;

    public ChatMessageManager() {
        this.chatMessageList = FXCollections.observableArrayList();
    }

    public void clearChatMessage() {
        chatMessageList.clear();
    }

    public void addChatMessage(ChatMessageVO chatMessageVO) {
        chatMessageList.add(chatMessageVO);
    }
}
