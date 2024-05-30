package org.zh.chatter.cmd.impl;

import cn.hutool.core.io.FileUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.Resource;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.NodeManager;
import org.zh.chatter.manager.StageHolder;
import org.zh.chatter.model.bo.*;
import org.zh.chatter.model.dto.TcpCommonDataDTO;

import java.io.File;
import java.io.Serializable;
import java.util.Optional;

@Component
public class FileTransferRequestCmdHandler implements TcpCommonCmdHandler {
    private static final String FILE_TRANSFER_REQUEST_TITLE = "接收文件";
    private static final String SAVE_FILE_CHOOSER_TITLE = "选择文件保存目录";
    private static final String CONTENT_TEXT_TEMPLATE = "用户：%s\n发送文件：%s\n大小：%s\n是否接收？";
    @Resource
    private NodeManager nodeManager;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Resource
    private FileTaskManager fileTaskManager;
    @Resource
    private StageHolder stageHolder;

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) {
        FileTransferRequestBO fileTransferRequestBO = (FileTransferRequestBO) payload;
        String filename = fileTransferRequestBO.getFilename();
        long fileSize = fileTransferRequestBO.getFileSize();
        String senderId = dataDTO.getUserId();
        //文件名为空，或者文件大小异常，不处理请求
        if (Strings.isEmpty(filename) || fileSize <= 0) {
            return;
        }
        NodeBO node = nodeManager.getNodeByUserId(senderId);
        //如果没有该用户的节点，不处理请求
        if (node == null) {
            return;
        }
        String senderName = node.getUser().getUsername();
        Optional<ButtonType> result = this.showAcceptFileConfirmation(senderName, filename, fileSize);
        boolean accept = result.filter(bt -> bt.equals(ButtonType.OK)).isPresent();
        long currentTimeMillis = System.currentTimeMillis();
        FileTransferAcknowledgeResponseBO responseBO = new FileTransferAcknowledgeResponseBO();
        responseBO.setAcknowledgeTimestamp(currentTimeMillis);
        //响应请求，并决定是否要保存文件任务
        String sessionId = dataDTO.getSessionId();
        boolean savePathSelected = false;
        if (accept) {
            //选择保存路径
            File savePath = this.showSavePathFileChooser(filename);
            if (savePath != null) {
                savePathSelected = true;
                FileTaskBO fileTaskBO = FileTaskBO.builder().taskId(sessionId).fileName(filename).targetFilePath(savePath)
                        .fileSize(fileSize).senderId(senderId).senderName(senderName).sendTime(dataDTO.getTimestamp())
                        .status(FileTaskStatusEnum.PENDING).currentChunkNo(0).chunkRetryTimes(0).transferProgress(0D).transferredSize(0L).channel((NioSocketChannel) ctx.channel())
                        .isMySelf(false).build();
                fileTaskManager.addOrUpdateTask(fileTaskBO);
            }
        }
        boolean fileAccepted = accept && savePathSelected;
        responseBO.setAccept(fileAccepted);
        String userId = currentUserInfoHolder.getCurrentUser().getId();
        ctx.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_TRANSFER_ACKNOWLEDGE_RESPONSE, sessionId, userId, responseBO));
        //请求第一个文件块
        if (fileAccepted) {
            this.sendFirstFileChunkRequest(ctx, sessionId, userId);
        }
    }

    private File showSavePathFileChooser(String filename) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(SAVE_FILE_CHOOSER_TITLE);
        fileChooser.setInitialFileName(filename);
        return fileChooser.showSaveDialog(stageHolder.getStage());
    }

    private Optional<ButtonType> showAcceptFileConfirmation(String senderName, String filename, long fileSize) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(FILE_TRANSFER_REQUEST_TITLE);
        alert.setHeaderText(null);
        alert.setContentText(this.generateContentText(senderName, filename, fileSize));
        return alert.showAndWait();
    }

    private void sendFirstFileChunkRequest(ChannelHandlerContext ctx, String sessionId, String userId) {
        FileChunkFetchRequestBO fileChunkFetchRequestBO = new FileChunkFetchRequestBO();
        fileChunkFetchRequestBO.setTimestamp(System.currentTimeMillis());
        ctx.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_CHUNK_FETCH_REQUEST, sessionId, userId, fileChunkFetchRequestBO));
    }

    private String generateContentText(String username, String filename, long fileSize) {
        return String.format(CONTENT_TEXT_TEMPLATE, username, filename, FileUtil.readableFileSize(fileSize));
    }
}
