package com.huatu.ztk.course;

import com.huatu.ztk.commons.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue
 * Created time 2016-11-22 11:28
 */
public class CourseWebServer {
    private static final Logger logger = LoggerFactory.getLogger(CourseWebServer.class);

    public static void main(String[] args) throws Exception {
        String serverAddress = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        WebServer webServer = new WebServer(serverAddress,port,"/c", "http://etcd01:2379,http://etcd02:2379,http://etcd03:2379");
        webServer.setMinThreads(20)
                .setMaxThreads(200)
                .start();
    }
}
