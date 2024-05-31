package org.zh.chatter.manager;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TcpConnectionManager {
    private final Map<InetAddress, Channel> addressChannelMap;

    public TcpConnectionManager() {
        addressChannelMap = new HashMap<>();
    }

    public Channel getChannel(InetAddress inetAddress) {
        return addressChannelMap.get(inetAddress);
    }

    public void addChannel(InetAddress address, Channel channel) {
        addressChannelMap.put(address, channel);
    }

    public void removeAndCloseChannel(InetAddress inetAddress) {
        Channel channel = addressChannelMap.remove(inetAddress);
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }
}
