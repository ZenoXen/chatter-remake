package org.zh.chatter.model.bo;

import lombok.Data;

@Data
public class FileChunkFetchResponseBO {
    private byte[] fileChunkChecksum;
    private byte[] chunkData;
}
