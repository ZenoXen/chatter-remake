package org.zh.chatter.component;

import jakarta.annotation.Resource;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.zh.chatter.manager.ChatMessageManager;
import org.zh.chatter.model.vo.UserVO;
import org.zh.chatter.util.Constants;


@Component
public class PrivateChatButtonActions {
    @Resource
    private ChatMessageManager chatMessageManager;
    @Resource
    private FileTaskButtonActions fileTaskButtonActions;

    @Getter
    private EventHandler<MouseEvent> onClearButtonClicked = e -> {
        Button button = (Button) e.getTarget();
        String tabId = (String) button.getProperties().get(Constants.TAB_ID);
        chatMessageManager.clearPrivateChatMessage(tabId);
    };

    @Getter
    private EventHandler<MouseEvent> onSendFileButtonClicked = e -> {
        Button button = (Button) e.getTarget();
        UserVO userVO = (UserVO) button.getProperties().get(Constants.USER_VO);
        fileTaskButtonActions.getSendFileButtonAction().apply(userVO, button);
    };
}
