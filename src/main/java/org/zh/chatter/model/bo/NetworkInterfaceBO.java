package org.zh.chatter.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.NetworkInterface;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetworkInterfaceBO {
    private String displayName;
    private NetworkInterface networkInterface;

    public NetworkInterfaceBO(NetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
        this.displayName = networkInterface.getDisplayName();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
