package org.zh.chatter.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    /**
     * 在线
     */
    ONLINE,
    /**
     * 已下线，用户主动退出软件时，会标为此状态
     */
    OFFLINE,
    /**
     * 已掉线，在若干时间内，节点没有心跳，用户会被标记为此状态
     */
    DISCONNECTED;
}
