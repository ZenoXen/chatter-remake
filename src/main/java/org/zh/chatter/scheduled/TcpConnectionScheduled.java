package org.zh.chatter.scheduled;

import cn.hutool.core.collection.CollectionUtil;
import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.manager.TcpConnectionManager;
import org.zh.chatter.model.bo.FileTaskBO;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TcpConnectionScheduled {

    @Resource
    private TcpConnectionManager tcpConnectionManager;
    @Resource
    private FileTaskManager fileTaskManager;

    /**
     * 每3分钟轮询空闲的tcp连接并关闭
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    public void scheduledCloseIdleTcpConnection() {
        Set<Map.Entry<InetAddress, Channel>> entries = tcpConnectionManager.getAllEntries();
        List<FileTaskBO> allTasks = fileTaskManager.getAllTasks();
        Map<String, List<FileTaskBO>> taskMap = allTasks.stream().filter(t -> t.getChannel() != null).collect(Collectors.groupingBy(t -> t.getChannel().id().asLongText()));
        Set<InetAddress> toRemoveSet = new HashSet<>();
        //从channels过滤出空闲的记录，并逐个关闭
        entries.forEach(entry -> {
            if (entry.getValue() == null) {
                toRemoveSet.add(entry.getKey());
                return;
            }
            List<FileTaskBO> tasks = taskMap.getOrDefault(entry.getValue().id().asLongText(), Collections.emptyList());
            List<FileTaskBO> unfinishedTasks = tasks.stream().filter(t -> !FileTaskStatusEnum.FINISHED_FILE_TASK_STATUSES.contains(t.getStatus())).toList();
            //如果使用该channel的任务都已完结，将这些channel关闭、移除
            if (CollectionUtil.isEmpty(unfinishedTasks)) {
                toRemoveSet.add(entry.getKey());
                tasks.forEach(t -> t.setChannel(null));
            }
        });
        toRemoveSet.forEach(tcpConnectionManager::removeAndCloseChannel);
    }
}
