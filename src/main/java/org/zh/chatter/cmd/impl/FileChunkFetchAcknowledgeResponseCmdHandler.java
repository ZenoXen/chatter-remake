package org.zh.chatter.cmd.impl;

import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.LockManager;
import org.zh.chatter.model.bo.FileChunkFetchAcknowledgeResponseBO;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.Serializable;

@Component
public class FileChunkFetchAcknowledgeResponseCmdHandler implements TcpCommonCmdHandler {
    @Resource
    private FileTaskManager fileTaskManager;
    @Autowired
    private LockManager lockManager;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) throws Exception {
        FileChunkFetchAcknowledgeResponseBO fileChunkFetchAcknowledgeResponseBO = (FileChunkFetchAcknowledgeResponseBO) payload;
        String sessionId = dataDTO.getSessionId();
        lockManager.runWithLock(sessionId, () -> {
            FileTaskBO task = fileTaskManager.getTask(sessionId);
            if (task == null) {
                return;
            }
            //确认文件块响应，更新进度，如果已全部发送完成，标记为任务已完成
            long receivedFileSize = fileChunkFetchAcknowledgeResponseBO.getReceivedFileSize();
            if (receivedFileSize > task.getTransferredSize()) {
                task.setTransferredSize(receivedFileSize);
                task.setTransferProgress((double) receivedFileSize / task.getFileSize());
            }
            if (task.getTransferredSize() >= task.getFileSize()) {
                task.setStatus(FileTaskStatusEnum.COMPLETED);
                ctx.close();
            }
            fileTaskManager.addOrUpdateTask(task);
        });
    }
}
