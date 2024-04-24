package org.zh.chatter.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeBO {
    private InetAddress address;
    private LocalDateTime lastHeartTime;
    private NodeUserBO user;
    private Boolean isMySelf;
}
