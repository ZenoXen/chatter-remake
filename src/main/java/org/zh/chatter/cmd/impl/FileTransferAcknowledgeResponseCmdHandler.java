package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.LockManager;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.bo.FileTransferAcknowledgeResponseBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

@Component
public class FileTransferAcknowledgeResponseCmdHandler implements TcpCommonCmdHandler {

    @Resource
    private FileTaskManager fileTaskManager;
    @Resource
    private LockManager lockManager;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) throws Exception {
        FileTransferAcknowledgeResponseBO fileTransferAcknowledgeResponseBO = (FileTransferAcknowledgeResponseBO) payload;
        lockManager.runWithLock(dataDTO.getSessionId(), () -> {
            FileTaskBO task = fileTaskManager.getTask(dataDTO.getSessionId());
            //如果任务状态不是PENDING，不更新
            if (task == null || !FileTaskStatusEnum.PENDING.equals(task.getStatus())) {
                return;
            }
            if (fileTransferAcknowledgeResponseBO.isAccept()) {
                task.setStatus(FileTaskStatusEnum.TRANSFERRING);
            } else {
                task.setStatus(FileTaskStatusEnum.REJECTED);
            }
            fileTaskManager.addOrUpdateTask(task);
        });
    }
}
