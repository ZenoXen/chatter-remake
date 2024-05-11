package org.zh.chatter.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.CommonDataTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.NetworkInterfaceHolder;
import org.zh.chatter.model.bo.BroadcastAddressBO;
import org.zh.chatter.model.bo.ChatMessageBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.dto.UdpCommonDataDTO;

import java.time.LocalDateTime;

@Component
@Slf4j
public class UdpServer implements Runnable {

    private final EventLoopGroup group;
    private final Channel channel;
    private final CurrentUserInfoHolder currentUserInfoHolder;
    private final Integer port;
    private final ObjectMapper objectMapper;
    private final NetworkInterfaceHolder networkInterfaceHolder;

    public UdpServer(UdpCommonDataDecoder udpCommonDataDecoder, UdpCommonChannelInboundHandler udpCommonChannelInboundHandler, UdpCommonDataEncoder udpCommonDataEncoder, CurrentUserInfoHolder currentUserInfoHolder, ObjectMapper objectMapper, NetworkInterfaceHolder networkInterfaceHolder, @Value("${app.port.udp}") Integer port) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true).handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(udpCommonDataDecoder);
                        pipeline.addLast(udpCommonChannelInboundHandler);
                        pipeline.addLast(udpCommonDataEncoder);
                    }
                });
        this.channel = bootstrap.bind(port).sync().channel();
        this.group = group;
        this.currentUserInfoHolder = currentUserInfoHolder;
        this.objectMapper = objectMapper;
        this.networkInterfaceHolder = networkInterfaceHolder;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            log.info("心跳监听服务已启动");
            channel.closeFuture().await();
        } catch (InterruptedException e) {
            log.error("启动心跳监听服务失败");
        } finally {
            log.info("心跳服务已关闭");
            group.shutdownGracefully();
        }
    }

    public void stopListening() {
        channel.close();
    }

    public void sendHeartbeat() throws Exception {
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        for (BroadcastAddressBO broadcastAddressBO : networkInterfaceHolder.getBroadcastAddressList()) {
            UdpCommonDataDTO udpCommonDataDTO = new UdpCommonDataDTO(CommonDataTypeEnum.HEARTBEAT.getCode(), null, broadcastAddressBO.getAddress(), port, objectMapper.writeValueAsString(currentUser));
            channel.writeAndFlush(udpCommonDataDTO);
            log.info("发送心跳信息： {} {}", broadcastAddressBO.getNetworkInterface().getDisplayName(), broadcastAddressBO.getAddress().getHostAddress());
        }
    }

    public void sendOfflineNotification() throws Exception {
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        for (BroadcastAddressBO broadcastAddressBO : networkInterfaceHolder.getBroadcastAddressList()) {
            UdpCommonDataDTO udpCommonDataDTO = new UdpCommonDataDTO(CommonDataTypeEnum.OFFLINE_NOTIFICATION.getCode(), null, broadcastAddressBO.getAddress(), port, objectMapper.writeValueAsString(currentUser));
            channel.writeAndFlush(udpCommonDataDTO);
            log.info("发送离线通知： {} {}", broadcastAddressBO.getNetworkInterface().getDisplayName(), broadcastAddressBO.getAddress().getHostAddress());
        }
    }

    public void sendChatMessage(String message) throws Exception {
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        ChatMessageBO chatMessageBO = new ChatMessageBO(currentUser, message, LocalDateTime.now());
        for (BroadcastAddressBO broadcastAddressBO : networkInterfaceHolder.getBroadcastAddressList()) {
            UdpCommonDataDTO udpCommonDataDTO = new UdpCommonDataDTO(CommonDataTypeEnum.CHAT_MESSAGE.getCode(), null, broadcastAddressBO.getAddress(), port, objectMapper.writeValueAsString(chatMessageBO));
            channel.writeAndFlush(udpCommonDataDTO);
            log.info("发送聊天消息： {} {}", broadcastAddressBO.getNetworkInterface().getDisplayName(), broadcastAddressBO.getAddress().getHostAddress());
        }
    }
}
