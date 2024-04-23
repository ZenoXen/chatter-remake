package org.zh.chatter.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.dto.UdpCommonDataDTO;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class UdpCommonDataEncoder extends MessageToMessageEncoder<UdpCommonDataDTO> {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void encode(ChannelHandlerContext ctx, UdpCommonDataDTO msg, List<Object> out) throws Exception {
        DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(objectMapper.writeValueAsString(msg).getBytes(StandardCharsets.UTF_8)), new InetSocketAddress(msg.getToAddress(), msg.getPort()));
        out.add(packet);
    }
}
