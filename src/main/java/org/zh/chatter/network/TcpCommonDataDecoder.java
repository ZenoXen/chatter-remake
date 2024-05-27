package org.zh.chatter.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@ChannelHandler.Sharable
public class TcpCommonDataDecoder extends ByteToMessageDecoder {

    private static final int SESSION_ID_LENGTH = 32;
    private static final int USER_ID_LENGTH = 32;
    private static final int MINIMUM_PAYLOAD_LENGTH = 76;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < MINIMUM_PAYLOAD_LENGTH) {
            log.warn("接收到的tcp包小于协议最小包大小，跳过");
            return;
        }
        TcpCommonDataDTO tcpCommonDataDTO = new TcpCommonDataDTO();
        tcpCommonDataDTO.setProtocolVersion(in.readByte());
        tcpCommonDataDTO.setMessageType(in.readByte());
        tcpCommonDataDTO.setSessionId(in.readBytes(SESSION_ID_LENGTH).toString(StandardCharsets.UTF_8));
        tcpCommonDataDTO.setUserId(in.readBytes(USER_ID_LENGTH).toString(StandardCharsets.UTF_8));
        tcpCommonDataDTO.setTimestamp(in.readLong());
        short payloadLength = in.readShort();
        if (payloadLength <= 0) {
            payloadLength = 0;
        }
        tcpCommonDataDTO.setPayloadLength(payloadLength);
        byte[] payload = new byte[payloadLength];
        in.readBytes(payload);
        tcpCommonDataDTO.setPayload(payload);
        out.add(tcpCommonDataDTO);
    }
}
