package org.zh.chatter.scheduled;

import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zh.chatter.manager.TcpConnectionManager;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class TcpConnectionScheduled {

    @Resource
    private TcpConnectionManager tcpConnectionManager;

    /**
     * 每3分钟轮询空闲的tcp连接并关闭
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void scheduledCloseIdleTcpConnection() {
        Set<Map.Entry<InetAddress, Channel>> entries = tcpConnectionManager.getAllEntries();
        Set<InetAddress> toRemoveSet = new HashSet<>();
        //从channels过滤出空闲的记录，并逐个关闭
        entries.forEach(entry -> {
            if (entry.getValue() == null) {
                log.debug("channel不存在，关闭空闲连接：" + entry);
                toRemoveSet.add(entry.getKey());
                return;
            }
            //如果使用该channel的引用数量为0，将这些channel关闭
            if (tcpConnectionManager.getReferenceCount(entry.getValue()) <= 0) {
                log.debug("channel引用数量归0，关闭空闲连接：" + entry);
                toRemoveSet.add(entry.getKey());
            }
        });
        toRemoveSet.forEach(tcpConnectionManager::removeAndCloseChannel);
    }
}
