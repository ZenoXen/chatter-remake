package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.model.bo.FileChunkFetchRequestBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

@Component
public class FileChunkFetchRequestCmdHandler implements TcpCommonCmdHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) {
        FileChunkFetchRequestBO fileChunkFetchRequestBO = (FileChunkFetchRequestBO) payload;
    }
}
