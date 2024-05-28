package org.zh.chatter.manager;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.vo.FileTaskCellVO;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class FileTaskManager {
    private static final Set<FileTaskStatusEnum> ON_GOING_STATUSES = Set.of(FileTaskStatusEnum.TRANSFERRING, FileTaskStatusEnum.SUSPENDED);
    private Map<String, FileTaskBO> map;
    @Getter
    private ObservableList<FileTaskCellVO> inactiveTasks;
    @Getter
    private ObservableList<FileTaskCellVO> ongoingTasks;

    public FileTaskManager() {
        this.map = new LinkedHashMap<>();
        this.inactiveTasks = FXCollections.observableArrayList();
        this.ongoingTasks = FXCollections.observableArrayList();
    }

    public void addOrUpdateTask(FileTaskBO fileTaskBO) {
        String taskId = fileTaskBO.getTaskId();
        map.put(taskId, fileTaskBO);
        FileTaskCellVO cellVO = this.convertFileTaskBO(fileTaskBO);
        this.handleFileTaskStatus(cellVO);
    }

    private FileTaskCellVO convertFileTaskBO(FileTaskBO fileTaskBO) {
        return FileTaskCellVO.builder()
                .fileName(new SimpleStringProperty(fileTaskBO.getFileName()))
                .fileSize(new SimpleLongProperty(fileTaskBO.getFileSize()))
                .senderId(new SimpleStringProperty(fileTaskBO.getSenderId()))
                .senderName(new SimpleStringProperty(fileTaskBO.getSenderName()))
                .sendTime(new SimpleObjectProperty<>(fileTaskBO.getSendTime()))
                .status(new SimpleObjectProperty<>(fileTaskBO.getStatus()))
                .transferredSize(new SimpleLongProperty(fileTaskBO.getTransferredSize()))
                .transferProgress(new SimpleDoubleProperty(fileTaskBO.getTransferProgress()))
                .taskId(new SimpleStringProperty(fileTaskBO.getTaskId())).build();
    }

    private void handleFileTaskStatus(FileTaskCellVO taskVO) {
        Boolean isOngoingNow = ON_GOING_STATUSES.contains(taskVO.getStatus().get());
        this.removeTaskFromList(taskVO);
        this.addTaskToList(taskVO, isOngoingNow);
    }

    private void removeTaskFromList(FileTaskCellVO taskVO) {
        inactiveTasks.remove(taskVO);
        ongoingTasks.remove(taskVO);
    }

    private void addTaskToList(FileTaskCellVO taskVO, Boolean isOngoingNow) {
        if (isOngoingNow) {
            ongoingTasks.add(taskVO);
        } else {
            inactiveTasks.add(taskVO);
        }
    }

    public FileTaskBO getTask(String taskId) {
        return map.get(taskId);
    }
}
