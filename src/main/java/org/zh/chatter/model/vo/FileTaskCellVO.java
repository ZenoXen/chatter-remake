package org.zh.chatter.model.vo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zh.chatter.enums.FileTaskStatusEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTaskCellVO {
    private SimpleStringProperty taskId;
    private SimpleStringProperty fileName;
    private SimpleLongProperty fileSize;
    private SimpleLongProperty transferredSize;
    private SimpleDoubleProperty transferProgress;
    private SimpleStringProperty senderId;
    private SimpleStringProperty senderName;
    private SimpleObjectProperty<LocalDateTime> sendTime;
    private SimpleObjectProperty<FileTaskStatusEnum> status;
}
