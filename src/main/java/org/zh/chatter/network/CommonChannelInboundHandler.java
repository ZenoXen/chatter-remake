package org.zh.chatter.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.CommonDataTypeEnum;
import org.zh.chatter.manager.NodeManager;
import org.zh.chatter.model.bo.NodeBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.model.dto.CommonDataDTO;

import java.time.LocalDateTime;

@Component
@Slf4j
public class CommonChannelInboundHandler extends SimpleChannelInboundHandler<CommonDataDTO> {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private NodeManager nodeManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CommonDataDTO commonDataDTO) throws Exception {
        CommonDataTypeEnum typeEnum = CommonDataTypeEnum.getByCode(commonDataDTO.getType());
        switch (typeEnum) {
            case HEARTBEAT -> this.handleHeartbeat(ctx, commonDataDTO);
            case CHAT_MESSAGE -> this.handleChatMessage(ctx, commonDataDTO);
            default -> log.error("接收到未知类型的udp消息：{}", commonDataDTO);
        }
    }

    private void handleHeartbeat(ChannelHandlerContext ctx, CommonDataDTO commonDataDTO) throws JsonProcessingException {
        NodeUserBO nodeUserBO = objectMapper.readValue(commonDataDTO.getContent(), NodeUserBO.class);
        if (nodeUserBO.getId() == null || nodeUserBO.getUsername() == null) {
            return;
        }
        NodeBO nodeBO = new NodeBO();
        nodeBO.setAddress(commonDataDTO.getAddress());
        nodeBO.setLastHeartTime(LocalDateTime.now());
        nodeBO.setUser(nodeUserBO);
        nodeManager.addNode(nodeBO);
    }

    private void handleChatMessage(ChannelHandlerContext ctx, CommonDataDTO commonDataDTO) {
        //todo 处理聊天消息
    }
}
