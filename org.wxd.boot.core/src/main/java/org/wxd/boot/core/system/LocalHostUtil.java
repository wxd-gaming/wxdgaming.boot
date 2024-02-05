package org.wxd.boot.core.system;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class LocalHostUtil {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        InetAddress ip;
        System.out.println("get LocalHost Address : " + getLocalHostAddress());
        // 正确的IP拿法
        System.out.println("get LocalHost LAN Address : " + getLocalHostLANAddress());
        System.out.println("get all ip : " + getAllIp());
    }

    public static String AllIpString = null;

    public static String getAllIp() {
        if (AllIpString == null) {
            try {
                ArrayList<String> ipList = new ArrayList<>();
                // 遍历所有的网络接口
                for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                    NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                    // 在所有的接口下再遍历IP
                    String tmpIp = "";
                    for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                        InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                        if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                            if (tmpIp.length() > 1) {
                                tmpIp += ", ";
                            } else {
                                tmpIp += "  ";
                            }
                            tmpIp += inetAddr.getHostAddress();
                        }
                    }
                    if (tmpIp.length() > 2) {
                        ipList.add(tmpIp);
                    }
                }
                if (!ipList.isEmpty()) {
                    AllIpString = "{" + String.join(",", ipList) + "}";
                } else {
                    // 如果没有发现 non-loopback地址.只能用最次选的方案
                    InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
                    if (jdkSuppliedAddress == null) {
                        AllIpString = "The JDK InetAddress.getLocalHost() method unexpectedly returned null.";
                    }
                    AllIpString = jdkSuppliedAddress.getHostAddress();
                }
            } catch (Exception e) {
                AllIpString = "Failed to determine LAN address: ";
            }
        }
        return AllIpString;
    }

    /** 正确的IP拿法，即优先拿site-local地址 */
    public static String getLocalHostLANAddress() {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr.getHostAddress();
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress.getHostAddress();
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                return "未知";
            }
            return jdkSuppliedAddress.getHostAddress();
        } catch (Exception e) {
            return "未知";
        }
    }

    /**
     * 出自这篇：http://www.cnblogs.com/zrui-xyu/p/5039551.html
     * 实际上的代码是不准的
     *
     * @return
     * @throws UnknownHostException
     */
    public static String getLocalHostAddress() {
        try {
            Enumeration allNetInterfaces;
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {
                        if (ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }

            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                return "未知";
            }
            return jdkSuppliedAddress.getHostAddress();
        } catch (Exception e) {
            return "未知";
        }
    }

}
