package org.zh.chatter.enums;

public interface TcpCommonDataMessageType {
    /**
     * 文件传输请求
     */
    byte FILE_TRANSFER_REQUEST = 0x1;
    /**
     * 文件传输确认响应
     */
    byte FILE_TRANSFER_ACKNOWLEDGE_RESPONSE = 0x2;
    /**
     * 文件块获取请求
     */
    byte FILE_CHUNK_FETCH_REQUEST = 0x3;
    /**
     * 文件块响应
     */
    byte FILE_CHUNK_FETCH_RESPONSE = 0x4;
    /**
     * 文件块获取确认响应
     */
    byte FILE_CHUNK_FETCH_ACKNOWLEDGE_RESPONSE = 0x5;
}
