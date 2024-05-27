package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileTransferAcknowledgeResponseBO implements Serializable {
    private long acknowledgeTimestamp;
    private boolean isAccept;
}
