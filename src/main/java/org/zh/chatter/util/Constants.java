package org.zh.chatter.util;

public class Constants {
    public static final byte PROTOCOL_VERSION = (byte) 1;
    public static final long CHUNK_FETCH_SIZE = 1024 * 20;

    public static final int INITIAL_BYTES_TO_STRIP = 0;
    public static final int MAXIMUM_FRAME_LENGTH = 1024 * 1024;
    public static final int LENGTH_FIELD_OFFSET = 74;
    public static final int LENGTH_FIELD_LENGTH = 8;
    public static final int LENGTH_FIELD_ADJUSTMENT = 0;
    public static final String TAB_ID = "tabId";
    public static final String TARGET_USER_ID = "targetUserId";
    public static final String SELECT_FILE_TITLE = "选择要发送的文件";
    public static final String USER_VO = "userVO";
}
