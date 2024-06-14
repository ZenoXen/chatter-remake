package org.zh.chatter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UdpCommonDataDTO {
    private Integer type;
    private String messageId;
    private InetAddress fromAddress;
    private InetAddress toAddress;
    private Integer port;
    private String content;
}
