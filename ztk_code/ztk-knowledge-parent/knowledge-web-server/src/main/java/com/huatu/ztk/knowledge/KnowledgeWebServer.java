package com.huatu.ztk.knowledge;


import com.huatu.ztk.commons.web.WebServer;

/**
 * Created by shaojieyue on 4/16/16.
 */
public class KnowledgeWebServer {
    public static void main(String[] args) throws Exception {
        String serverAddress = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        WebServer webServer = new WebServer(serverAddress,port,"/k","http://etcd01:2379,http://etcd02:2379,http://etcd03:2379");
        webServer.setMinThreads(20)
                .setMaxThreads(400)
                .start();
    }
}
