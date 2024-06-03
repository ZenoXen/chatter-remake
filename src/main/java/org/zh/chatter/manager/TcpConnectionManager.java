package org.zh.chatter.manager;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public Set<Map.Entry<InetAddress, Channel>> getAllEntries() {
        return addressChannelMap.entrySet();
    }

    public void addOrUpdateChannel(InetAddress address, Channel channel) {
        addressChannelMap.put(address, channel);
    }

    public void removeAndCloseChannel(InetAddress inetAddress) {
        Channel channel = addressChannelMap.remove(inetAddress);
        if (channel != null && channel.isOpen()) {
            log.debug("关闭channel：{}", inetAddress);
            channel.close();
        }
    }
}
