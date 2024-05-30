package org.zh.chatter.model.bo;

import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zh.chatter.enums.FileTaskStatusEnum;

import java.io.File;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTaskBO {
    private String taskId;
    private String fileName;
    private long fileSize;
    private long transferredSize;
    private double transferProgress;
    private int currentChunkNo;
    private int chunkRetryTimes;
    private String senderId;
    private String senderName;
    private long sendTime;
    private FileTaskStatusEnum status;
    private NioSocketChannel channel;
    private boolean isMySelf;

    /**
     * 发送方使用的字段
     */
    private File sourceFilePath;
    private RandomAccessFile sourceFile;

    /**
     * 接收方使用的字段
     */

    private File targetFilePath;
    private RandomAccessFile targetFile;
}
