package org.zh.chatter.manager;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.bo.NodeBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.network.UdpServer;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Order
public class AppLifecycleManager implements InitializingBean, DisposableBean {
    @Resource
    private NodeManager nodeManager;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;
    @Resource
    private UdpServer udpServer;
    private final AtomicBoolean isDisconnected = new AtomicBoolean(false);

    @Override
    public void afterPropertiesSet() throws Exception {
        //生成本机用户，并加入节点列表
        NodeUserBO currentUser = NodeUserBO.generateOne();
        NodeBO nodeBO = new NodeBO(InetAddress.getLocalHost(), LocalDateTime.now(), currentUser, true);
        currentUserInfoHolder.setCurrentUser(currentUser);
        nodeManager.addNode(nodeBO);
        //启动udp服务
        new Thread(udpServer).start();
        //发送一次心跳信息
        udpServer.sendHeartbeat();
    }

    @Override
    public void destroy() throws Exception {
        //发送一次离线通知
        udpServer.sendOfflineNotification();
        //停止监听
        udpServer.stopListening();
    }

    public boolean isDisconnected() {
        return isDisconnected.get();
    }

    public void setIsDisconnected(boolean val) {
        isDisconnected.set(val);
    }
}