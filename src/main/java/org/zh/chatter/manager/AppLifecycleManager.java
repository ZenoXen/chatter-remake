package org.zh.chatter.manager;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.bo.NodeBO;
import org.zh.chatter.model.bo.NodeUserBO;
import org.zh.chatter.network.TcpClient;
import org.zh.chatter.network.TcpServer;
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
    @Resource
    private TcpServer tcpServer;
    @Resource
    private TcpClient tcpClient;
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
        //启动tcp服务
        new Thread(tcpServer).start();
    }

    @Override
    public void destroy() throws Exception {
        //发送一次离线通知
        udpServer.sendOfflineNotification();
        //停止udp服务
        udpServer.stopListening();
        //停止tcp服务
        tcpServer.stopListening();
        //关闭tcp客户端
        tcpClient.shutdown();
    }

    public boolean isDisconnected() {
        return isDisconnected.get();
    }

    public void setIsDisconnected(boolean val) {
        isDisconnected.set(val);
    }
}
