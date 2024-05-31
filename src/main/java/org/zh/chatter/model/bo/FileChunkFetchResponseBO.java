package org.zh.chatter.model.bo;

import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileChunkFetchResponseBO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8827715923935975500L;
    private byte[] fileChunkChecksum;
    @ToString.Exclude
    private byte[] chunkData;
}
