package org.zh.chatter.model.bo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
public class FileChunkFetchResponseBO implements Serializable {
    private byte[] fileChunkChecksum;
    @ToString.Exclude
    private byte[] chunkData;
}
