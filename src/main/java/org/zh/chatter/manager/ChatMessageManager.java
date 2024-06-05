package org.zh.chatter.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.vo.ChatMessageVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Component
public class ChatMessageManager {
    private final ObservableList<ChatMessageVO> groupChatMessageList;
    private final Map<String, ObservableList<ChatMessageVO>> privateChatMessageListMap;

    public ChatMessageManager() {
        this.groupChatMessageList = FXCollections.observableArrayList();
        this.privateChatMessageListMap = new HashMap<>();
    }

    public void clearGroupChatMessage() {
        groupChatMessageList.clear();
    }

    public void addGroupChatMessage(ChatMessageVO chatMessageVO) {
        groupChatMessageList.add(chatMessageVO);
    }

    public void clearPrivateChatMessage(String tabId) {
        Optional.ofNullable(privateChatMessageListMap.get(tabId)).ifPresent(List::clear);
    }

    public ObservableList<ChatMessageVO> initPrivateChatMessageList(String tabId) {
        ObservableList<ChatMessageVO> list = FXCollections.observableArrayList();
        privateChatMessageListMap.put(tabId, list);
        return list;
    }

    public void addPrivateChatMessage(String tabId, ChatMessageVO chatMessageVO) {
        privateChatMessageListMap.get(tabId).add(chatMessageVO);
    }
}
