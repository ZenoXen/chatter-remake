package org.zh.chatter.model.bo;

import lombok.Data;

@Data
public class FileChunkFetchAcknowledgeResponseBO {
    private int chunkNo;
    private short chunkSize;
    private long receivedFileSize;
}
