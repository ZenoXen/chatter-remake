package org.zh.chatter.cmd.impl;

import cn.hutool.crypto.digest.MD5;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.LockManager;
import org.zh.chatter.model.bo.FileChunkFetchResponseBO;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;
import org.zh.chatter.util.Constants;

import java.io.RandomAccessFile;
import java.io.Serializable;

@Component
public class FileChunkFetchRequestCmdHandler implements TcpCommonCmdHandler {

    @Resource
    private FileTaskManager fileTaskManager;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Resource
    private MD5 md5;
    @Resource
    private LockManager lockManager;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) throws Exception {
        //接收文件块的发送请求
        String sessionId = dataDTO.getSessionId();
        lockManager.runWithLock(sessionId, () -> {
            try {
                this.responseFileChunk(ctx, sessionId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void responseFileChunk(ChannelHandlerContext ctx, String sessionId) throws Exception {
        FileTaskBO task = fileTaskManager.getTask(sessionId);
        //文件任务不存在，则跳过
        if (task == null) {
            return;
        }
        RandomAccessFile randomAccessFile = task.getSourceFile();
        if (randomAccessFile == null) {
            randomAccessFile = new RandomAccessFile(task.getSourceFilePath(), "r");
            task.setSourceFile(randomAccessFile);
            fileTaskManager.addOrUpdateTask(task);
        }
        int chunkSize = (int) Math.min(Constants.CHUNK_FETCH_SIZE, task.getFileSize() - task.getTransferredSize());
        byte[] chunkData = new byte[chunkSize];
        randomAccessFile.read(chunkData);
        FileChunkFetchResponseBO fileChunkFetchResponseBO = new FileChunkFetchResponseBO();
        fileChunkFetchResponseBO.setFileChunkChecksum(md5.digest(chunkData));
        fileChunkFetchResponseBO.setChunkData(chunkData);
        //返回文件块，但暂时不更新文件传输进度，等待FileChunkFetchAcknowledgeResponse后再更新进度
        ctx.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_CHUNK_FETCH_RESPONSE, sessionId, currentUserInfoHolder.getCurrentUser().getId(), fileChunkFetchResponseBO));
    }

    public static void main(String[] args) {
        SimpleStringProperty simpleStringProperty = new SimpleStringProperty("1233");
        System.out.println(simpleStringProperty.equals(new SimpleStringProperty("1233")));
    }
}
