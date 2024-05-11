package org.zh.chatter.manager;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.bo.BroadcastAddressBO;
import org.zh.chatter.util.NetworkUtil;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Getter
public class NetworkInterfaceHolder {
    private final Collection<NetworkInterface> allNetworkInterfaces;
    private NetworkInterface selectedNetworkInterface;
    private List<BroadcastAddressBO> broadcastAddressList;

    public NetworkInterfaceHolder() throws SocketException {
        this.broadcastAddressList = Collections.emptyList();
        this.allNetworkInterfaces = NetworkUtil.getAllSelectableNetworkInterfaces();
        NetworkInterface defaultNetworkInterface = this.allNetworkInterfaces.stream().findFirst().orElse(null);
        this.saveNetworkInterfaceReference(defaultNetworkInterface);
    }

    public void saveNetworkInterfaceReference(NetworkInterface networkInterface) {
        if (networkInterface == null) {
            return;
        }
        List<BroadcastAddressBO> broadcastAddressList = networkInterface.getInterfaceAddresses().stream()
                .filter(a -> a != null && a.getBroadcast() != null)
                .map(a -> new BroadcastAddressBO(networkInterface, a.getBroadcast()))
                .collect(Collectors.toList());
        this.selectedNetworkInterface = networkInterface;
        this.broadcastAddressList = broadcastAddressList;
    }
}
