package org.zh.chatter.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.controller.ChatAreaController;
import org.zh.chatter.enums.CommonDataTypeEnum;
import org.zh.chatter.manager.NodeManager;
import org.zh.chatter.model.bo.ChatMessageBO;
import org.zh.chatter.model.bo.NodeBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.dto.UdpCommonDataDTO;

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
    private ChatAreaController chatAreaController;

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
        nodeBO.setLastHeartTime(LocalDateTime.now());
        nodeBO.setUser(nodeUserBO);
        nodeBO.setIsMySelf(false);
        nodeManager.addNode(nodeBO);
    }

    private void handleOfflineNotification(ChannelHandlerContext ctx, UdpCommonDataDTO udpCommonDataDTO) {
        InetAddress fromAddress = udpCommonDataDTO.getFromAddress();
        nodeManager.removeNode(fromAddress);
    }

    private void handleChatMessage(ChannelHandlerContext ctx, UdpCommonDataDTO udpCommonDataDTO) throws JsonProcessingException {
        ChatMessageBO chatMessageBO = objectMapper.readValue(udpCommonDataDTO.getContent(), ChatMessageBO.class);
        NodeUserBO user = chatMessageBO.getUser();
        this.doHandleHeartBeat(user, udpCommonDataDTO.getFromAddress());
        chatAreaController.showChatMessage(chatMessageBO.getMessage(), user.getId(), user.getUsername());
    }
}
