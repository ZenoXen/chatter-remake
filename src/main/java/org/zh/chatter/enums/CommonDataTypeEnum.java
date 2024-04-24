package org.zh.chatter.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum CommonDataTypeEnum {
    HEARTBEAT(1),
    CHAT_MESSAGE(2),
    OFFLINE_NOTIFICATION(3);
    private final int code;

    private static final Map<Integer, CommonDataTypeEnum> CODE_MAP;

    static {
        CODE_MAP = Arrays.stream(CommonDataTypeEnum.values()).collect(Collectors.toMap(CommonDataTypeEnum::getCode, Function.identity()));
    }

    CommonDataTypeEnum(int code) {
        this.code = code;
    }

    public static CommonDataTypeEnum getByCode(int code) {
        return CODE_MAP.get(code);
    }
}
