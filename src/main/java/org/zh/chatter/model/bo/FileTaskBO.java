package org.zh.chatter.model.bo;

import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zh.chatter.enums.FileTaskStatusEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTaskBO {
    private String taskId;
    private String fileName;
    private Long fileSize;
    private Long transferredSize;
    private Double transferProgress;
    private String senderId;
    private String senderName;
    private LocalDateTime sendTime;
    private FileTaskStatusEnum status;
    private NioSocketChannel channel;
    private Boolean isMySelf;
}
