package com.huatu.ztk.report;

import com.huatu.ztk.commons.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue
 * Created time 2016-05-31 18:44
 */
public class ReportWebServer {
    private static final Logger logger = LoggerFactory.getLogger(ReportWebServer.class);

    public static void main(String[] args) throws Exception {
        String serverAddress = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        final String contentPath = "/r";
        logger.info("start report webserver at http://{}:{}{}",serverAddress,port,contentPath);

//        WebServer webServer = new WebServer(serverAddress,port, contentPath);
        WebServer webServer = new WebServer(serverAddress,port, contentPath, "http://etcd01:2379,http://etcd02:2379,http://etcd03:2379");
        webServer.setMinThreads(20)
                .setMaxThreads(200)
                .start();
    }
}
