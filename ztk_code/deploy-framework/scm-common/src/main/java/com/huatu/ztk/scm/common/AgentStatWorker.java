package com.huatu.ztk.scm.common;

import com.google.common.io.Files;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZookeeperUtil;
import org.apache.zookeeper.data.Stat;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-8-2
 * Time: 下午3:08
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class AgentStatWorker {

    private final static Logger logger = LoggerFactory.getLogger(AgentStatWorker.class);
    private static CuratorFramework zkClient = ZookeeperUtil.getZkClient();
    private final static ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);

    public void start() {

        schedule.schedule(new Runnable() {
            @Override
            public void run() {

                String[] jps = ShellExec.exec(Paths.BIN_HOME + "/cmd_jps.sh",null);

                File dir = new File(Paths.PID_HOME);
                if (dir.isDirectory()) {
                    File[] pids = dir.listFiles();
                    assert pids != null;
                    for (File pid : pids) {
                        try {
                            String id = Files.readFirstLine(pid, Charset.defaultCharset());
                            System.out.println("pid content: " + id + "=== jps :" + jps);
                            if (!jps[1].contains(id)) {
                                setStat(ZookeeperUtil.current_ip, Stats.ERROR_DOWN, pid.getName().split("\\.")[0],true);
                                pid.deleteOnExit();
                            }
                        } catch (IOException e) {
                            logger.error("zk error",e);
                        }
                    }
                }
            }
        }, 3, TimeUnit.SECONDS);
    }


    public static void setStat(String ip, Stats stats, String serverName,boolean isSuccess) {
        logger.info("set stat to zk, " + ip + "||" + stats + "||" + serverName);
        String path = String.format(ZookeeperUtil.statPath_format, ip, serverName);

        JSONObject json = ZookeeperUtil.getJson(path);
        if(json==null){
        	json = new JSONObject();
        }
        try {
        	if(stats!=null){
        		json.put("stat", stats);
        	}
            json.put("action_success", isSuccess);
            json.put("uptime", System.currentTimeMillis());
            json.put("serverName", serverName);
        } catch (JSONException e) {
          logger.error("JSONException",e);
        }

        try {
            logger.info("write zk, inform console server ===>>> " + path + "||" + json);
            Stat st = zkClient.checkExists().forPath(path);
            if (st == null) {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, json.toString().getBytes());
            }
            zkClient.setData().forPath(path, json.toString().getBytes());
        } catch (Exception e) {
            logger.error("zk error",e);
        }

    }
}
