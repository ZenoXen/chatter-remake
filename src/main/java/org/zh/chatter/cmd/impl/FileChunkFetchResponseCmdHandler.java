package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.model.bo.FileChunkFetchResponseBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

public class FileChunkFetchResponseCmdHandler implements TcpCommonCmdHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) {
        FileChunkFetchResponseBO fileChunkFetchResponseBO = (FileChunkFetchResponseBO) payload;
    }
}
