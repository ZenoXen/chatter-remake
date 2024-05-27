package org.zh.chatter.model.bo;

import lombok.Data;

@Data
public class FileTransferAcknowledgeResponseBO {
    private long acknowledgeTimestamp;
    private boolean isAccept;
}
