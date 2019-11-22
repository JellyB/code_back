package com.huatu.ztk.pc;

import com.huatu.ztk.commons.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue
 * Created time 2016-09-08 15:52
 */
public class PCWebServer {
    private static final Logger logger = LoggerFactory.getLogger(PCWebServer.class);

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        WebServer webServer = new WebServer(host, port, "/pc", "http://etcd01:2379,http://etcd02:2379,http://etcd03:2379");
        final String resoucesPath = System.getProperty("server_resources")+ "/webapp/resource";
        logger.info("add static resource={}",resoucesPath);
        webServer.addResourceHandler(resoucesPath);
        webServer.setMinThreads(20)
                .setMaxThreads(200)
                .start();
    }
}
