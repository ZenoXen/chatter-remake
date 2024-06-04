package org.zh.chatter.component;

import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.manager.CurrentUserInfoHolder;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.LockManager;
import org.zh.chatter.model.bo.FileChunkFetchRequestBO;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.bo.FileTransferStatusChangedNotificationBO;
import org.zh.chatter.model.dto.TcpCommonDataDTO;
import org.zh.chatter.model.vo.FileTaskCellVO;
import org.zh.chatter.model.vo.UserVO;
import org.zh.chatter.network.TcpClient;

import java.io.File;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
public class FileTaskButtonActions {
    @Resource
    private FileTaskManager fileTaskManager;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Resource
    private LockManager lockManager;
    @Resource
    private TcpClient tcpClient;
    private static final String SELECT_FILE_TITLE = "选择要发送的文件";

    @Getter
    private BiFunction<FileTaskCellVO, Button, FileTaskCellVO> suspendButtonAction = (cellVO, button) -> {
        String taskId = cellVO.getTaskId().get();
        lockManager.runWithLock(taskId, () -> {
            FileTaskBO task = fileTaskManager.getTask(taskId);
            if (task != null && FileTaskStatusEnum.ON_GOING_STATUSES.contains(task.getStatus())) {
                FileTaskStatusEnum oppositeStatus = getOppositeStatus(task.getStatus());
                task.setStatus(oppositeStatus);
                fileTaskManager.addOrUpdateTask(task);
                Channel channel = task.getChannel();
                String currentUserId = currentUserInfoHolder.getCurrentUser().getId();
                FileTransferStatusChangedNotificationBO fileTransferStatusChangedNotificationBO = new FileTransferStatusChangedNotificationBO();
                fileTransferStatusChangedNotificationBO.setTargetStatus(oppositeStatus);
                channel.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_TRANSFER_STATUS_CHANGED_NOTIFICATION, taskId, currentUserId, fileTransferStatusChangedNotificationBO));
                if (FileTaskStatusEnum.TRANSFERRING.equals(oppositeStatus)) {
                    FileChunkFetchRequestBO fileChunkFetchRequestBO = new FileChunkFetchRequestBO();
                    fileChunkFetchRequestBO.setTimestamp(System.currentTimeMillis());
                    channel.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_CHUNK_FETCH_REQUEST, task.getTaskId(), currentUserInfoHolder.getCurrentUser().getId(), fileChunkFetchRequestBO));
                }
                button.setText(getStatusButtonText(oppositeStatus));
            }
        });
        return cellVO;
    };

    @Getter
    private Function<FileTaskCellVO, Boolean> suspendButtonShowAction = (cellVO) -> !FileTaskStatusEnum.ON_GOING_STATUSES.contains(cellVO.getStatus().get());

    @Getter
    private BiFunction<FileTaskCellVO, Button, FileTaskCellVO> cancelButtonAction = (cellVO, button) -> {
        String taskId = cellVO.getTaskId().get();
        lockManager.runWithLock(taskId, () -> {
            FileTaskBO task = fileTaskManager.getTask(taskId);
            if (task != null && FileTaskStatusEnum.ON_GOING_STATUSES.contains(task.getStatus())) {
                task.setStatus(FileTaskStatusEnum.CANCELLED);
                fileTaskManager.addOrUpdateTask(task);
                Channel channel = task.getChannel();
                String currentUserId = currentUserInfoHolder.getCurrentUser().getId();
                FileTransferStatusChangedNotificationBO fileTransferStatusChangedNotificationBO = new FileTransferStatusChangedNotificationBO();
                fileTransferStatusChangedNotificationBO.setTargetStatus(FileTaskStatusEnum.CANCELLED);
                channel.writeAndFlush(TcpCommonDataDTO.encapsulate(TcpCmdTypeEnum.FILE_TRANSFER_STATUS_CHANGED_NOTIFICATION, taskId, currentUserId, fileTransferStatusChangedNotificationBO));
            }
        });
        return cellVO;
    };

    @Getter
    private BiFunction<UserVO, Button, UserVO> sendFileButtonAction = (userVO, button) -> {
        //弹出windows文件选框
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(SELECT_FILE_TITLE);
        File chosenFile = fileChooser.showOpenDialog(button.getScene().getWindow());
        if (chosenFile != null) {
            tcpClient.sendFileTransferRequest(userVO, chosenFile);
        }
        return userVO;
    };

    private FileTaskStatusEnum getOppositeStatus(FileTaskStatusEnum status) {
        return FileTaskStatusEnum.TRANSFERRING.equals(status) ? FileTaskStatusEnum.SUSPENDED : FileTaskStatusEnum.TRANSFERRING;
    }

    private String getStatusButtonText(FileTaskStatusEnum status) {
        return FileTaskStatusEnum.TRANSFERRING.equals(status) ? "暂停" : "继续";
    }
}
