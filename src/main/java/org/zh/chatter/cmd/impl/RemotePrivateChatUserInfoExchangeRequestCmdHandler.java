
package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.component.PrivateChatButtonActions;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.PrivateChatTabManager;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.bo.RemotePrivateChatUserInfoExchangeBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;
import org.zh.chatter.model.vo.UserVO;

import java.io.Serializable;
import java.net.InetSocketAddress;

@Component
public class RemotePrivateChatUserInfoExchangeRequestCmdHandler implements TcpCommonCmdHandler {

    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Resource
    private PrivateChatButtonActions privateChatButtonActions;
    @Resource
    private PrivateChatTabManager privateChatTabManager;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) throws Exception {
        RemotePrivateChatUserInfoExchangeBO remotePrivateChatUserInfoExchangeBO = (RemotePrivateChatUserInfoExchangeBO) payload;
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        UserVO userVO = new UserVO();
        userVO.setId(remotePrivateChatUserInfoExchangeBO.getId());
        userVO.setUsername(remotePrivateChatUserInfoExchangeBO.getUsername());
        userVO.setAddress(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
        userVO.setIsMySelf(false);
        //保存聊天tab
        privateChatButtonActions.getOrInitPrivateChatTab(privateChatTabManager.getChatArea(), userVO);
        //交换用户信息
        RemotePrivateChatUserInfoExchangeBO responseBO = new RemotePrivateChatUserInfoExchangeBO();
        responseBO.setId(currentUser.getId());
        responseBO.setUsername(currentUser.getUsername());
        ctx.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.REMOTE_PRIVATE_CHAT_USER_INFO_EXCHANGE_RESPONSE, dataDTO.getSessionId(), currentUser.getId(), responseBO));
    }
}
