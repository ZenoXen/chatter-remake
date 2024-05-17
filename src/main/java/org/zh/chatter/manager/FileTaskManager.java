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

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class FileTaskManager {
    private static final Set<FileTaskStatusEnum> ON_GOING_STATUSES = Set.of(FileTaskStatusEnum.TRANSFERRING, FileTaskStatusEnum.SUSPENDED);
    private Map<String, FileTaskBO> map;
    @Getter
    private ObservableList<FileTaskBO> inactiveTasks;
    @Getter
    private ObservableList<FileTaskBO> ongoingTasks;

    public FileTaskManager() {
        this.map = new LinkedHashMap<>();
        this.inactiveTasks = FXCollections.observableArrayList();
        this.ongoingTasks = FXCollections.observableArrayList();
        //todo 去除测试数据
        this.addOrUpdateTask(FileTaskBO.builder().fileName(new SimpleStringProperty("123")).fileSize(new SimpleLongProperty(1000)).senderName(new SimpleStringProperty("123")).senderId(new SimpleStringProperty("123")).transferredSize(new SimpleLongProperty(500)).transferProgress(new SimpleDoubleProperty(0.5)).sendTime(new SimpleObjectProperty<>(LocalDateTime.now())).status(new SimpleObjectProperty<>(FileTaskStatusEnum.PENDING)).taskId(new SimpleStringProperty("123")).build());
        this.addOrUpdateTask(FileTaskBO.builder().fileName(new SimpleStringProperty("123")).fileSize(new SimpleLongProperty(1000)).senderName(new SimpleStringProperty("123")).senderId(new SimpleStringProperty("123")).transferredSize(new SimpleLongProperty(500)).transferProgress(new SimpleDoubleProperty(0.5)).sendTime(new SimpleObjectProperty<>(LocalDateTime.now())).status(new SimpleObjectProperty<>(FileTaskStatusEnum.TRANSFERRING)).taskId(new SimpleStringProperty("123")).build());
    }

    public void addOrUpdateTask(FileTaskBO taskBO) {
        String taskId = taskBO.getTaskId().get();
        map.put(taskId, taskBO);
        this.handleFileTaskStatus(taskBO);
    }

    private void handleFileTaskStatus(FileTaskBO taskBO) {
        Boolean isOngoingNow = ON_GOING_STATUSES.contains(taskBO.getStatus().get());
        this.removeTaskFromList(taskBO);
        this.addTaskToList(taskBO, isOngoingNow);
    }

    private void removeTaskFromList(FileTaskBO taskBO) {
        inactiveTasks.remove(taskBO);
        ongoingTasks.remove(taskBO);
    }

    private void addTaskToList(FileTaskBO taskBO, Boolean isOngoingNow) {
        if (isOngoingNow) {
            ongoingTasks.add(taskBO);
        } else {
            inactiveTasks.add(taskBO);
        }
    }

    public FileTaskBO getTask(String taskId) {
        return map.get(taskId);
    }

    public void removeAllTask() {
        map.clear();
    }
}
