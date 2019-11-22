package com.huatu.ztk.scm.common;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZookeeperUtil;
import org.apache.zookeeper.data.Stat;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * watch instance stat
 * User: shijinkui
 * Date: 13-7-25
 * Time: 下午6:55
 * To change this template use File | Settings | File Templates.
 */
public class StatLock {
    private final static Logger logger = LoggerFactory.getLogger(StatLock.class);

    private static CuratorFramework zkClient = ZookeeperUtil.getZkClient();
    private final static StatWatcher statWatcher = new StatWatcher();

    /**
     * @param node ip+serverName
     * @return
     */
    public static String getStat(String node) {
        String ret = null;
        try {
            Stat stat = zkClient.checkExists().forPath(node);
            if (stat != null) {
                byte[] d = zkClient.getData().forPath(node);
                ret = new String(d);
            }
        } catch (Exception e) {
            logger.error("exception",e);
        }
        logger.info(node + "||" + ret);
        return ret;
    }

    public static StatWatcher getStatWatcher() {
        return statWatcher;
    }

    public static String waintUnitTimeOut(String path, String olderNodeDate,String targetIp, int timeout) {
        int times = timeout > 0 ? timeout : 10;

        JSONObject older = null;
        try {
        	if(olderNodeDate!=null){
        		older =  new JSONObject(olderNodeDate);
        	}
        } catch (JSONException e) {
            logger.error("exception",e);
        }
        String url = "http://"+targetIp+":37443/cmd/ping";
      try {
        String[] res = HttpUtil.httpGetAll(url);
        if(!"200".equals(res[0])){
            logger.error("url="+url+" conn code:"+res[0]);
            logger.error("error content: "+res[1]);
            return "agent未知异常";
        }else {
          JSONObject json = new JSONObject(res[1]);
          logger.info("-->"+targetIp+" zk="+json);
          if(json.has("zkStarted")&&!json.optBoolean("zkStarted")){
              return json.getString("msg");
          }
        }
      } catch (Exception e) {
        logger.error("exception",e);
        return "agent未启动";
      }
      String ret = "time out";
        boolean isBreak = false;
        for (int i = 0; i < times; i++) {
            String node = getStat(path);
            if (node != null) {
                try {
                    JSONObject js = new JSONObject(node);
                    long ct = js.optLong("uptime");
                    String stat = js.optString("stat");
                    assert older != null;
                    //older==null 说明节点是新创建的
                    if (older==null||ct > older.optLong("uptime",0)) {
                    	boolean b = js.optBoolean("action_success", true);
                        ret = stat;
                        if(b){
                        	ret = ret + " 操作结果:成功";
                        }else{
                        	ret = ret + " 操作结果:失败";
                        }
                        isBreak = true;
                    }
                } catch (JSONException e) {
                    logger.error("exception",e);
                }
            }
            //新节点无，旧节点有数据，说明节点已经删除
            if(node==null&&olderNodeDate!=null){
            	ret= " 操作结果:成功";
            	isBreak = true;
            }
            
            

            if (isBreak) {
                break;
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("exception",e);
                }
            }
        }

        return ret;
    }

    private static class StatWatcher implements CuratorWatcher {

        private JSONObject getData(String path) {
            JSONObject json = null;
            String data = null;
            try {
                byte[] _data = zkClient.getData().forPath(path);
                data = new String(_data);
                json = new JSONObject(data);
            } catch (Exception e) {
                logger.error("exceprion path="+path+" data="+data,e);
            }

            return json;
        }

        @Override
        public synchronized void process(WatchedEvent event) {

            String path = event.getPath();
            if (path == null || !path.contains(ZookeeperUtil.basePath + "/stat/")) {
                logger.info(path + ", i cannot care.");
                return;
            }

            try {
            	ZookeeperUtil.dataSingleWatch(path, this);
                switch (event.getType()) {
                    case NodeDeleted:
                        System.out.println("NodeDeleted......" + path);

                        break;
                    case NodeCreated:
                        System.out.println("NodeCreated......" + path);
                        break;
                    case NodeDataChanged:
                        System.out.println("NodeDataChanged......" + path);
                        JSONObject json = getData(path);
                        if (json == null) {
                            System.err.println("json is null");
                        }

                        break;
                    case NodeChildrenChanged:
                        System.out.println("NodeChildrenChanged......" + path);

                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("exception",e);
            }
        }

    }
}
