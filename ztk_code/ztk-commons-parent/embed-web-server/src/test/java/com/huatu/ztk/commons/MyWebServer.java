package com.huatu.ztk.commons;

import com.huatu.ztk.commons.web.WebServer;

/**
 * Created by shaojieyue on 4/14/16.
 */
public class MyWebServer {
    public static void main(String[] args) throws Exception {
        WebServer webServer = new WebServer("localhost",8089,"/","http://192.168.100.19:2379");
        webServer.addResourceHandler("/home/shaojieyue/tools/workspace/ztk-commons-parent/embed-web-server/src/test/webapp");
        webServer.setMinThreads(10)
                .setMaxThreads(100)
                .start();

    }
}
