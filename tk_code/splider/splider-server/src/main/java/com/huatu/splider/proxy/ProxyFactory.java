package com.huatu.splider.proxy;

/**
 * 一个简单的okhttp代理获取
 * @author hanchao
 * @date 2018/2/23 11:09
 */
public class ProxyFactory {
    public static class Proxy {
        private int type;//1 http 2 socks
        private String host;
        private int port;
        //private String username;
        //private String password;
    }
}
