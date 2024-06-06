package org.zh.chatter.model.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RemotePrivateChatUserInfoExchangeBO implements Serializable {
    @Serial
    private static final long serialVersionUID = -989025529957912367L;
    private String id;
    private String username;
}
