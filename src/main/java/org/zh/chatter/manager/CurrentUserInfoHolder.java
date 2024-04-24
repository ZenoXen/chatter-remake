package org.zh.chatter.manager;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.bo.NodeUserBO;

@Component
@Data
public class CurrentUserInfoHolder {
    private NodeUserBO currentUser;
}
