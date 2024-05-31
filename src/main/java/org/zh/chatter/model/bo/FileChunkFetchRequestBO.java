package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileChunkFetchRequestBO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1195506920939599705L;
    private long timestamp;
}
