package org.zh.chatter.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@ChannelHandler.Sharable
public class TcpCommonDataEncoder extends MessageToByteEncoder<TcpCommonDataDTO> {
    @Override
    protected void encode(ChannelHandlerContext ctx, TcpCommonDataDTO msg, ByteBuf out) throws Exception {
        out.writeByte(msg.getProtocolVersion());
        out.writeByte(msg.getMessageType());
        out.writeCharSequence(msg.getSessionId(), StandardCharsets.UTF_8);
        out.writeCharSequence(msg.getUserId(), StandardCharsets.UTF_8);
        out.writeLong(msg.getTimestamp());
        out.writeShort(msg.getPayloadLength());
        out.writeBytes(msg.getPayload());
    }
}
