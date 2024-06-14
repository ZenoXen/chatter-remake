package org.zh.chatter.network;

import cn.hutool.core.util.SerializeUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.TcpConnectionManager;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

@Component
@Slf4j
@ChannelHandler.Sharable
public class TcpCommonChannelInboundHandler extends SimpleChannelInboundHandler<TcpCommonDataDTO> {
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private TcpConnectionManager tcpConnectionManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Channel channel = ctx.channel();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
        InetAddress address = inetSocketAddress.getAddress();
        log.debug("接收到tcp连接请求：address = {}", address);
        tcpConnectionManager.addOrUpdateChannel(address, channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel channel = ctx.channel();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
        InetAddress address = inetSocketAddress.getAddress();
        log.debug("tcp连接已断开：address = {}", address);
        tcpConnectionManager.removeAndCloseChannel(address);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("tcp连接过程中发生异常：", cause);
        Channel channel = ctx.channel();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
        InetAddress address = inetSocketAddress.getAddress();
        tcpConnectionManager.removeAndCloseChannel(address);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO) throws Exception {
        //根据messageType决定将payload解析成哪个类
        TcpCmdTypeEnum type = TcpCmdTypeEnum.getByCode(dataDTO.getMessageType());
        if (type == null) {
            log.warn("tcp消息解析失败，未知的messageType={}", dataDTO.getMessageType());
            return;
        }
        byte[] payload = dataDTO.getPayload();
        Serializable deserialized = SerializeUtil.deserialize(payload);
        TcpCommonCmdHandler handler = applicationContext.getBean(type.getHandlerClass());
        log.debug("分发payload处理，type = {}, deserialized = {}", type, deserialized);
        handler.handle(ctx, dataDTO, deserialized);
    }
}
