package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileChunkFetchAcknowledgeResponseBO implements Serializable {
    private int chunkNo;
    private long chunkSize;
    private long receivedFileSize;
}
