package org.zh.chatter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;

@Data
@AllArgsConstructor
public class CommonDataDTO {
    private Integer type;
    private InetAddress address;
    private String content;
}
