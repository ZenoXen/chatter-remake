package org.zh.chatter.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum UdpCommonDataTypeEnum {
    HEARTBEAT(1),
    CHAT_MESSAGE(2),
    OFFLINE_NOTIFICATION(3);
    private final int code;

    private static final Map<Integer, UdpCommonDataTypeEnum> CODE_MAP;

    static {
        CODE_MAP = Arrays.stream(UdpCommonDataTypeEnum.values()).collect(Collectors.toMap(UdpCommonDataTypeEnum::getCode, Function.identity()));
    }

    UdpCommonDataTypeEnum(int code) {
        this.code = code;
    }

    public static UdpCommonDataTypeEnum getByCode(int code) {
        return CODE_MAP.get(code);
    }
}
