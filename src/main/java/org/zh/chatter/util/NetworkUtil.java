package org.zh.chatter.util;

import cn.hutool.core.net.NetUtil;

import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

public class NetworkUtil {

    public static boolean isLocalAddress(InetAddress address) throws SocketException {
        // 判断是否是回环地址（loopback address）
        if (address.isLoopbackAddress()) {
            return true;
        }
        // 获取所有网络接口
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
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

    public static Collection<NetworkInterface> getAllSelectableNetworkInterfaces() throws SocketException {
        Collection<NetworkInterface> allInterfaces = NetUtil.getNetworkInterfaces();
        Collection<NetworkInterface> result = new ArrayList<>();
        for (NetworkInterface networkInterface : allInterfaces) {
            if (isNormalNetworkInterface(networkInterface)) {
                result.add(networkInterface);
            }
        }
        return result;
    }

    private static boolean isNormalNetworkInterface(NetworkInterface networkInterface) throws SocketException {
        return !networkInterface.isLoopback() && !networkInterface.isVirtual() && networkInterface.getInetAddresses().hasMoreElements();
    }

    public static boolean isFromSelectedNetworkInterface(SocketAddress address, NetworkInterface networkInterface) throws SocketException {
        DatagramSocket ds = new DatagramSocket();
        ds.connect(address);
        InetAddress localAddress = ds.getLocalAddress();
        NetworkInterface localNetworkInterface = NetworkInterface.getByInetAddress(localAddress);
        ds.disconnect();
        ds.close();
        return networkInterface.equals(localNetworkInterface);
    }
}
