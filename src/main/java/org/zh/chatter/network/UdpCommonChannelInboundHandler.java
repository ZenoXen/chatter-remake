package org.zh.chatter.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.CommonDataTypeEnum;
import org.zh.chatter.enums.NotificationTypeEnum;
import org.zh.chatter.manager.ChatMessageManager;
import org.zh.chatter.manager.NodeManager;
import org.zh.chatter.manager.NotificationManager;
import org.zh.chatter.model.bo.ChatMessageBO;
import org.zh.chatter.model.bo.NodeBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.dto.UdpCommonDataDTO;
import org.zh.chatter.model.vo.ChatMessageVO;
import org.zh.chatter.model.vo.NotificationVO;

import java.net.InetAddress;
import java.time.LocalDateTime;

@Component
@Slf4j
public class UdpCommonChannelInboundHandler extends SimpleChannelInboundHandler<UdpCommonDataDTO> {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private NodeManager nodeManager;
    @Resource
    private ChatMessageManager chatMessageManager;
    @Resource
    private NotificationManager notificationManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UdpCommonDataDTO udpCommonDataDTO) throws Exception {
        CommonDataTypeEnum typeEnum = CommonDataTypeEnum.getByCode(udpCommonDataDTO.getType());
        switch (typeEnum) {
            case HEARTBEAT -> this.handleHeartbeat(ctx, udpCommonDataDTO);
            case CHAT_MESSAGE -> this.handleChatMessage(ctx, udpCommonDataDTO);
            case OFFLINE_NOTIFICATION -> this.handleOfflineNotification(ctx, udpCommonDataDTO);
            default -> log.error("接收到未知类型的udp消息：{}", udpCommonDataDTO);
        }
    }

    private void handleHeartbeat(ChannelHandlerContext ctx, UdpCommonDataDTO udpCommonDataDTO) throws JsonProcessingException {
        NodeUserBO nodeUserBO = objectMapper.readValue(udpCommonDataDTO.getContent(), NodeUserBO.class);
        this.doHandleHeartBeat(nodeUserBO, udpCommonDataDTO.getFromAddress());
    }

    private void doHandleHeartBeat(NodeUserBO nodeUserBO, InetAddress fromAddress) {
        if (nodeUserBO.getId() == null || nodeUserBO.getUsername() == null) {
            return;
        }
        NodeBO nodeBO = new NodeBO();
        nodeBO.setAddress(fromAddress);
        LocalDateTime now = LocalDateTime.now();
        nodeBO.setLastHeartTime(now);
        nodeBO.setUser(nodeUserBO);
        nodeBO.setIsMySelf(false);
        boolean added = nodeManager.addNode(nodeBO);
        if (added) {
            notificationManager.addNotification(new NotificationVO(NotificationTypeEnum.NEW_USER_JOINED, now, nodeUserBO.getUsername()));
        }
    }

    private void handleOfflineNotification(ChannelHandlerContext ctx, UdpCommonDataDTO udpCommonDataDTO) {
        InetAddress fromAddress = udpCommonDataDTO.getFromAddress();
        NodeBO removed = nodeManager.removeNode(fromAddress);
        notificationManager.addNotification(new NotificationVO(NotificationTypeEnum.USER_LEFT, LocalDateTime.now(), removed.getUser().getUsername()));
    }

    private void handleChatMessage(ChannelHandlerContext ctx, UdpCommonDataDTO udpCommonDataDTO) throws JsonProcessingException {
        ChatMessageBO chatMessageBO = objectMapper.readValue(udpCommonDataDTO.getContent(), ChatMessageBO.class);
        NodeUserBO user = chatMessageBO.getUser();
        this.doHandleHeartBeat(user, udpCommonDataDTO.getFromAddress());
        chatMessageManager.addChatMessage(new ChatMessageVO(user.getId(), user.getUsername(), chatMessageBO.getMessage(), chatMessageBO.getSendTime()));
    }
}
