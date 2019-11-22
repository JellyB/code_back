package com.huatu.ztk;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 *
 * 服务注册工具
 * Created by shaojieyue
 * Created time 2016-10-20 19:33
 */
public class RegisterServer {
    private static final Logger logger = LoggerFactory.getLogger(RegisterServer.class);
    public static final String REGISTER_DATA= "{\"host\":\"%s\",\"weight\":%s,\"port\":%s}";
    public static final String ETCD_SERVERS_PREFIX = "/ztk-servers/";
    private String host;
    private int port;

    private EtcdClient etcdClient;
    private String etcdServerHome;//服务注册dir
    private String etcdServerNode;//服务注册路径

    public RegisterServer(String host, int port,String serverName, String etcdConnectString) {
        assert serverName != null;
        this.host = host;
        this.port = port;
        this.etcdServerHome = ETCD_SERVERS_PREFIX + serverName;
        this.etcdServerNode = etcdServerHome+"/" + host + ":" + port;
        final URI[] etcdServers = Stream.of(etcdConnectString.split(",")).map(etcdServer -> URI.create(etcdServer)).toArray(URI[]::new);
        //init etcd client
        etcdClient = new EtcdClient(etcdServers);
    }

    /**
     * 注册服务
     */
    public void register() {
        logger.info("start register server http(s)://{}:{} to {} success.",host,port,etcdServerNode);
        Throwable throwable = null;
        for (int i = 0; i < 3; i++) {//循环重试
            try {
                etcdClient.put(etcdServerNode, String.format(REGISTER_DATA,host,5,port))
                        .timeout(3, TimeUnit.SECONDS)
                        .send().get();
                logger.info("register to {} success.",etcdServerNode);
                return;
            } catch (IOException e) {
                throwable = e;
            } catch (EtcdException e) {
                throwable = e;
            } catch (EtcdAuthenticationException e) {
                logger.error("register fail.",e);
                return;
            } catch (TimeoutException e) {
                throwable = e;
            }
            sleep(1000);
        }

        //注册失败
        if (throwable != null) {
            logger.error("register fail.",throwable);
        }

    }

    /**
     * 下线服务
     */
    public void unregister() {
        logger.info("unregister the server from etcd. node={}",etcdServerNode);
        for (int i = 0; i < 3; i++) {
            try {
                etcdClient.delete(etcdServerNode).send().get();
                break;
            } catch (IOException e) {
                logger.warn("unregister fail.",e);
            } catch (EtcdException e) {
                logger.warn("unregister fail.",e);
            } catch (EtcdAuthenticationException e) {
                logger.warn("unregister fail.",e);
                break;
            } catch (TimeoutException e) {
                logger.warn("unregister fail.",e);
            }
            sleep(1000);
        }
        sleep(2000);
        logger.info("unregister the server from etcd success. node={}",etcdServerNode);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

}
