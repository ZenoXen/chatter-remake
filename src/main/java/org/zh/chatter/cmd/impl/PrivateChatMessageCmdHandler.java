
package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.component.PrivateChatButtonActions;
import org.zh.chatter.manager.ChatMessageManager;
import org.zh.chatter.manager.PrivateChatTabManager;
import org.zh.chatter.model.bo.ChatMessageBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;
import org.zh.chatter.model.vo.ChatMessageVO;
import org.zh.chatter.model.vo.UserVO;

import java.io.Serializable;
import java.net.InetSocketAddress;

@Component
public class PrivateChatMessageCmdHandler implements TcpCommonCmdHandler {

    @Resource
    private PrivateChatButtonActions privateChatButtonActions;
    @Resource
    private PrivateChatTabManager privateChatTabManager;
    @Resource
    private ChatMessageManager chatMessageManager;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) throws Exception {
        //保存tab
        ChatMessageBO chatMessageBO = (ChatMessageBO) payload;
        UserVO userVO = new UserVO();
        userVO.setId(chatMessageBO.getUser().getId());
        userVO.setUsername(chatMessageBO.getUser().getUsername());
        userVO.setAddress(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
        userVO.setIsMySelf(false);
        TabPane chatArea = privateChatTabManager.getChatArea();
        Tab tab = privateChatButtonActions.getOrInitPrivateChatTab(chatArea, userVO, ctx.channel(), dataDTO.getSessionId());
        chatMessageManager.addPrivateChatMessage(tab.getId(), new ChatMessageVO(userVO.getId(), userVO.getUsername(), chatMessageBO.getMessage(), chatMessageBO.getSendTime()));
    }
}
