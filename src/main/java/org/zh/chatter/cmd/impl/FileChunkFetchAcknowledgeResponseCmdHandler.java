package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.model.bo.FileChunkFetchAcknowledgeResponseBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

@Component
public class FileChunkFetchAcknowledgeResponseCmdHandler implements TcpCommonCmdHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) {
        FileChunkFetchAcknowledgeResponseBO fileChunkFetchAcknowledgeResponseBO = (FileChunkFetchAcknowledgeResponseBO) payload;
    }
}
