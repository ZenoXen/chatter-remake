package org.zh.chatter.network;

import cn.hutool.core.util.ObjectUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.TcpCommonDataMessageTypeEnum;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

@Component
@Slf4j
@ChannelHandler.Sharable
public class TcpCommonChannelInboundHandler extends SimpleChannelInboundHandler<TcpCommonDataDTO> {
    @Resource
    private ApplicationContext applicationContext;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO) throws Exception {
        //根据messageType决定将payload解析成哪个类
        TcpCommonDataMessageTypeEnum type = TcpCommonDataMessageTypeEnum.getByCode(dataDTO.getMessageType());
        if (type == null) {
            log.warn("tcp消息解析失败，未知的messageType={}", dataDTO.getMessageType());
            return;
        }
        byte[] payload = dataDTO.getPayload();
        Serializable deserialized = ObjectUtil.deserialize(payload, type.getPayloadClass());
        TcpCommonCmdHandler handler = applicationContext.getBean(type.getHandlerClass());
        handler.handle(ctx, dataDTO, deserialized);
    }
}
