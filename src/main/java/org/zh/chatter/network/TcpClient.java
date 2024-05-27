package org.zh.chatter.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TcpClient {

    private final EventLoopGroup group;
    private final Integer serverTcpPort;
    private Bootstrap bootstrap;


    public TcpClient(@Value("${app.port.tcp}") Integer port, LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder, TcpCommonDataDecoder tcpCommonDataDecoder, TcpCommonChannelInboundHandler tcpCommonChannelInboundHandler, TcpCommonDataEncoder tcpCommonDataEncoder) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //inbound
                pipeline.addLast(lengthFieldBasedFrameDecoder);
                pipeline.addLast(tcpCommonDataDecoder);
                pipeline.addLast(tcpCommonChannelInboundHandler);
                //outbound
                pipeline.addLast(tcpCommonDataEncoder);
            }
        });
        this.group = group;
        this.serverTcpPort = port;
        this.bootstrap = bootstrap;
    }

    public void shutdown() {
        group.shutdownGracefully();
    }

    public ChannelFuture connectHost(String host) throws InterruptedException {
        return bootstrap.connect(host, serverTcpPort).sync();
    }
}
