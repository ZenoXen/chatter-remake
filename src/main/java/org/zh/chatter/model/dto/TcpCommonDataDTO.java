package org.zh.chatter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TcpCommonDataDTO {
    private byte protocolVersion;
    private byte messageType;
    private String sessionId;
    private String userId;
    private long timestamp;
    private short payloadLength;
    private byte[] payload;
}
