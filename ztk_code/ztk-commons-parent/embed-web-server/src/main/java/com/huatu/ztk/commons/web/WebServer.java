package com.huatu.ztk.commons.web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 简易的内嵌jetty的web服务
 * 这个服务基于传统的webapp
 * Created by shaojieyue on 4/11/16.
 */
public class WebServer {
    private static final Logger logger = Log.getLogger(WebServer.class);
    /**
     * 默认上下文地址
     */
    public static final String DEFAULT_CONTENT_PATH = "/";

    private int minThreads = 20;//最小线程池
    private int maxThreads = 200;//最大线程池
    private String serverIp;//服务ip
    private int serverPort;//服务端口
    private String contentPath;//服务上下文
    private String etcdConnectString;
    private List<Handler> handlerList = new ArrayList();
    public WebServer(String serverIp, int serverPort, String contentPath,String etcdConnectString) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.contentPath = contentPath;
        this.etcdConnectString = etcdConnectString;
    }

    public WebServer(String serverIp, int serverPort, String contentPath) {
        this(serverIp, serverPort,contentPath,null);
    }

    public WebServer(String serverIp, int serverPort) {
        this(serverIp, serverPort,DEFAULT_CONTENT_PATH);
    }

    /**
     * web服务启动类
     * @throws Exception
     */
    public void start() throws Exception {
        if (!contentPath.startsWith("/")) {
            contentPath = "/"+contentPath;
        }
        //防止出现两个//
        contentPath = contentPath.replaceAll("//","/");

        logger.info("startting server at: serverIp={},serverPort={},contentPath={},minThreads={},maxThreads={}",serverIp,serverPort,contentPath,minThreads,maxThreads);
        run(serverIp,serverPort,contentPath);
    }

    /**
     * 添加静态资源路径
     * @param path
     */
    public synchronized void addResourceHandler(String path){
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setResourceBase(path);
        resourceHandler.setEtags(true);
        /* the server uri path */
        ContextHandler ctx = new ContextHandler(contentPath);
        ctx.setHandler(resourceHandler);
        handlerList.add(ctx);
    }

    private void run(String serverAddress, int serverPort,String contentPath) throws Exception {
        String webappDir = getWebappPath();
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(serverAddress, serverPort);
        final Server server = new Server(inetSocketAddress);
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(minThreads);
        threadPool.setMaxThreads(maxThreads);
        //设置线程池
        server.setAttribute("threadpool",threadPool);
        WebAppContext webapp = new WebAppContext(webappDir,contentPath);
        webapp.setMaxFormContentSize(2048000);
        handlerList.add(webapp);
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(handlerList.stream().toArray(Handler[]::new));
        GzipHandler gzip = new GzipHandler();
        gzip.setHandler(handlers);
        server.setHandler(gzip);
        server.setStopAtShutdown(true);
        if (etcdConnectString != null) {//添加注册监听
            server.addLifeCycleListener(new EtcdRegisterListener(serverAddress,serverPort,etcdConnectString));
        }
        server.start();
        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
        server.join();
    }

    private String getWebappPath() {
        //开发环境webapp相对路径
        File webappDir = null;
        final String pdir = System.getProperty("webapp.dir");
        if (notBlank(pdir)) {
            logger.info("find webapp.dir="+pdir);
            webappDir = new File(pdir.trim());
        }

        //获取server路径,参数server_home由服务启动时指定
        final String serverResources = System.getProperty("server_resources");
        //serverResources 非空
        if (notBlank(serverResources)) {//优先级最高
            logger.info("find server_resources="+serverResources);
            webappDir = new File(serverResources.trim()+"/webapp");
        }
        if (webappDir == null) {
            throw new RuntimeException("not found webapp path. please check system property 'webapp.dir' or 'server_home'");
        }
        String webappPath = webappDir.getAbsolutePath();
        logger.info("use webapp: "+webappPath);
        //webapp目录没有找到
        if (!webappDir.exists()) {
            throw new IllegalArgumentException("webapp dir not exist. dir="+webappPath);
        }
        return webappPath;
    }

    private boolean notBlank(String str) {
        return str!=null && !"".equals(str.trim());
    }

    public int getMinThreads() {
        return minThreads;
    }

    public WebServer setMinThreads(int minThreads) {
        this.minThreads = minThreads;
        return this;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public WebServer setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }
}
