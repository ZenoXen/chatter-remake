package org.zh.chatter.scheduled;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.NotificationTypeEnum;
import org.zh.chatter.manager.AppLifecycleManager;
import org.zh.chatter.manager.NodeManager;
import org.zh.chatter.manager.NotificationManager;
import org.zh.chatter.model.bo.NodeBO;
import org.zh.chatter.model.vo.NotificationVO;
import org.zh.chatter.network.UdpServer;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class HeartbeatScheduled {

    @Resource
    private UdpServer udpServer;

    @Resource
    private NodeManager nodeManager;

    @Resource
    private AppLifecycleManager appLifecycleManager;

    @Resource
    private NotificationManager notificationManager;

    private final AtomicInteger sendHeartbeatFailedCount = new AtomicInteger(0);

    private final AtomicBoolean canSendHeartbeat = new AtomicBoolean(true);

    private static final Integer HEARTBEAT_FAIL_UPPER_LIMIT = 6;
    private static final Integer HEARTBEAT_DISCONNECT_COUNT = 3;
    private static final long NODE_OFFLINE_DURATION = 1;

    /**
     * 每10秒钟发送一次心跳信息
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void scheduledSendHeartbeat() {
        if (!canSendHeartbeat.get()) {
            return;
        }
        try {
            udpServer.sendHeartbeat();
            sendHeartbeatFailedCount.set(0);
        } catch (Exception e) {
            sendHeartbeatFailedCount.addAndGet(1);
            log.error("定时发送心跳信息失败：", e);
        } finally {
            //如果心跳发送失败次数超过最高次数限制，停止发送心跳
            //如果心跳发送失败次数超过离线标准，将聊天状态标为已离线
            if (sendHeartbeatFailedCount.get() >= HEARTBEAT_DISCONNECT_COUNT) {
                canSendHeartbeat.set(false);
            } else if (sendHeartbeatFailedCount.get() >= HEARTBEAT_FAIL_UPPER_LIMIT) {
                appLifecycleManager.setIsDisconnected(true);
            }
        }
    }

    /**
     * 每30秒轮询一遍所有节点的信息，将超过一定时间没有心跳的节点删除
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void scheduledNodeStatusCheck() {
        Collection<NodeBO> nodes = nodeManager.getAllNodes();
        LocalDateTime offlineMinTime = LocalDateTime.now().minusMinutes(NODE_OFFLINE_DURATION);
        nodes.stream().filter(n -> n.getLastHeartTime().isBefore(offlineMinTime) && !n.getIsMySelf()).forEach(n -> {
            NodeBO removed = nodeManager.removeNode(n.getAddress());
            notificationManager.addNotification(new NotificationVO(NotificationTypeEnum.USER_LEFT, LocalDateTime.now(), removed.getUser().getUsername()));
        });
    }
}
