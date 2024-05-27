package org.zh.chatter.config;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NetworkConfig {
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final int MAXIMUM_FRAME_LENGTH = 1024 * 1024;
    private static final int LENGTH_FIELD_OFFSET = 74;
    private static final int LENGTH_FIELD_LENGTH = 2;
    private static final int LENGTH_FIELD_ADJUSTMENT = 0;

    @Bean
    public LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder() {
        return new LengthFieldBasedFrameDecoder(MAXIMUM_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_FIELD_ADJUSTMENT, INITIAL_BYTES_TO_STRIP);
    }
}
