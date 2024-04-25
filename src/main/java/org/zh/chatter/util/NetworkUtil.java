package org.zh.chatter.util;

import cn.hutool.core.net.NetUtil;
import org.zh.chatter.model.bo.BroadcastAddressBO;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtil {
    public static List<BroadcastAddressBO> getAllBroadcastAddresses() throws Exception {
        List<BroadcastAddressBO> result = new ArrayList<>();
        for (NetworkInterface networkInterface : NetUtil.getNetworkInterfaces()) {
            if (networkInterface.isLoopback()) {
                continue;
            }
            List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
            for (InterfaceAddress address : addresses) {
                InetAddress broadcast = address.getBroadcast();
                if (broadcast == null) {
                    continue;
                }
                result.add(new BroadcastAddressBO(networkInterface, broadcast));
            }
        }
        return result;
    }

    public static boolean isLocalAddress(InetAddress address) throws SocketException {

        // 判断是否是回环地址（loopback address）
        if (address.isLoopbackAddress()) {
            return true;
        }

        // 获取所有网络接口
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            // 排除回环接口和虚拟接口
            if (networkInterface.isLoopback() || networkInterface.isVirtual()) {
                continue;
            }

            // 判断地址是否属于该网络接口
            Enumeration<InetAddress> interfaceAddresses = networkInterface.getInetAddresses();
            while (interfaceAddresses.hasMoreElements()) {
                InetAddress inetAddress = interfaceAddresses.nextElement();
                if (address.equals(inetAddress)) {
                    return true; // 是本地地址
                }
            }
        }

        return false; // 不是本地地址
    }
}
