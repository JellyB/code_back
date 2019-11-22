package com.huatu.springboot.webMessage.util;

import com.huatu.common.utils.env.IpAddrUtil;

/**
 * Created by junli on 2018/4/10.
 */
public class HostCacheUtil {

    private volatile static String host = "";

    static {
        String prefer = System.getProperty("preferedNetworks", "192.168.100");
        host = IpAddrUtil.getLocalIP(prefer.split(","));
    }

    public static String getHost() {
        return host;
    }
}
