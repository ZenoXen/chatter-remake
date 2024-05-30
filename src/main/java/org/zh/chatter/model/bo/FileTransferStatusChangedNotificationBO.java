package org.zh.chatter.model.bo;

import lombok.Data;
import org.zh.chatter.enums.FileTaskStatusEnum;

import java.io.Serializable;

@Data
public class FileTransferStatusChangedNotificationBO implements Serializable {
    private FileTaskStatusEnum targetStatus;
}
