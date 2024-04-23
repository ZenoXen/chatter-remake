package org.zh.chatter.manager;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.zh.chatter.network.UdpServer;

@Component
@Order
public class AppInitializer {
    public AppInitializer(UdpServer udpServer) {
        new Thread(udpServer).start();
    }
}
