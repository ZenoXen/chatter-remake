package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileChunkFetchResponseBO implements Serializable {
    private byte[] fileChunkChecksum;
    private byte[] chunkData;
}
