package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.model.bo.FileTransferRequestBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

public class FileTransferRequestCmdHandler implements TcpCommonCmdHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) {
        FileTransferRequestBO fileTransferRequestBO = (FileTransferRequestBO) payload;
    }
}
