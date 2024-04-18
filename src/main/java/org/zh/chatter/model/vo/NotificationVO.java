package org.zh.chatter.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zh.chatter.enums.NotificationTypeEnum;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVO {
    private NotificationTypeEnum type;
    private LocalDateTime time;
    private String content;
}
