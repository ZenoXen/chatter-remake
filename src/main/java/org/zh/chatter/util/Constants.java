package org.zh.chatter.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class Constants {
    public static final byte PROTOCOL_VERSION = (byte) 1;
    public static final long CHUNK_FETCH_SIZE = 1024 * 20;

    public static final int INITIAL_BYTES_TO_STRIP = 0;
    public static final int MAXIMUM_FRAME_LENGTH = 1024 * 1024;
    public static final int LENGTH_FIELD_OFFSET = 74;
    public static final int LENGTH_FIELD_LENGTH = 8;
    public static final int LENGTH_FIELD_ADJUSTMENT = 0;
    public static final String SELECT_FILE_TITLE = "选择要发送的文件";
    public static final String USER_VO = "userVO";
    public static final String IS_CLIENT_TAB = "isClientTab";
    public static final String CHANNEL = "channel";
    public static final String TAB_ID = "tabId";
    public static final String SESSION_ID = "sessionId";
    public static final KeyCodeCombination SEND_MESSAGE_SHORTCUT = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
    public static final int FILE_TRANSFER_DIALOG_WIDTH = 300;
    public static final int FILE_TRANSFER_DIALOG_HEIGHT = 400;
}
