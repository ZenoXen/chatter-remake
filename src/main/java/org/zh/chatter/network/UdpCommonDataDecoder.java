package org.zh.chatter.network;

import cn.hutool.cache.Cache;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.manager.NetworkInterfaceHolder;
import org.zh.chatter.model.dto.UdpCommonDataDTO;
import org.zh.chatter.util.NetworkUtil;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@ChannelHandler.Sharable
public class UdpCommonDataDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private NetworkInterfaceHolder networkInterfaceHolder;
    @Resource
    private Cache<String, Object> messageFilterCache;

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        InetSocketAddress sender = msg.sender();
        //如果接收到udp包的网卡并非当前选择的网卡，跳过不处理
        if (!NetworkUtil.isFromSelectedNetworkInterface(sender, networkInterfaceHolder.getSelectedNetworkInterface())) {
            return;
        }
        //如果发送者地址是本地地址
        if (NetworkUtil.isLocalAddress(sender.getAddress())) {
            return;
        }
        ByteBuf byteBuf = msg.content();
        if (byteBuf.readableBytes() <= 0) {
            return;
        }
        UdpCommonDataDTO udpCommonDataDTO = objectMapper.readValue(byteBuf.toString(StandardCharsets.UTF_8), UdpCommonDataDTO.class);
        udpCommonDataDTO.setFromAddress(sender.getAddress());
        if (messageFilterCache.containsKey(udpCommonDataDTO.getMessageId())) {
            log.warn("收到重复udp消息，抛弃：{}", udpCommonDataDTO.getMessageId());
            return;
        }
        log.debug("接收commonDataDTO: {}", udpCommonDataDTO);
        out.add(udpCommonDataDTO);
    }
}
