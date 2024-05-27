package org.zh.chatter.model.bo;

import lombok.Data;

@Data
public class FileChunkFetchRequestBO {
    private int chunkNo;
    private short chunkSize;
}
