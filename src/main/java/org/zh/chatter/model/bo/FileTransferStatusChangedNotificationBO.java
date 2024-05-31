package org.zh.chatter.model.bo;

import lombok.Data;
import org.zh.chatter.enums.FileTaskStatusEnum;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileTransferStatusChangedNotificationBO implements Serializable {
    @Serial
    private static final long serialVersionUID = -4619931766512727481L;
    private FileTaskStatusEnum targetStatus;
}
