package org.zh.chatter.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TcpServer implements Runnable {

    public static final int BOSS_GROUP_THREAD_NUM = 1;
    public static final int SO_BACKLOG_VALUE = 100;

    private final NioServerSocketChannel channel;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;


    public TcpServer(@Value("${app.port.tcp}") Integer port,
                     LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder,
                     TcpCommonDataDecoder tcpCommonDataDecoder,
                     TcpCommonChannelInboundHandler tcpCommonChannelInboundHandler,
                     TcpCommonDataEncoder tcpCommonDataEncoder) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(BOSS_GROUP_THREAD_NUM),
                workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, SO_BACKLOG_VALUE)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
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
        this.channel = (NioServerSocketChannel) bootstrap.bind(port).sync().channel();
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
    }

    @Override
    public void run() {
        try {
            log.info("tcp监听服务已启动");
            channel.closeFuture().await();
        } catch (InterruptedException e) {
            log.error("启动tcp监听服务失败");
        } finally {
            log.info("tcp服务已关闭");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stopListening() {
        channel.close();
    }
}
