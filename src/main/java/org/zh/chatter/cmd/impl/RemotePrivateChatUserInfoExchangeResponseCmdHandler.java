
package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.component.PrivateChatButtonActions;
import org.zh.chatter.manager.PrivateChatTabManager;
import org.zh.chatter.model.bo.RemotePrivateChatUserInfoExchangeBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;
import org.zh.chatter.model.vo.UserVO;

import java.io.Serializable;
import java.net.InetSocketAddress;

@Component
public class RemotePrivateChatUserInfoExchangeResponseCmdHandler implements TcpCommonCmdHandler {

    @Resource
    private PrivateChatButtonActions privateChatButtonActions;
    @Resource
    private PrivateChatTabManager privateChatTabManager;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) throws Exception {
        //保存tab
        RemotePrivateChatUserInfoExchangeBO remotePrivateChatUserInfoExchangeBO = (RemotePrivateChatUserInfoExchangeBO) payload;
        UserVO userVO = new UserVO();
        userVO.setId(remotePrivateChatUserInfoExchangeBO.getId());
        userVO.setUsername(remotePrivateChatUserInfoExchangeBO.getUsername());
        userVO.setAddress(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
        userVO.setIsMySelf(false);
        TabPane chatArea = privateChatTabManager.getChatArea();
        Tab tab = privateChatButtonActions.getOrInitPrivateChatTab(chatArea, userVO);
        chatArea.getSelectionModel().select(tab);
    }
}
