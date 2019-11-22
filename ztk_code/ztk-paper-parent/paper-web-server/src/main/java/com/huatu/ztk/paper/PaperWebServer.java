package com.huatu.ztk.paper;

import com.huatu.ztk.commons.web.WebServer;

/**
 * 试卷web服务
 * Created by shaojieyue
 * Created time 2016-04-23 15:19
 */
public class PaperWebServer {
    public static void main(String[] args) throws Exception {
        String serverAddress = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        WebServer webServer = new WebServer(serverAddress,port,"/p","http://etcd01:2379,http://etcd02:2379,http://etcd03:2379");
        webServer.setMinThreads(20)
                .setMaxThreads(500)
                .start();
    }
}
