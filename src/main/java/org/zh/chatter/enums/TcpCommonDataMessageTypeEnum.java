package org.zh.chatter.enums;

import lombok.Getter;
import org.zh.chatter.cmd.TcpCommonCmdHandler;
import org.zh.chatter.cmd.impl.*;
import org.zh.chatter.model.bo.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum TcpCommonDataMessageTypeEnum {
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
    FILE_CHUNK_FETCH_ACKNOWLEDGE_RESPONSE((byte) 0x5, FileChunkFetchAcknowledgeResponseBO.class, FileChunkFetchAcknowledgeResponseCmdHandler.class);

    private final byte code;
    private final Class<? extends Serializable> payloadClass;
    private final Class<? extends TcpCommonCmdHandler> handlerClass;

    TcpCommonDataMessageTypeEnum(byte code, Class<? extends Serializable> payloadClass, Class<? extends TcpCommonCmdHandler> handlerClass) {
        this.code = code;
        this.payloadClass = payloadClass;
        this.handlerClass = handlerClass;
    }

    private static final Map<Byte, TcpCommonDataMessageTypeEnum> MAP;

    static {
        MAP = new HashMap<>();
        for (TcpCommonDataMessageTypeEnum type : TcpCommonDataMessageTypeEnum.values()) {
            MAP.put(type.getCode(), type);
        }
    }

    public static TcpCommonDataMessageTypeEnum getByCode(byte code) {
        return MAP.get(code);
    }
}
