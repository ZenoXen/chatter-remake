package org.zh.chatter.enums;

import lombok.Getter;

@Getter
public enum NotificationTypeEnum {
    NEW_USER_JOINED("用户加入群聊"),
    USER_LEFT("用户离开群聊"),
    NETWORK_ERROR("发生网络错误");

    private final String desc;

    NotificationTypeEnum(String desc) {
        this.desc = desc;
    }
}
