package com.huatu.ztk.search;

import com.huatu.ztk.commons.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue
 * Created time 2016-05-04 09:56
 */
public class SearchWebServer {

    private static final Logger logger = LoggerFactory.getLogger(SearchWebServer.class);

    public static void main(String[] args) throws Exception {
        String serverAddress = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        WebServer webServer = new WebServer(serverAddress,port,"/s", "http://etcd01:2379,http://etcd02:2379,http://etcd03:2379");
        webServer.setMinThreads(20)
                .setMaxThreads(200)
                .start();
    }
}
