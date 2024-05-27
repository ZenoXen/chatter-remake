package org.zh.chatter.cmd;

import io.netty.channel.ChannelHandlerContext;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

public interface TcpCommonCmdHandler {
    void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload);
}
