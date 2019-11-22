package com.huatu.ztk.backend;

import com.huatu.ztk.commons.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue
 * Created time 2016-09-11 10:11
 */
public class DataToolWebServer {
    private static final Logger logger = LoggerFactory.getLogger(DataToolWebServer.class);

    public static void main(String[] args) throws Exception {
        String serverAddress = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        WebServer webServer = new WebServer(serverAddress,port,"/data");
        webServer.setMinThreads(20)
                .setMaxThreads(200)
                .start();
    }
}
