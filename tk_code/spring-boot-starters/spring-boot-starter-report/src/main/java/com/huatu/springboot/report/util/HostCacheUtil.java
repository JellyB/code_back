package com.huatu.springboot.report.util;

import com.huatu.common.utils.env.IpAddrUtil;

/**
 * @author hanchao
 * @date 2018/1/16 17:04
 */
public class HostCacheUtil {
    private volatile static String host = "";
    static {
        String prefer = System.getProperty("preferedNetworks","192.168.100");
        host = IpAddrUtil.getLocalIP(prefer.split(","));
    }

    public static String getHost(){
        return host;
    }
}
