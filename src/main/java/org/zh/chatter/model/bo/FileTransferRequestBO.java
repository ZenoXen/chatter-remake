package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileTransferRequestBO implements Serializable {
    private long fileSize;
    private String filename;
}
