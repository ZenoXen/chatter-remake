package org.zh.chatter.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO {
    private String senderId;
    private String senderName;
    private String message;
    private LocalDateTime sendTime;
}
