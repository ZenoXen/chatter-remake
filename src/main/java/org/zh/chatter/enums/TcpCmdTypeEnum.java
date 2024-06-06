package org.zh.chatter.enums;

import lombok.Getter;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.cmd.impl.*;
import org.zh.chatter.model.bo.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum TcpCmdTypeEnum {
    /**
     * 文件传输请求
     */
    FILE_TRANSFER_REQUEST((byte) 0x1, FileTransferRequestBO.class, FileTransferRequestCmdHandler.class),
    /**
     * 文件传输确认响应
     */
    FILE_TRANSFER_ACKNOWLEDGE_RESPONSE((byte) 0x2, FileTransferAcknowledgeResponseBO.class, FileTransferAcknowledgeResponseCmdHandler.class),
    /**
     * 文件块获取请求
     */
    FILE_CHUNK_FETCH_REQUEST((byte) 0x3, FileChunkFetchRequestBO.class, FileChunkFetchRequestCmdHandler.class),
    /**
     * 文件块响应
     */
    FILE_CHUNK_FETCH_RESPONSE((byte) 0x4, FileChunkFetchResponseBO.class, FileChunkFetchResponseCmdHandler.class),
    /**
     * 文件块获取确认响应
     */
    FILE_CHUNK_FETCH_ACKNOWLEDGE_RESPONSE((byte) 0x5, FileChunkFetchAcknowledgeResponseBO.class, FileChunkFetchAcknowledgeResponseCmdHandler.class),
    /**
     * 文件传输状态变更通知
     */
    FILE_TRANSFER_STATUS_CHANGED_NOTIFICATION((byte) 0x6, FileTransferStatusChangedNotificationBO.class, FileTransferStatusChangedNotificationCmdHandler.class),
    /**
     * 私聊信交换请求
     */
    REMOTE_PRIVATE_CHAT_USER_INFO_EXCHANGE_REQUEST((byte) 0x7, RemotePrivateChatUserInfoExchangeBO.class, RemotePrivateChatUserInfoExchangeRequestCmdHandler.class),
    /**
     * 私聊信交换响应
     */
    REMOTE_PRIVATE_CHAT_USER_INFO_EXCHANGE_RESPONSE((byte) 0x8, RemotePrivateChatUserInfoExchangeBO.class, RemotePrivateChatUserInfoExchangeResponseCmdHandler.class);

    private final byte code;
    private final Class<? extends Serializable> payloadClass;
    private final Class<? extends TcpCommonCmdHandler> handlerClass;

    TcpCmdTypeEnum(byte code, Class<? extends Serializable> payloadClass, Class<? extends TcpCommonCmdHandler> handlerClass) {
        this.code = code;
        this.payloadClass = payloadClass;
        this.handlerClass = handlerClass;
    }

    private static final Map<Byte, TcpCmdTypeEnum> MAP;

    static {
        MAP = new HashMap<>();
        for (TcpCmdTypeEnum type : TcpCmdTypeEnum.values()) {
            MAP.put(type.getCode(), type);
        }
    }

    public static TcpCmdTypeEnum getByCode(byte code) {
        return MAP.get(code);
    }
}
