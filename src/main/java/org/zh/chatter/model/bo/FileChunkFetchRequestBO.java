package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileChunkFetchRequestBO implements Serializable {
    private long timestamp;
}
