package org.apache.zookeeper;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkImpl;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.data.Stat;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * This shows a very simplified method of registering an instance with the service discovery. Each individual
 * instance in your distributed set of applications would create an instance of something similar to ExampleServer,
 * start it when the application comes up and close it when the application shuts down.
 */
public class ZookeeperUtil {
    private final static Logger log = LoggerFactory.getLogger(ZookeeperUtil.class);
    public final static String ZK_ADDR = "192.168.100.110:2181,192.168.100.111:2181,192.168.100.112:2181";
    public final static String basePath = "/huatu-scm";
    public final static String statPath_format = "/huatu-scm/stat/%s%s";
    public final static String path_format = basePath + "/%s/%s/%s";
    public final static String init_server_path_format = basePath + "/init/%s%s";

    public static String current_ip;
    private static CuratorFrameworkImpl zkClient = null;
    static {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        zkClient = (CuratorFrameworkImpl) CuratorFrameworkFactory.newClient(ZK_ADDR, retryPolicy);
        zkClient.start();
    }

    public static CuratorFrameworkImpl getZkClient() {
        return zkClient;
    }
    
    public static void delete(String path){
    	try {
    		if(zkClient.checkExists().forPath(path)!=null){
    			log.info("delete the node "+path);
    			zkClient.delete().forPath(path);
    		}else{
    			log.warn("the node "+path +" not exist,delete fail.");
    		}
		} catch (Exception e) {
			log.error("exceprion",e);
		}
    }

    public static void execute(Function f) {
          f.apply(zkClient);
    }
    
    public static JSONObject getJson(String path){
    	String str = get(path);
    	try {
    		if(str!=null){
    			JSONObject json = new JSONObject(str);
    			return json;
    		}
		} catch (JSONException e) {
			log.error("zk query data ex",e);
		}
    	return null;
    }

    public static String get(String path) {
        if (path == null) {
            return null;
        }

        String ret = null;
        try {
            //检测节点是否存在
            Stat stat = zkClient.checkExists().forPath(path);
            if(stat ==null){
                log.warn("path="+path+" is not exist");
                return null;
            }

            byte[] data = zkClient.getData().forPath(path);
            if (data != null) {
                ret = new String(data);
            }

        } catch (Exception e) {
            log.error("zk error",e);
        }
        return ret;
    }

    /**
     * 为节点添加单个watch，如果已经存在，则不添加
     * @param path
     * @param watcher
     * @throws Exception
     */
    public synchronized static void dataSingleWatch(String path,CuratorWatcher watcher) {
        if(!checkDataWatcher(path)){
            log.info("watch path="+path);
            try {
                zkClient.getData().usingWatcher(watcher).forPath(path);
            } catch (Exception e) {
                try {
                    if(!checkDataWatcher(path)) {
                        zkClient.getChildren().usingWatcher(watcher).forPath(path);
                    }
                } catch (Exception e1) {
                    log.error("ex",e);
                }
            }
        }else {
            log.warn("path=" + path + " hased data watch,add watch canel");
        }
    }

    /**
     * 为节点添加单个watch，如果已经存在，则不添加
     * @param path
     * @param watcher
     * @throws Exception
     */
    public synchronized static void childrenSingleWatch(String path,CuratorWatcher watcher) {
        if(!checkChildrenWatcher(path)){
            System.out.println("watch path="+path);
            try {
                zkClient.getChildren().usingWatcher(watcher).forPath(path);
            } catch (Exception e) {
                if(!checkChildrenWatcher(path)) {
                    try {
                        zkClient.getChildren().usingWatcher(watcher).forPath(path);
                    } catch (Exception e1) {
                        log.error("ex", e);
                    }
                }
            }
        }else {
            log.warn("path=" + path + " hased children watch,add watch canel");
        }
    }

    /**
     * 为节点添加单个watch，如果已经存在，则不添加
     * @param path
     * @param watcher
     * @throws Exception
     */
    public synchronized static void existSingleWatch(String path,CuratorWatcher watcher) {
        if(!checkExistWatcher(path)){
            log.info("watch path="+path);
            try {
                zkClient.checkExists().usingWatcher(watcher).forPath(path);
            } catch (Exception e) {
                if(!checkExistWatcher(path)) {
                    try {
                        zkClient.checkExists().usingWatcher(watcher).forPath(path);
                    } catch (Exception e1) {
                        log.error("ex", e);
                    }
                }
            }
        }else {
            log.warn("path=" + path + " hased children watch,add watch canel");
        }
    }

    public static void printAllWatcher() {
        try {
            ZooKeeper zoo = zkClient.getZookeeperClient().getZooKeeper();

            List<String> dataWatches = zoo.getDataWatches();
            List<String> childWatches = zoo.getChildWatches();
            List<String> existWatches = zoo.getExistWatches();

            for (String str : dataWatches) {
              log.info("data watch ===>>" + str);
            }

            for (String str : childWatches) {
              log.info("children watch ===>>" + str);
            }

            for (String str : existWatches) {
              log.info("exist watch ===>>" + str);
            }

        } catch (Exception e) {
            log.error("exceprion",e);
        }
    }

    /**
     * 检查该节点是否存在data watch
     * @param path
     * @return
     */
    public static boolean checkDataWatcher(String path) {
        try {
            ZooKeeper zoo = zkClient.getZookeeperClient().getZooKeeper();
            List<String> dataWatches = zoo.getDataWatches();
            return dataWatches.contains(path);
        } catch (Exception e) {
            log.error("exceprion",e);
        }
        return false;
    }

    /**
     * 检查该节点是否存在 child 节点
     * @param path
     * @return
     */
    public static boolean checkChildrenWatcher(String path) {
        try {
            ZooKeeper zoo = zkClient.getZookeeperClient().getZooKeeper();
            List<String> dataWatches = zoo.getChildWatches();
            return dataWatches.contains(path);
        } catch (Exception e) {
            log.error("exceprion",e);
        }
        return false;
    }

    /**
     * 检查该节点是否存exist监听
     * @param path
     * @return
     */
    public static boolean checkExistWatcher(String path) {
        try {
            ZooKeeper zoo = zkClient.getZookeeperClient().getZooKeeper();
            List<String> dataWatches = zoo.getExistWatches();
            return dataWatches.contains(path);
        } catch (Exception e) {
            log.error("exceprion",e);
        }
        return false;
    }

    public static void main(String[] args) {
        ZookeeperUtil.execute(new Function<CuratorFramework, Void>(){
            @Override
            public Void apply(CuratorFramework client) {
                try {
                List list=    client.getChildren().forPath("/huatu-scm/init");
                    System.out.println(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }
}
