package org.zh.chatter.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zh.chatter.util.Constants;

@Component
@Slf4j
public class TcpServer implements Runnable {

    public static final int BOSS_GROUP_THREAD_NUM = 1;
    public static final int SO_BACKLOG_VALUE = 100;

    private final Channel channel;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;


    public TcpServer(@Value("${app.port.tcp}") Integer port,
                     TcpCommonChannelInboundHandler tcpCommonChannelInboundHandler,
                     TcpCommonDataEncoder tcpCommonDataEncoder) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(BOSS_GROUP_THREAD_NUM),
                workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, SO_BACKLOG_VALUE)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        //inbound
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Constants.MAXIMUM_FRAME_LENGTH, Constants.LENGTH_FIELD_OFFSET, Constants.LENGTH_FIELD_LENGTH, Constants.LENGTH_FIELD_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP));
                        pipeline.addLast(new TcpCommonDataDecoder());
                        pipeline.addLast(tcpCommonChannelInboundHandler);
                        //outbound
                        pipeline.addLast(tcpCommonDataEncoder);
                    }
                });
        this.channel = bootstrap.bind(port).sync().channel();
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
