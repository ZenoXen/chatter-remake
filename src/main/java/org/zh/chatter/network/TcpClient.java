package org.zh.chatter.network;

import cn.hutool.core.io.FileUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.TcpConnectionManager;
import org.zh.chatter.model.bo.*;
import org.zh.chatter.model.dto.TcpCommonDataDTO;
import org.zh.chatter.util.Constants;
import org.zh.chatter.util.IdUtil;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

@Component
@Slf4j
public class TcpClient {
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final FileTaskManager fileTaskManager;
    private final CurrentUserInfoHolder currentUserInfoHolder;
    private final TcpConnectionManager tcpConnectionManager;


    public TcpClient(TcpCommonChannelInboundHandler tcpCommonChannelInboundHandler,
                     TcpCommonDataEncoder tcpCommonDataEncoder,
                     FileTaskManager fileTaskManager,
                     CurrentUserInfoHolder currentUserInfoHolder,
                     TcpConnectionManager tcpConnectionManager) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        //outbound
                        pipeline.addLast(tcpCommonDataEncoder);
                        //inbound
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Constants.MAXIMUM_FRAME_LENGTH, Constants.LENGTH_FIELD_OFFSET, Constants.LENGTH_FIELD_LENGTH, Constants.LENGTH_FIELD_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP));
                        //ByteToMessageDecoder无法标为Sharable
                        pipeline.addLast(new TcpCommonDataDecoder());
                        pipeline.addLast(tcpCommonChannelInboundHandler);
                    }
                });
        this.group = group;
        this.bootstrap = bootstrap;
        this.fileTaskManager = fileTaskManager;
        this.currentUserInfoHolder = currentUserInfoHolder;
        this.tcpConnectionManager = tcpConnectionManager;
    }

    public void shutdown() {
        group.shutdownGracefully();
    }

    private Channel connectHost(InetAddress host, int port) {
        Channel channel = tcpConnectionManager.getChannel(host);
        if (channel == null) {
            try {
                channel = bootstrap.connect(host, port).sync().channel();
                return channel;
            } catch (Exception e) {
                log.error("建立tcp连接失败：");
                throw new RuntimeException(e);
            }
        }
        return channel;
    }

    public void sendFileTransferRequest(Channel channel, File file) {
        //文件是目录，或者不存在，跳过
        if (!FileUtil.exist(file) || FileUtil.isDirectory(file)) {
            return;
        }
        //保存channel（用于传输完成后关闭），发送文件传输请求
        String taskId = IdUtil.genId();
        String fileName = file.getName();
        long fileSize = file.length();
        if (Strings.isEmpty(fileName) || fileSize <= 0) {
            return;
        }
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        FileTaskBO fileTaskBO = FileTaskBO.builder().taskId(taskId).fileName(fileName).sourceFilePath(file)
                .fileSize(fileSize).senderId(currentUser.getId()).senderName(currentUser.getUsername()).sendTime(System.currentTimeMillis())
                .status(FileTaskStatusEnum.PENDING).transferProgress(0D).transferredSize(0L).channel(channel).isMySelf(true).build();
        fileTaskManager.addOrUpdateTask(fileTaskBO);
        FileTransferRequestBO requestBO = new FileTransferRequestBO();
        requestBO.setFileSize(fileSize);
        requestBO.setFilename(fileName);
        TcpCommonDataDTO tcpCommonDataDTO = TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_TRANSFER_REQUEST, taskId, currentUser.getId(), requestBO);
        tcpConnectionManager.addReferenceCount(channel);
        channel.writeAndFlush(tcpCommonDataDTO);
        log.info("发送文件请求：{}", tcpCommonDataDTO);
    }

    public void sendRemotePrivateChatUserInfoExchangeRequest(InetAddress address, int port) {
        Channel channel = this.connectHost(address, port);
        String sessionId = IdUtil.genId();
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        RemotePrivateChatUserInfoExchangeBO requestBO = new RemotePrivateChatUserInfoExchangeBO();
        requestBO.setId(currentUser.getId());
        requestBO.setUsername(currentUser.getUsername());
        TcpCommonDataDTO tcpCommonDataDTO = TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.REMOTE_PRIVATE_CHAT_USER_INFO_EXCHANGE_REQUEST, sessionId, currentUser.getId(), requestBO);
        tcpConnectionManager.addReferenceCount(channel);
        channel.writeAndFlush(tcpCommonDataDTO);
        log.info("发送私聊用户信息到{}：{}", address, tcpCommonDataDTO);
    }

    public void sendPrivateChatMessage(Channel channel, String sessionId, String message) {
        NodeUserBO currentUser = currentUserInfoHolder.getCurrentUser();
        ChatMessageBO chatMessageBO = new ChatMessageBO(currentUser, message, LocalDateTime.now());
        TcpCommonDataDTO tcpCommonDataDTO = TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.PRIVATE_CHAT_MESSAGE, sessionId, currentUser.getId(), chatMessageBO);
        channel.writeAndFlush(tcpCommonDataDTO);
        log.info("发送私聊消息到{}：{}", ((InetSocketAddress) channel.remoteAddress()).getAddress(), tcpCommonDataDTO);
    }
}
