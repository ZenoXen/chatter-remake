package org.zh.chatter.model.vo;

import javafx.beans.property.*;
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
}
