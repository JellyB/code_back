package com.huatu.ztk.question;

import com.huatu.ztk.commons.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by shaojieyue on 4/16/16.
 */
public class QuestionWebServer {
    public static final Logger logger = LoggerFactory.getLogger(QuestionWebServer.class);

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("server_ip");//server地址
        int port = Integer.valueOf(args[0]);//服务端口
        WebServer webServer = new WebServer(host, port, "/q", "http://etcd01:2379,http://etcd02:2379,http://etcd03:2379");
        webServer.setMinThreads(20)
                .setMaxThreads(400)
                .start();
    }
}
