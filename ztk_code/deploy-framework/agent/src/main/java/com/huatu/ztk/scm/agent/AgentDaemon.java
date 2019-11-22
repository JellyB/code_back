package com.huatu.ztk.scm.agent;


import com.google.common.base.Preconditions;
import com.huatu.ztk.scm.agent.streaming.WebSocketServer;
import com.huatu.ztk.scm.common.ZooAgentWatcher;
import com.huatu.ztk.scm.agent.resource.CommandResource;
import com.huatu.ztk.scm.common.IpUtil;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.ZookeeperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-7-16
 * Time: 下午3:04
 * To change this template use File | Settings | File Templates.
 */
public class AgentDaemon extends Service<AgentConfiguration> {
    protected final static Logger logger = LoggerFactory.getLogger(AgentDaemon.class);
    private final static ZooAgentWatcher watcher = new ZooAgentWatcher(ZookeeperUtil.getZkClient());

    public static void main(String... args) throws Exception {
        String ip = System.getProperty("server_ip");
        System.out.println("param ip" +ip);
        Preconditions.checkArgument(ip != null && !"".equals(ip.trim()), "the ip of agent boudle is valid. ip="+ip);
        try {
            boolean ret = IpUtil.containtIp(ip);
            System.out.println("ip rs "+ret);
            if (ret) {
                System.out.println("agent biddle the ip: " + ip);
                ZookeeperUtil.current_ip = ip;
            } else {
                System.err.println("the server instance param [server_ip=" + ip + "] is not in this server instance.");
                System.exit(1);
            }
        } catch (SocketException e) {
            logger.error("SocketException", e);
            System.exit(1);
        }
        try{
            new AgentDaemon().run(args);
        }catch (Exception e){
            logger.error("lunach server ex",e);
            System.exit(1);
        }
    }

    @Override
    public void initialize(Bootstrap<AgentConfiguration> bootstrap) {
        logger.info("exec initialize");
        bootstrap.addCommand(new RenderCommand());
        //monitor current java instance
        ZookeeperUtil.getZkClient().getConnectionStateListenable().addListener(new ConnectionStateListener() {

            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info("state changed : "+newState);
                if (newState == ConnectionState.RECONNECTED || newState == ConnectionState.CONNECTED) {
                    //重新监听节点
                    //状态监控
                    //ZookeeperUtil.dataSingleWatch(ZookeeperUtil.basePath + "/stat", StatLock.getStatWatcher());
                    //初始节点
                    ZookeeperUtil.childrenSingleWatch(ZookeeperUtil.basePath + "/init", watcher);
                    dataSingleWatch(ZookeeperUtil.basePath, watcher, true);
                }
            }
        });
        //监听、由于ZookeeperUtil已经start，不能触发stateChanged，所以在此手动监听
        ZookeeperUtil.childrenSingleWatch(ZookeeperUtil.basePath + "/init", watcher);
        dataSingleWatch(ZookeeperUtil.basePath, watcher, true);
    }

    public static void dataSingleWatch(String path, CuratorWatcher watcher,boolean recursiveSubNode) {
        //状态和初始化节点不监控
        logger.info("dataSingleWatch path --"+path);
        if(path.equals("/huatu-scm/init")||"/huatu-scm/stat".equals(path)) return;
        try {
            //只监控本机节点
            if(path.contains(ZookeeperUtil.current_ip)){
                ZookeeperUtil.dataSingleWatch(path,watcher);
            }
            if (recursiveSubNode&&ZookeeperUtil.getZkClient().checkExists().forPath(path)!=null) {
                List<String> nodes = ZookeeperUtil.getZkClient().getChildren().forPath(path);
                if(nodes!=null){
                    for (String node : nodes) {
                        String subPath= path + "/" + node;
                        dataSingleWatch(subPath, watcher,recursiveSubNode);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("exceprion",e);
        }
    }

    @Override
    public void run(AgentConfiguration agentConfiguration, Environment environment) throws Exception {
        environment.addResource(new CommandResource());
        new WebSocketServerThread().start();
    }
}

class WebSocketServerThread extends Thread {
    protected final static Logger logger = LoggerFactory.getLogger(WebSocketServerThread.class);
    @Override
    public void run() {
        WebSocketServer flumeServer = new WebSocketServer(37442);
        try {
            flumeServer.run();
        } catch (Exception e) {
            logger.error("flumeserver ex",e);
        }
    }

}
