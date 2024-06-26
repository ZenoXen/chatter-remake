package org.zh.chatter.manager;

import jakarta.annotation.Resource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.model.bo.FileTaskBO;
import org.zh.chatter.model.vo.FileTaskCellVO;

import java.io.RandomAccessFile;
import java.util.*;

@Component
@Slf4j
public class FileTaskManager {
    private final Map<String, FileTaskBO> map;
    private final Map<String, FileTaskCellVO> cellMap;
    private final Map<String, List<FileTaskBO>> channelIdTaskMap;
    @Getter
    private final ObservableList<FileTaskCellVO> inactiveTasks;
    @Getter
    private final ObservableList<FileTaskCellVO> ongoingTasks;
    @Resource
    private TcpConnectionManager tcpConnectionManager;

    public FileTaskManager() {
        this.map = new LinkedHashMap<>();
        this.cellMap = new LinkedHashMap<>();
        this.channelIdTaskMap = new HashMap<>();
        this.inactiveTasks = FXCollections.observableArrayList();
        this.ongoingTasks = FXCollections.observableArrayList();
    }

    public void addOrUpdateTask(FileTaskBO fileTaskBO) {
        String taskId = fileTaskBO.getTaskId();
        boolean firstTimeAdded = this.isFirstTimeAdded(fileTaskBO.getTaskId());
        boolean taskFinished = this.isListChanged(fileTaskBO);
        map.put(taskId, fileTaskBO);
        FileTaskCellVO cellVO = FileTaskCellVO.convertFromFileTaskBO(fileTaskBO);
        Optional.ofNullable(cellMap.get(taskId)).ifPresent(c -> {
            c.setIsMySelf(cellVO.getIsMySelf());
            c.getStatus().setValue(cellVO.getStatus().getValue());
            c.getTransferredSize().setValue(cellVO.getTransferredSize().getValue());
            c.getTransferProgress().setValue(cellVO.getTransferProgress().getValue());
            c.getSenderName().setValue(cellVO.getSenderName().getValue());
            c.getStatus().setValue(cellVO.getStatus().getValue());
        });
        //第一次添加这个任务
        if (firstTimeAdded) {
            ongoingTasks.add(cellVO);
            cellMap.put(taskId, cellVO);
            String channelId = fileTaskBO.getChannel().id().asLongText();
            List<FileTaskBO> channelTasks = channelIdTaskMap.getOrDefault(channelId, new ArrayList<>());
            channelTasks.add(fileTaskBO);
            channelIdTaskMap.put(channelId, channelTasks);
        }
        //任务变成终结状态
        if (taskFinished) {
            ongoingTasks.remove(cellVO);
            inactiveTasks.add(cellVO);
            this.closeFile(fileTaskBO);
            if (cellVO.getIsMySelf()) {
                tcpConnectionManager.deductReferenceCount(fileTaskBO.getChannel());
            }
        }
        log.debug("添加/更新文件任务：id = {} status = {}", taskId, fileTaskBO.getStatus());
    }

    private boolean isFirstTimeAdded(String taskId) {
        return !map.containsKey(taskId);
    }

    private void closeFile(FileTaskBO fileTaskBO) {
        fileTaskBO.setSourceFilePath(null);
        fileTaskBO.setTargetFilePath(null);
        RandomAccessFile sourceFile = fileTaskBO.getSourceFile();
        RandomAccessFile targetFile = fileTaskBO.getTargetFile();
        try {
            if (sourceFile != null) {
                log.debug("关闭sourceFile：{}", fileTaskBO.getFileName());
                sourceFile.close();
                fileTaskBO.setSourceFile(null);
            }
            if (targetFile != null) {
                log.debug("关闭targetFile：{}", fileTaskBO.getFileName());
                targetFile.close();
                fileTaskBO.setTargetFile(null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public List<FileTaskBO> getTasksByChannelId(String channelId) {
        return channelIdTaskMap.getOrDefault(channelId, Collections.emptyList());
    }
}
