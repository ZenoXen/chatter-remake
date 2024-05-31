package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileTransferAcknowledgeResponseBO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2362028390451055734L;
    private long acknowledgeTimestamp;
    private boolean isAccept;
}
