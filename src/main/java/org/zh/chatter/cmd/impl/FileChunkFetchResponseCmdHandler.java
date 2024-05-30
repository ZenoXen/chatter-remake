package org.zh.chatter.cmd.impl;

import cn.hutool.crypto.digest.MD5;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.model.bo.FileChunkFetchAcknowledgeResponseBO;
import org.zh.chatter.model.bo.FileChunkFetchRequestBO;
import org.zh.chatter.model.bo.FileChunkFetchResponseBO;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Arrays;

@Component
@Slf4j
public class FileChunkFetchResponseCmdHandler implements TcpCommonCmdHandler {

    @Resource
    private MD5 md5;
    @Resource
    private FileTaskManager fileTaskManager;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;

    private static final int MAX_CHUNK_RETRY_TIMES = 3;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) throws Exception {
        FileChunkFetchResponseBO fileChunkFetchResponseBO = (FileChunkFetchResponseBO) payload;
        String sessionId = dataDTO.getSessionId();
        FileTaskBO task = fileTaskManager.getTask(sessionId);
        if (task == null) {
            return;
        }
        //校验和对比
        byte[] chunkData = fileChunkFetchResponseBO.getChunkData();
        byte[] checkSum = md5.digest(chunkData);
        int chunkSize = chunkData.length;
        //如果校验和不一致，重试请求这一个文件块
        boolean needRetry = !Arrays.equals(fileChunkFetchResponseBO.getFileChunkChecksum(), checkSum);
        RandomAccessFile targetFile = task.getTargetFile();
        if (targetFile == null) {
            targetFile = new RandomAccessFile(task.getTargetFilePath(), "w");
        }
        task.setTargetFile(targetFile);
        //如果需要重试，则再次请求当前文件快（如果重试次数达到上限，任务结束），否则请求下一个文件块
        if (needRetry) {
            if (task.getChunkRetryTimes() >= MAX_CHUNK_RETRY_TIMES) {
                task.setStatus(FileTaskStatusEnum.FAILED);
            } else {
                task.setChunkRetryTimes(task.getChunkRetryTimes() + 1);
                log.warn("文件 {} 块号 {} 校验和不一致，当前重试次数 {}", task.getFileName(), task.getCurrentChunkNo(), task.getChunkRetryTimes());
            }
        } else {
            targetFile.write(chunkData);
            long transferredSize = task.getTransferredSize() + chunkSize;
            task.setTransferredSize(transferredSize);
            task.setTransferProgress(transferredSize / (double) task.getFileSize());
            this.sendChunkAcknowledgeResponse(ctx, task, chunkSize);
        }
        //如果文件数据全部传输完毕，更新任务状态完结，否则继续获取下一个文件块
        if (task.getTransferredSize() >= task.getFileSize()) {
            task.setStatus(FileTaskStatusEnum.COMPLETED);
        } else {
            task.setCurrentChunkNo(task.getCurrentChunkNo() + 1);
            this.requestChunk(ctx, task);
        }
        fileTaskManager.addOrUpdateTask(task);
    }

    private void requestChunk(ChannelHandlerContext ctx, FileTaskBO task) {
        FileChunkFetchRequestBO fileChunkFetchRequestBO = new FileChunkFetchRequestBO();
        fileChunkFetchRequestBO.setTimestamp(System.currentTimeMillis());
        ctx.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_CHUNK_FETCH_REQUEST, task.getTaskId(), currentUserInfoHolder.getCurrentUser().getId(), fileChunkFetchRequestBO));
    }

    private void sendChunkAcknowledgeResponse(ChannelHandlerContext ctx, FileTaskBO task, long chunkSize) {
        FileChunkFetchAcknowledgeResponseBO fileChunkFetchAcknowledgeResponseBO = new FileChunkFetchAcknowledgeResponseBO();
        fileChunkFetchAcknowledgeResponseBO.setChunkNo(task.getCurrentChunkNo());
        fileChunkFetchAcknowledgeResponseBO.setChunkSize(chunkSize);
        fileChunkFetchAcknowledgeResponseBO.setReceivedFileSize(task.getTransferredSize());
        ctx.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_CHUNK_FETCH_ACKNOWLEDGE_RESPONSE, task.getTaskId(), currentUserInfoHolder.getCurrentUser().getId(), fileChunkFetchAcknowledgeResponseBO));
    }
}
