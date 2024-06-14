package org.zh.chatter.event;

import io.netty.channel.Channel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

@Getter
public class ChannelClosedEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = 1786631567789853807L;

    private final Channel channel;

    public ChannelClosedEvent(Object source, Channel channel) {
        super(source);
        this.channel = channel;
    }
}
