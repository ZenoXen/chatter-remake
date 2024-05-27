package org.zh.chatter.model.bo;

import lombok.Data;

@Data
public class FileTransferRequestBO {
    private long fileSize;
    private String filename;
}
