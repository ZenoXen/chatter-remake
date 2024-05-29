package org.zh.chatter.enums;

import lombok.Getter;

@Getter
public enum FileTaskStatusEnum {
    PENDING("待定"),
    TRANSFERRING("传输中"),
    SUSPENDED("已暂停"),
    REJECTED("已拒绝"),
    FAILED("传输失败"),
    CANCELLED("已取消"),
    COMPLETED("已完成");
    private final String displayName;

    FileTaskStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}