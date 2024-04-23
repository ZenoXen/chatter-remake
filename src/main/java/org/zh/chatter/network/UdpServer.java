package org.zh.chatter.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UdpServer implements Runnable, DisposableBean {
    @Value("${app.port.heart-beat}")
    private Integer port;

    @Resource
    private CommonDataDecoder commonDataDecoder;
    @Resource
    private CommonChannelInboundHandler commonChannelInboundHandler;
    private Channel channel;

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(commonDataDecoder);
                            pipeline.addLast(commonChannelInboundHandler);
                        }
                    });
            Channel channel = bootstrap.bind(port).sync().channel();
            this.channel = channel;
            channel.closeFuture().await();
        } catch (InterruptedException e) {
            log.error("启动心跳监听服务失败");
        } finally {
            log.info("心跳服务已关闭");
            group.shutdownGracefully();
        }
    }

    @Override
    public void destroy() throws Exception {
        channel.close();
    }
}
