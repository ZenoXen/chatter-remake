package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.LockManager;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.bo.FileTransferStatusChangedNotificationBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

@Component
public class FileTransferStatusChangedNotificationCmdHandler implements TcpCommonCmdHandler {
    @Resource
    private FileTaskManager fileTaskManager;
    @Resource
    private LockManager lockManager;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) throws Exception {
        FileTransferStatusChangedNotificationBO fileTransferStatusChangedNotificationBO = (FileTransferStatusChangedNotificationBO) payload;
        lockManager.runWithLock(dataDTO.getSessionId(), () -> {
            FileTaskBO task = fileTaskManager.getTask(dataDTO.getSessionId());
            if (task == null) {
                return;
            }
            //如果目标状态不是取消或者暂停，跳过不处理
            FileTaskStatusEnum targetStatus = fileTransferStatusChangedNotificationBO.getTargetStatus();
            if (!FileTaskStatusEnum.VALID_CHANGE_TARGET_STATUSES.contains(targetStatus)) {
                return;
            }
            task.setStatus(targetStatus);
            fileTaskManager.addOrUpdateTask(task);
        });
    }
}
