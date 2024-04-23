package org.zh.chatter.scheduled;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zh.chatter.network.UdpServer;

@Component
@Slf4j
public class HeartbeatScheduled {

    @Resource
    private UdpServer udpServer;

    @Scheduled(cron = "0/10 * * * * ?")
    public void scheduledSendHeartbeat() {
        try {
            udpServer.sendHeartbeat();
        } catch (Exception e) {
            log.error("定时发送心跳信息失败：", e);
        }
    }
}
