package org.zh.chatter.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.UdpCommonDataTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.NetworkInterfaceHolder;
import org.zh.chatter.model.bo.ChatMessageBO;
import org.zh.chatter.model.bo.MulticastAddressBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.dto.UdpCommonDataDTO;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;

@Component
@Slf4j
public class UdpServer implements Runnable {

    private final EventLoopGroup group;
    private NioDatagramChannel channel;
    private final CurrentUserInfoHolder currentUserInfoHolder;
    private final Integer port;
    private final ObjectMapper objectMapper;
    private final NetworkInterfaceHolder networkInterfaceHolder;
    private final UdpCommonDataDecoder udpCommonDataDecoder;
    private final UdpCommonChannelInboundHandler udpCommonChannelInboundHandler;
    private final UdpCommonDataEncoder udpCommonDataEncoder;


    public UdpServer(UdpCommonDataDecoder udpCommonDataDecoder,
                     UdpCommonChannelInboundHandler udpCommonChannelInboundHandler,
                     UdpCommonDataEncoder udpCommonDataEncoder,
                     CurrentUserInfoHolder currentUserInfoHolder,
                     ObjectMapper objectMapper,
                     NetworkInterfaceHolder networkInterfaceHolder,
                     @Value("${app.port.udp}") Integer port) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        this.group = group;
        this.currentUserInfoHolder = currentUserInfoHolder;
        this.objectMapper = objectMapper;
        this.networkInterfaceHolder = networkInterfaceHolder;
        this.port = port;
        this.udpCommonDataDecoder = udpCommonDataDecoder;
        this.udpCommonChannelInboundHandler = udpCommonChannelInboundHandler;
        this.udpCommonDataEncoder = udpCommonDataEncoder;
        this.bootstrapUdpServer();
    }

    private void bootstrapUdpServer() {
        Bootstrap bootstrap = new Bootstrap();
        MulticastAddressBO addressBO = networkInterfaceHolder.getSelectedLocalAddress();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .localAddress(addressBO.getAddress(), port)
                .option(ChannelOption.IP_MULTICAST_IF, addressBO.getNetworkInterface())
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        //inbound
                        pipeline.addLast(udpCommonDataDecoder);
                        pipeline.addLast(udpCommonChannelInboundHandler);
                        //outbound
                        pipeline.addLast(udpCommonDataEncoder);
                    }
                });
        try {
            NioDatagramChannel channel = (NioDatagramChannel) bootstrap.bind(port).sync().channel();
            InetSocketAddress multicastAddress = networkInterfaceHolder.getMulticastAddress();
            channel.joinGroup(multicastAddress, addressBO.getNetworkInterface()).sync();
            this.channel = channel;
        } catch (Exception e) {
            log.error("心跳服务绑定失败");
        }
    }

    @Override
    public void run() {
        try {
            log.info("心跳监听服务已启动");
            channel.closeFuture().await();
        } catch (InterruptedException e) {
            log.error("启动心跳监听服务失败");
        }
    }

    public void stopListening(boolean shutdownGroup) {
        channel.close();
        if (shutdownGroup) {
            group.shutdownGracefully();
            log.info("心跳服务已关闭");
        }
    }

    public void changeNetworkInterface(NetworkInterface networkInterface) {
        this.stopListening(false);
        networkInterfaceHolder.saveNetworkInterfaceReference(networkInterface);
        this.bootstrapUdpServer();
    }

    public void sendHeartbeat() throws Exception {
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        InetAddress address = networkInterfaceHolder.getMulticastAddress().getAddress();
        UdpCommonDataDTO udpCommonDataDTO = new UdpCommonDataDTO(UdpCommonDataTypeEnum.HEARTBEAT.getCode(), null, address, port, objectMapper.writeValueAsString(currentUser));
        channel.writeAndFlush(udpCommonDataDTO);
        log.debug("发送心跳信息： {} {}", networkInterfaceHolder.getSelectedNetworkInterface().getDisplayName(), address.getHostAddress());
    }

    public void sendOfflineNotification() throws Exception {
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        InetAddress address = networkInterfaceHolder.getMulticastAddress().getAddress();
        UdpCommonDataDTO udpCommonDataDTO = new UdpCommonDataDTO(UdpCommonDataTypeEnum.OFFLINE_NOTIFICATION.getCode(), null, address, port, objectMapper.writeValueAsString(currentUser));
        channel.writeAndFlush(udpCommonDataDTO);
        log.debug("发送离线通知： {} {}", networkInterfaceHolder.getSelectedNetworkInterface().getDisplayName(), address.getHostAddress());
    }

    public void sendChatMessage(String message) throws Exception {
        InetAddress address = networkInterfaceHolder.getMulticastAddress().getAddress();
        this.sendChatMessage(message, address, UdpCommonDataTypeEnum.GROUP_CHAT_MESSAGE);
    }

    public void sendChatMessage(String message, InetAddress address, UdpCommonDataTypeEnum type) throws Exception {
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        ChatMessageBO chatMessageBO = new ChatMessageBO(currentUser, message, LocalDateTime.now());
        UdpCommonDataDTO udpCommonDataDTO = new UdpCommonDataDTO(type.getCode(), null, address, port, objectMapper.writeValueAsString(chatMessageBO));
        channel.writeAndFlush(udpCommonDataDTO);
        log.debug("发送聊天消息： {} {}", networkInterfaceHolder.getSelectedNetworkInterface().getDisplayName(), address.getHostAddress());
    }
}
