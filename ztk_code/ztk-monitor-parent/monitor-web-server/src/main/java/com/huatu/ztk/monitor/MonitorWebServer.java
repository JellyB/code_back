package com.huatu.ztk.monitor;

import com.huatu.ztk.commons.web.WebServer;

/**
 * Created by shaojieyue
 * Created time 2017-01-10 09:09
 */
public class MonitorWebServer {
    public static void main(String[] args) throws Exception{
        String serverAddress = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        WebServer webServer = new WebServer(serverAddress,port);
        webServer.setMinThreads(20)
                .setMaxThreads(200)
                .start();
    }
}
