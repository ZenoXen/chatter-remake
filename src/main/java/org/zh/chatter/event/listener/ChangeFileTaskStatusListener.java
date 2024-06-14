package org.zh.chatter.event.listener;

import jakarta.annotation.Resource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.zh.chatter.enums.FileTaskStatusEnum;
import org.zh.chatter.event.ChannelClosedEvent;
import org.zh.chatter.manager.FileTaskManager;
import org.zh.chatter.model.bo.FileTaskBO;

import java.util.List;

@Component
public class ChangeFileTaskStatusListener {

    @Resource
    private FileTaskManager fileTaskManager;

    @EventListener
    public void changeFileTaskStatus(ChannelClosedEvent event) {
        String channelId = event.getChannel().id().asLongText();
        List<FileTaskBO> tasks = fileTaskManager.getTasksByChannelId(channelId);
        //如果连接提前关闭，则将所有传输中的任务标为失败
        tasks.stream().filter(t -> FileTaskStatusEnum.TRANSFERRING.equals(t.getStatus())).forEach(t -> {
            t.setStatus(FileTaskStatusEnum.FAILED);
            fileTaskManager.addOrUpdateTask(t);
        });
    }
}
