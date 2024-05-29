package org.zh.chatter.network;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.bo.FileTransferRequestBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;
import org.zh.chatter.model.vo.UserVO;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalDateTime;

@Component
@Slf4j
public class TcpClient {
    private final EventLoopGroup group;
    private final Integer serverTcpPort;
    private final Bootstrap bootstrap;
    private final FileTaskManager fileTaskManager;
    private final CurrentUserInfoHolder currentUserInfoHolder;


    public TcpClient(@Value("${app.port.tcp}") Integer port,
                     LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder,
                     TcpCommonChannelInboundHandler tcpCommonChannelInboundHandler,
                     TcpCommonDataEncoder tcpCommonDataEncoder,
                     FileTaskManager fileTaskManager,
                     CurrentUserInfoHolder currentUserInfoHolder) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //inbound
                pipeline.addLast(lengthFieldBasedFrameDecoder);
                //ByteToMessageDecoder无法标为Sharable
                pipeline.addLast(new TcpCommonDataDecoder());
                pipeline.addLast(tcpCommonChannelInboundHandler);
                //outbound
                pipeline.addLast(tcpCommonDataEncoder);
            }
        });
        this.group = group;
        this.serverTcpPort = port;
        this.bootstrap = bootstrap;
        this.fileTaskManager = fileTaskManager;
        this.currentUserInfoHolder = currentUserInfoHolder;
    }

    public void shutdown() {
        group.shutdownGracefully();
    }

    public NioSocketChannel connectHost(InetAddress host) {
        try {
            return (NioSocketChannel) bootstrap.connect(host, serverTcpPort).sync().channel();
        } catch (Exception e) {
            log.error("建立tcp连接失败：", e);
        }
        return null;
    }

    public void sendFileTransferRequest(UserVO userVO, File file) {
        //不会给自己发送文件
        if (userVO.getIsMySelf()) {
            return;
        }
        if (!FileUtil.exist(file) || FileUtil.isDirectory(file)) {
            return;
        }
        //保存channel，发送文件传输请求
        NioSocketChannel channel = this.connectHost(userVO.getAddress());
        String taskId = UUID.fastUUID().toString(true);

        String fileName = file.getName();
        long fileSize = file.length();
        if (Strings.isEmpty(fileName) || fileSize <= 0) {
            return;
        }
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        FileTaskBO fileTaskBO = FileTaskBO.builder().taskId(taskId).fileName(fileName)
                .fileSize(fileSize).senderId(currentUser.getId()).senderName(currentUser.getUsername()).sendTime(LocalDateTime.now())
                .status(FileTaskStatusEnum.PENDING).transferProgress(0D).transferredSize(0L).channel(channel).isMySelf(true).build();
        fileTaskManager.addOrUpdateTask(fileTaskBO);
        FileTransferRequestBO requestBO = new FileTransferRequestBO();
        requestBO.setFileSize(fileSize);
        requestBO.setFilename(fileName);
        channel.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_TRANSFER_REQUEST, taskId, currentUser.getId(), requestBO));
    }
}
