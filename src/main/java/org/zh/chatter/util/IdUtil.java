package org.zh.chatter.util;

import cn.hutool.core.lang.UUID;

public class IdUtil {

    /**
     * 生成32位不带横杠的uuid
     *
     * @return
     */
    public static String genId() {
        return UUID.fastUUID().toString(true);
    }
}
