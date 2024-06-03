package org.zh.chatter.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.vo.FileTaskCellVO;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class FileTaskManager {
    private final Map<String, FileTaskBO> map;
    @Getter
    private final ObservableList<FileTaskCellVO> inactiveTasks;
    @Getter
    private final ObservableList<FileTaskCellVO> ongoingTasks;

    public FileTaskManager() {
        this.map = new LinkedHashMap<>();
        this.inactiveTasks = FXCollections.observableArrayList();
        this.ongoingTasks = FXCollections.observableArrayList();
    }

    public void addOrUpdateTask(FileTaskBO fileTaskBO) {
        String taskId = fileTaskBO.getTaskId();
        boolean firstTimeAdded = this.isFirstTimeAdded(fileTaskBO.getTaskId());
        boolean taskFinished = this.isListChanged(fileTaskBO);
        map.put(taskId, fileTaskBO);
        FileTaskCellVO cellVO = FileTaskCellVO.convertFromFileTaskBO(fileTaskBO);
        if (firstTimeAdded) {
            ongoingTasks.add(cellVO);
        } else if (taskFinished) {
            ongoingTasks.remove(cellVO);
            inactiveTasks.add(cellVO);
        }
        log.info("添加/更新文件任务：id = {} status = {}", taskId, fileTaskBO.getStatus());
    }

    private boolean isFirstTimeAdded(String taskId) {
        return !map.containsKey(taskId);
    }

    private boolean isListChanged(FileTaskBO fileTaskBO) {
        FileTaskBO existing = map.get(fileTaskBO.getTaskId());
        if (existing == null) {
            return false;
        }
        return FileTaskStatusEnum.FINISHED_FILE_TASK_STATUSES.contains(fileTaskBO.getStatus());
    }

    public FileTaskBO getTask(String taskId) {
        return map.get(taskId);
    }
}
