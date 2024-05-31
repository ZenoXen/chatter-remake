package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileChunkFetchAcknowledgeResponseBO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7034176754617562549L;
    private int chunkNo;
    private long chunkSize;
    private long receivedFileSize;
}
