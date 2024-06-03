package org.zh.chatter.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class TcpCommonDataDecoder extends ByteToMessageDecoder {

    private static final int SESSION_ID_LENGTH = 32;
    private static final int USER_ID_LENGTH = 32;
    private static final int MINIMUM_PAYLOAD_LENGTH = 76;


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < MINIMUM_PAYLOAD_LENGTH) {
            log.warn("接收到的tcp包小于协议最小包大小，跳过");
            return;
        }
        log.debug("ByteBuf in size：{}", in.readableBytes());
        TcpCommonDataDTO tcpCommonDataDTO = new TcpCommonDataDTO();
        tcpCommonDataDTO.setProtocolVersion(in.readByte());
        tcpCommonDataDTO.setMessageType(in.readByte());
        ByteBuf sessionIdByteBuf = in.readBytes(SESSION_ID_LENGTH);
        tcpCommonDataDTO.setSessionId(sessionIdByteBuf.toString(StandardCharsets.UTF_8));
        sessionIdByteBuf.release();
        ByteBuf userIdByteBuf = in.readBytes(USER_ID_LENGTH);
        tcpCommonDataDTO.setUserId(userIdByteBuf.toString(StandardCharsets.UTF_8));
        userIdByteBuf.release();
        tcpCommonDataDTO.setTimestamp(in.readLong());
        long payloadLength = in.readLong();
        if (payloadLength <= 0) {
            payloadLength = 0;
        }
        tcpCommonDataDTO.setPayloadLength(payloadLength);
        byte[] payload = new byte[(int) payloadLength];
        in.readBytes(payload);
        tcpCommonDataDTO.setPayload(payload);
        log.debug("接收到来自{}的TcpCommonData：{}", ctx.channel().remoteAddress(), tcpCommonDataDTO);
        out.add(tcpCommonDataDTO);
    }
}
