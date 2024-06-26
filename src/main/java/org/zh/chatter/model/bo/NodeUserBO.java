package org.zh.chatter.model.bo;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NodeUserBO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1943639115009854874L;
    private String id;
    private String username;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    private static final String DEFAULT_USERNAME_PREFIX = "用户";

    /**
     * 随机生成一个用户
     *
     * @return
     */
    public static NodeUserBO generateOne() {
        NodeUserBO nodeUserBO = new NodeUserBO();
        nodeUserBO.setId(UUID.fastUUID().toString(true));
        String username = generateRandomUsername();
        nodeUserBO.setUsername(username);
        nodeUserBO.setJoinTime(LocalDateTime.now());
        return nodeUserBO;
    }

    private static String generateRandomUsername() {
        return DEFAULT_USERNAME_PREFIX + RandomUtil.randomString(5);
    }
}
