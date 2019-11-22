package com.huatu.ztk.commons.web;

import com.huatu.ztk.RegisterServer;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 * etcd 注册监听器
 * Created by shaojieyue
 * Created time 2016-08-06 21:22
 */
public class EtcdRegisterListener  implements LifeCycle.Listener{
    private static final Logger logger = LoggerFactory.getLogger(EtcdRegisterListener.class);
    private RegisterServer registerServer;
    public EtcdRegisterListener(String host, int port, String etcdConnectString) {
        registerServer = new RegisterServer(host,port,System.getProperty("server_name"),etcdConnectString);
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
        logger.info("server lifeCycleStarting");
    }

    @Override
    public void lifeCycleStarted(LifeCycle event) {
        registerServer.register();
    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        logger.warn("proccess web server life cycle fail.",cause);
        System.exit(0);
    }

    @Override
    public void lifeCycleStopping(LifeCycle event) {
        registerServer.unregister();
    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
        logger.info("server lifeCycleStopped");
    }
}
