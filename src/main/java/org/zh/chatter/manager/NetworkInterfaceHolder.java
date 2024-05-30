package org.zh.chatter.manager;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zh.chatter.model.bo.MulticastAddressBO;
import org.zh.chatter.util.NetworkUtil;

import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;

@Component
@Getter
public class NetworkInterfaceHolder {
    private final Collection<NetworkInterface> allNetworkInterfaces;
    private NetworkInterface selectedNetworkInterface;
    private MulticastAddressBO selectedLocalAddress;
    private final InetSocketAddress multicastAddress;

    public NetworkInterfaceHolder(@Value("${app.port.udp}") Integer port,
                                  @Value("${app.address.multicast}") String multicastAddress) throws SocketException {
        this.allNetworkInterfaces = NetworkUtil.getAllSelectableNetworkInterfaces();
        NetworkInterface defaultNetworkInterface = this.allNetworkInterfaces.stream().findFirst().orElse(null);
        this.saveNetworkInterfaceReference(defaultNetworkInterface);
        this.multicastAddress = new InetSocketAddress(multicastAddress, port);
    }

    public synchronized void saveNetworkInterfaceReference(NetworkInterface networkInterface) {
        if (networkInterface == null) {
            return;
        }
        this.selectedNetworkInterface = networkInterface;
        this.selectedLocalAddress = networkInterface.getInterfaceAddresses().stream().map(InterfaceAddress::getAddress).findAny().map(a -> new MulticastAddressBO(networkInterface, a)).orElse(null);
    }
}
