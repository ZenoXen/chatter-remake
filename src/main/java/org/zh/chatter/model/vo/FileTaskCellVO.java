package org.zh.chatter.model.vo;

import cn.hutool.core.date.LocalDateTimeUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.model.bo.FileTaskBO;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTaskCellVO {
    private Boolean isMySelf;
    private SimpleStringProperty taskId;
    private SimpleStringProperty fileName;
    private SimpleLongProperty fileSize;
    private SimpleLongProperty transferredSize;
    private SimpleDoubleProperty transferProgress;
    private SimpleStringProperty senderId;
    private SimpleStringProperty senderName;
    private SimpleObjectProperty<LocalDateTime> sendTime;
    private SimpleObjectProperty<FileTaskStatusEnum> status;

    public static FileTaskCellVO convertFromFileTaskBO(FileTaskBO fileTaskBO) {
        return FileTaskCellVO.builder()
                .fileName(new SimpleStringProperty(fileTaskBO.getFileName()))
                .fileSize(new SimpleLongProperty(fileTaskBO.getFileSize()))
                .senderId(new SimpleStringProperty(fileTaskBO.getSenderId()))
                .senderName(new SimpleStringProperty(fileTaskBO.getSenderName()))
                .sendTime(new SimpleObjectProperty<>(LocalDateTimeUtil.of(fileTaskBO.getSendTime())))
                .status(new SimpleObjectProperty<>(fileTaskBO.getStatus()))
                .transferredSize(new SimpleLongProperty(fileTaskBO.getTransferredSize()))
                .transferProgress(new SimpleDoubleProperty(fileTaskBO.getTransferProgress()))
                .taskId(new SimpleStringProperty(fileTaskBO.getTaskId())).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileTaskCellVO cellVO = (FileTaskCellVO) o;
        return taskId.getValue().equals(cellVO.taskId.getValue());
    }

    @Override
    public int hashCode() {
        return taskId.getValue().hashCode();
    }
}
