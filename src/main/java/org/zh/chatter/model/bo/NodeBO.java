package org.zh.chatter.model.bo;

import lombok.Data;

import java.net.InetAddress;
import java.time.LocalDateTime;

@Data
public class NodeBO {
    private InetAddress address;
    private LocalDateTime lastHeartTime;
    private NodeUserBO user;
}
