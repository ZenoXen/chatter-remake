package org.zh.chatter.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;
import java.net.NetworkInterface;

@Data
@AllArgsConstructor
public class BroadcastAddressBO {
    private NetworkInterface networkInterface;
    private InetAddress address;
}
