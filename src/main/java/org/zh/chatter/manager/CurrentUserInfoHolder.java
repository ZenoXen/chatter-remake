package org.zh.chatter.manager;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.bo.NodeUserBO;

@Component
@Data
public class CurrentUserInfoHolder implements InitializingBean {
    private NodeUserBO currentUser;

    @Override
    public void afterPropertiesSet() throws Exception {
        //todo 需要可以修改当前用户昵称
        currentUser = NodeUserBO.generateOne();
        //todo 当前用户加入用户列表
    }
}
