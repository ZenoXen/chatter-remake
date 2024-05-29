package org.zh.chatter.cmd.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
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
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.bo.FileTransferAcknowledgeResponseBO;
import org.zh.chatter.model.bo.FileTransferRequestBO;
import org.zh.chatter.model.bo.NodeBO;
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

    @Override
    public void handle(ChannelHandlerContext ctx, TcpCommonDataDTO dataDTO, Serializable payload) {
        FileTransferRequestBO fileTransferRequestBO = (FileTransferRequestBO) payload;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(FILE_TRANSFER_REQUEST_TITLE);
        alert.setHeaderText(null);
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
        alert.setContentText(this.generateContentText(senderName, filename, fileSize));
        Optional<ButtonType> result = alert.showAndWait();
        long currentTimeMillis = System.currentTimeMillis();
        FileTransferAcknowledgeResponseBO responseBO = new FileTransferAcknowledgeResponseBO();
        responseBO.setAcknowledgeTimestamp(currentTimeMillis);
        boolean accept = result.filter(bt -> bt.equals(ButtonType.OK)).isPresent();
        boolean savePathSelected = false;
        //响应请求，并决定是否要保存文件任务
        if (accept) {
            //选择保存路径
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(SAVE_FILE_CHOOSER_TITLE);
            fileChooser.setInitialFileName(filename);
            File savePath = fileChooser.showSaveDialog(alert.getOwner());
            if (savePath != null) {
                savePathSelected = true;
                FileTaskBO fileTaskBO = FileTaskBO.builder().taskId(dataDTO.getSessionId()).fileName(filename).savePath(savePath)
                        .fileSize(fileSize).senderId(senderId).senderName(senderName).sendTime(LocalDateTimeUtil.of(dataDTO.getTimestamp()))
                        .status(FileTaskStatusEnum.PENDING).transferProgress(0D).transferredSize(0L).channel((NioSocketChannel) ctx.channel()).build();
                fileTaskManager.addOrUpdateTask(fileTaskBO);
            }
        }
        responseBO.setAccept(accept && savePathSelected);
        ctx.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_TRANSFER_ACKNOWLEDGE_RESPONSE, dataDTO.getSessionId(), currentUserInfoHolder.getCurrentUser().getId(), responseBO));
    }

    private String generateContentText(String username, String filename, long fileSize) {
        return String.format(CONTENT_TEXT_TEMPLATE, username, filename, FileUtil.readableFileSize(fileSize));
    }
}
