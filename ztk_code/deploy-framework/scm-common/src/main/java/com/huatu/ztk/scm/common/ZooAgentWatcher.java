package com.huatu.ztk.scm.common;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZookeeperUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-7-25
 * Time: 下午6:55
 * To change this template use File | Settings | File Templates.
 */
public class ZooAgentWatcher implements CuratorWatcher {
    private final static Logger log = LoggerFactory.getLogger(ZooAgentWatcher.class);
    private final CuratorFramework client;


    public ZooAgentWatcher(CuratorFramework client) {
        this.client = client;
    }

    /**
     * 检查变更的节点是不是当前本机
     *
     * @param path
     * @return
     */
    private static boolean check(String path) {
        boolean ret = false;
        //root/projectGroup/ip/instance
        String[] seg = path.split("/");
        String pathIp = seg[seg.length - 2];

        int s = 0;
        Character cc = new Character('.');
        for (int i = 0; i < pathIp.length(); i++) {
            Character c = pathIp.charAt(i);
            if (c.compareTo(cc) == 0) {
                s++;
            }
        }

        if (s == 3 && seg.length > 2 && pathIp.equals(ZookeeperUtil.current_ip)) {
            return true;
        }
        log.info("path= " + path + " pathIp=" + pathIp + " current_ip=" + ZookeeperUtil.current_ip);
        return ret;
    }

    @Override
    public synchronized void process(WatchedEvent event) {
        long ct = System.currentTimeMillis();
        log.info("envnt happened: path="+event.getPath()+"  type="+event.getType());
        String path = event.getPath();
        if (path.startsWith("/huatu-scm/stat/")) {
            return;
        }
        //server初始化事件
        if (path.startsWith("/huatu-scm/init")) {
            //重新监听init
            ZookeeperUtil.childrenSingleWatch(path, this);
            initServerNode("/huatu-scm/init");
            return;
        }

        //lib包更新事件
        if (path.equals(ZookeeperUtil.basePath + "/lib")) {
            processUpdateLib();
            return;
        }
        //检查事件是否应该处理
        if (!check(path)) {
        	log.debug("give up event path="+path);
            return;
        }
        JSONObject json = ZookeeperUtil.getJson(path);
        String serverName = null;
        String action = null;
        Stats stats = null;
        StringBuffer sf = new StringBuffer();
        if(json!=null){
        	sf.append(json.toString() + "\r\n");
        	action = json.optString(InstanceDetail.Key.action.name());
            serverName = json.optString(InstanceDetail.Key.server_name.name());
        }
        String[] resultArr = null;
        boolean isSuccess = false;
        try {
            switch (event.getType()) {
                case NodeCreated:
                    log.info("create......" + path);
                    //重新监听
                    ZookeeperUtil.dataSingleWatch(path, this);
                    if (json == null) {
                        log.error("json is null, data:" + json);
                        return;
                    }
                    resultArr = processNodeCreated(json, path);
                    break;
                case NodeDataChanged:
                    //重新监听
                    ZookeeperUtil.dataSingleWatch(path, this);
                    if (json == null) {
                        log.error("json is null, data:" + json);
                        return;
                    }
                    log.info("NodeDataChanged......" + path);
                    resultArr = processNodeDataChanged(json, path);
                    log.info("exec finsh "+path);
                    break;
                default:
                    log.error("unknow event " + event.getType());
                    break;
            }
        }catch (Exception e) {
          log.error("Exception",e);
		} finally {
            log.info(action + "||" + serverName);
            //非节点创建和节点数据改变
            if(event.getType()!=EventType.NodeCreated&&
            		event.getType()!=EventType.NodeDataChanged){
            	ct = System.currentTimeMillis() - ct;
            	log.debug(event.getType()+" don't need set state");
                log.info("agent watcher execute time: " + ct + " ms");
                return;
            }

            if(resultArr!=null){
            	isSuccess="0".equals(resultArr[0]);
            	sf.append(resultArr[1]);
            }else{//未知错误
                isSuccess=false;
                sf.append("未知错误");
            }
            if(isSuccess){//操作成功
                if (action.equals(Action.DEPLOY.getCommand()) || action.equals(Action.SERVER_START.getCommand())
                        || action.equals(Action.SERVER_RESTART.getCommand())) {
                    stats = Stats.STARTED;
                } else if (action.equals(Action.SERVER_STOP.getCommand())) {
                    stats = Stats.STOPPED;
                } else if (action.equals(Action.SERVER_INIT.getCommand())) {
                    stats = Stats.INITED;
                }
            }
            AgentStatWorker.setStat(ZookeeperUtil.current_ip, stats, serverName,isSuccess);
            ct = System.currentTimeMillis() - ct;
            log.info("agent watcher execute time: " + ct + " ms");
        }

        OperationResult.put(serverName, ZookeeperUtil.current_ip, sf.toString());
    }

    private void initServerNode(String path) {
        try {
            List<String> list = client.getChildren().forPath(path);
            JSONObject json = null;
            String npath = null;
            for (String subPath : list) {
                subPath = path + "/" + subPath;
                if (subPath.contains(ZookeeperUtil.current_ip)) {
                    json = ZookeeperUtil.getJson(subPath);
                    //删除init数据存放节点
                    ZookeeperUtil.delete(subPath);
                    if (json == null) {
                        log.info("json is null path:" + subPath);
                        continue;
                    }
                    //server节点
                    npath = String.format(ZookeeperUtil.path_format,
                            json.getString(InstanceDetail.Key.project_name.name()), json.getString(InstanceDetail.Key.server_ip.name()),
                            json.getString(InstanceDetail.Key.server_name.name()));
                    //删除服务数据存放节点
                    ZookeeperUtil.delete(npath);
                    //创建前监听该节点
                    ZookeeperUtil.existSingleWatch(npath, this);
                    //创建持久节点
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(npath, json.toString().getBytes());
                }
            }
        } catch (Exception e) {
          log.error("Exception",e);
        }
    }

    /**
     * 通過節點path解析出server的名稱
     *
     * @param path
     * @return
     */
    private String parseServerName(String path) {
        String[] arr = path.split("/");
        if (arr.length != 5) {
            return null;
        }
        return arr[4];
    }

    /**
     * 更新本地jar类库
     */
    private void processUpdateLib() {
        String[] cmd = {Paths.BIN_HOME + "/jarlib.sh", "update"};
        ShellExec.exec(cmd,null);
    }

    private String[] processNodeCreated(JSONObject json, String path) {
        if (json == null || json.equals("")) {
            return new String[]{"1","unknow error"};
        }

        String[] ret = null;
        String action = json.optString(InstanceDetail.Key.action.name());

        if (Action.SERVER_INIT.getCommand().equals(action)) {
            String sname = json.optString(InstanceDetail.Key.server_name.name());
            Map<String, String> map = json2Map(json);
            ret = ShellExec.exec(Paths.BIN_HOME + "/init_server.sh",map);
            log.info(action + " instance[" + sname + "] from [" + ZookeeperUtil.current_ip + "]");
        }

        return ret;
    }


    private String[] processNodeDataChanged(JSONObject json, String path) {

        String[] ret = null;
        String action = json.optString(InstanceDetail.Key.action.name());
        String serverName = json.getString(InstanceDetail.Key.server_name.name());

        //update config
        if(action.equals(Action.SERVER_UPDATE.getCommand())){
            Map<String, String> map = json2Map(json);
            String[] shell = {Paths.BIN_HOME + "/update_server.sh", action};
            ret = ShellExec.exec(shell,map);
            log.info(action + " instance[" + serverName + "] from [" + ZookeeperUtil.current_ip + "]");

        }else if (action.equals(Action.DEPLOY.getCommand())||
                action.equals(Action.SERVER_START.getCommand())
                || action.equals(Action.SERVER_STOP.getCommand())
                || action.equals(Action.SERVER_RESTART.getCommand())
                || action.equals(Action.SERVER_DUMP.getCommand())
                || action.equals(Action.SERVER_DELETE.getCommand())) {
            Map<String, String> map = json2Map(json);
            String[] shell = {Paths.BIN_HOME + "/deploy_server.sh", action};
            ret = ShellExec.exec(shell,map);
            //删除的话，得删除zk节点
            if(action.equals(Action.SERVER_DELETE.getCommand())){
                if("0".equals(ret[0])){//操作成功
                    log.info("delete server node: "+path);
                    //产出数据节点
                    ZookeeperUtil.delete(path);
                    String spath = String.format(ZookeeperUtil.statPath_format, ZookeeperUtil.current_ip, serverName);
                    //删除状态节点
                    ZookeeperUtil.delete(spath);
                    String initPath = String.format(ZookeeperUtil.init_server_path_format, ZookeeperUtil.current_ip, serverName);
                    //删除初始化节点
                    ZookeeperUtil.delete(initPath);
                }else {
                    log.warn("delete server "+serverName+" fail");
                }
            }
            log.info(action + " instance[" + serverName + "] from [" + ZookeeperUtil.current_ip + "]");
        }
        return ret;
    }

    private Map json2Map(JSONObject data){
        Iterator<String> keys = data.keys();
        Map map = new HashMap();
        String key = null;
        Object value = null;
        while (keys.hasNext()){
            key = keys.next();
            value = data.get(key);
            if(value!=null){
                map.put(key,value.toString());
            }
        }

        return map;
    }

}
