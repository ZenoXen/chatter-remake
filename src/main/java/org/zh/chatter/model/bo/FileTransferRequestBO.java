package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileTransferRequestBO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8749083197803167964L;
    private long fileSize;
    private String filename;
}
