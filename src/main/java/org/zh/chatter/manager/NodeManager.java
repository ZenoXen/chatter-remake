package org.zh.chatter.manager;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.bo.NodeBO;
import org.zh.chatter.model.vo.UserVO;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NodeManager {
    private final Map<InetAddress, NodeBO> nodeMap;
    private final Set<String> userIdSet;
    @Resource
    private CurrentUserInfoHolder currentUserInfoHolder;

    public NodeManager() {
        this.nodeMap = new LinkedHashMap<>();
        this.userIdSet = new HashSet<>();
    }

    public boolean addNode(NodeBO node) {
        InetAddress address = node.getAddress();
        String userId = node.getUser().getId();
        if (!this.isNodeOrUserExists(address, userId)) {
            nodeMap.put(address, node);
            userIdSet.add(userId);
            return true;
        }
        return false;
    }

    public NodeBO removeNode(InetAddress address) {
        NodeBO removed = nodeMap.remove(address);
        userIdSet.remove(removed.getUser().getId());
        return removed;
    }

    public Collection<NodeBO> getAllNodes() {
        return nodeMap.values();
    }

    public List<UserVO> getUserList() {
        return nodeMap.values().stream().map(n -> {
            UserVO userVO = new UserVO();
            userVO.setId(n.getUser().getId());
            userVO.setUsername(n.getUser().getUsername());
            userVO.setIsMySelf(n.getUser().getId().equals(currentUserInfoHolder.getCurrentUser().getId()));
            return userVO;
        }).collect(Collectors.toList());
    }

    private boolean isNodeOrUserExists(InetAddress address, String userId) {
        return nodeMap.containsKey(address) || userIdSet.contains(userId);
    }
}
