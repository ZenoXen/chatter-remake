package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.bo.FileTransferStatusChangedNotificationBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

@Component
public class FileTransferStatusChangedNotificationCmdHandler implements TcpCommonCmdHandler {
    @Resource
    private FileTaskManager fileTaskManager;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) {
        FileTransferStatusChangedNotificationBO fileTransferStatusChangedNotificationBO = (FileTransferStatusChangedNotificationBO) payload;
        FileTaskBO task = fileTaskManager.getTask(dataDTO.getSessionId());
        //如果当前状态不是传输中或者暂停中，跳过不处理
        if (task == null || !FileTaskStatusEnum.ON_GOING_STATUSES.contains(task.getStatus())) {
            return;
        }
        //如果目标状态不是取消或者暂停，跳过不处理
        FileTaskStatusEnum targetStatus = fileTransferStatusChangedNotificationBO.getTargetStatus();
        if (!FileTaskStatusEnum.VALID_CHANGE_TARGET_STATUSES.contains(targetStatus)) {
            return;
        }
        task.setStatus(targetStatus);
        fileTaskManager.addOrUpdateTask(task);
    }
}
