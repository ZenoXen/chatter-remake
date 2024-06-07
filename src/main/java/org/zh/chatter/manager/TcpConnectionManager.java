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
    private final Map<String, Integer> channelReferenceCountMap;

    public TcpConnectionManager() {
        addressChannelMap = new HashMap<>();
        channelReferenceCountMap = new HashMap<>();
    }

    public Channel getChannel(InetAddress inetAddress) {
        return addressChannelMap.get(inetAddress);
    }

    public void addReferenceCount(Channel channel) {
        channelReferenceCountMap.compute(channel.id().asLongText(), (k, v) -> v == null ? 1 : v + 1);
    }

    public void deductReferenceCount(Channel channel) {
        channelReferenceCountMap.compute(channel.id().asLongText(), (k, v) -> v == null ? 0 : v - 1);
    }

    public Integer getReferenceCount(Channel channel) {
        return channelReferenceCountMap.getOrDefault(channel.id().asLongText(), 0);
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
