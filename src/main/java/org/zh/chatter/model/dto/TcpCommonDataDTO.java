package org.zh.chatter.model.dto;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zh.chatter.enums.TcpCmdTypeEnum;
import org.zh.chatter.util.Constants;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TcpCommonDataDTO {
    private byte protocolVersion;
    private byte messageType;
    private String sessionId;
    private String userId;
    private long timestamp;
    private long payloadLength;
    private byte[] payload;

    public static <T extends Serializable> TcpCommonDataDTO encapsulate(TcpCmdTypeEnum cmdType, String sessionId, String userId, T data) {
        byte[] serialized = ObjectUtil.serialize(data);
        return TcpCommonDataDTO.builder().messageType(cmdType.getCode()).protocolVersion(Constants.PROTOCOL_VERSION)
                .sessionId(sessionId).userId(userId)
                .timestamp(System.currentTimeMillis()).payload(serialized).payloadLength(serialized.length).build();
    }
}
