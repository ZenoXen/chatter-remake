package org.zh.chatter.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.util.NetworkUtil;
import org.zh.chatter.model.dto.UdpCommonDataDTO;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class UdpCommonDataDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        if (NetworkUtil.isLocalAddress(msg.sender().getAddress())) {
            return;
        }
        ByteBuf byteBuf = msg.content();
        if (byteBuf.readableBytes() <= 0) {
            return;
        }
        UdpCommonDataDTO udpCommonDataDTO = objectMapper.readValue(byteBuf.toString(StandardCharsets.UTF_8), UdpCommonDataDTO.class);
        udpCommonDataDTO.setFromAddress(msg.sender().getAddress());
        log.info("接收commonDataDTO: {}", udpCommonDataDTO);
        out.add(udpCommonDataDTO);
    }
}
