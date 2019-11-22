package com.huatu.ztk.scm.common;

import com.google.common.base.Function;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZookeeperUtil;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.zookeeper.ZookeeperUtil.basePath;
import static org.apache.zookeeper.ZookeeperUtil.path_format;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-7-22
 * Time: 下午2:18
 * To change this template use File | Settings | File Templates.
 */
public class ConsoleCommand {
    private final static Logger log = LoggerFactory.getLogger(ConsoleCommand.class);
    private final static ZooAgentWatcher watcher = new ZooAgentWatcher(ZookeeperUtil.getZkClient());
    
    /**
     * 编译打包
     * @param bin_home
     * @param dto
     * @return
     */
    public static String pack(String reqId,String bin_home, PackDto dto) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("git_home", dto.getGit_home());
        param.put("project", dto.getProject());
        param.put("tag", dto.getTag());
        param.put("branch", dto.getBranch());
        param.put("module", dto.getModule());
        param.put("environment", dto.getEnvironment());
        param.put("type", dto.getType() + "");
        param.put("pack_type", dto.getPack_type() + "");
        param.put("tag_remark", dto.getRemark());
        if(dto.getUpdateDependency()){
        	param.put("updateDependency", "-U");
        }else{
        	param.put("updateDependency", "");
        }
        return ShellExec.exec(reqId,bin_home + "package.sh",param)[1];
    }
    
    /**
     * 版本回退
     * @param bin_home
     * @param module 模块名
     * @param tag 版本号
     * @return
     */
    public static JSONObject tagBack(String bin_home,String module,String tag){
    	Map<String, String> param = new HashMap<String, String>();
    	param.put("module",module );
    	param.put("tag", tag);
      JSONObject json = new JSONObject();
    	String[] arr = ShellExec.exec(bin_home + "tag_back.sh",param);
      boolean res = false;
    	if(arr!=null && "0".equals(arr[0])){
        res = true;
      }
      json.put("result",res);
      json.put("msg",arr[1]);
      return json;
    }

    /**
     * 初始化project
     *
     * @param bin_home
     * @param gitHome
     * @param project
     * @return
     */
    public static boolean initProject(String bin_home, String gitHome, String project) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("git_home", gitHome);
        param.put("project", project);
        String[] arr = ShellExec.exec(bin_home + "init_project.sh",param);
        if(arr!=null && "0".equals(arr[0])){
        	return true;
        }
        return false;
    }


    public static String deploy(final InstanceDetail detail,int timeOut) {
        log.info("deploy action, instance detail ==>>" + detail.toJson());
        final String serverIp = detail.getServerIp();
        String spath = String.format(ZookeeperUtil.statPath_format, serverIp, detail.getServerName());
        String old = StatLock.getStat(spath);
        ZookeeperUtil.execute(new Function<CuratorFramework, Void>() {
            final String path = String.format(path_format, detail.getProjectName(), serverIp, detail.getServerName());

            @Override
            public Void apply(CuratorFramework client) {
                try {
                    Stat stat = client.checkExists().forPath(path);
                    if (stat == null) {
                        log.error("server instance " + path + " not installed, deploy fail.");
                        return null;
                    }

                    JSONObject json = detail.toJson();
                    stat = client.setData().forPath(path, json.toString().getBytes());
                    if (stat != null) {
                        log.debug("deploy " + path + " success");
                    } else {
                        log.error("deploy " + path + " fail.");
                    }
                } catch (Exception e) {
                    log.error("Exception",e);
                }

                return null;
            }
        });

        String node = serverIp + "," + detail.getServerName() + "," + StatLock.waintUnitTimeOut(spath, old,serverIp, timeOut);
        log.info("deploy result==> " + node);
        return node;
    }

    public static String initializeInstance(final InstanceDetail detail) {
    	final String serverIp = detail.getServerIp();
    	final String initPath = String.format(ZookeeperUtil.init_server_path_format, serverIp, detail.getServerName());
    	final String spath = String.format(ZookeeperUtil.statPath_format, serverIp, detail.getServerName());
    	String old = StatLock.getStat(spath);
        ZookeeperUtil.execute(new Function<CuratorFramework, Void>() {
            @Override
            public Void apply(CuratorFramework client) {
                try {
                    Stat stat = client.checkExists().forPath(initPath);
                    if(stat!=null){
                    	log.warn("path is exist " + initPath+", delete it");
                    	client.delete().forPath(initPath);
                    }
                    JSONObject json = detail.toJson();
                    json.put(InstanceDetail.Key.action.name(), Action.SERVER_INIT.getCommand());
                    json.put(InstanceDetail.Key.stat.name(), InstanceDetail.Stat.INIT.Value);
                    //创建持久节点
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(initPath, json.toString().getBytes());
                    log.info("write init data: path={} data={}",initPath,json.toString());
                } catch (Exception e) {
                    log.error("Exception",e);
                }
                return null;
            }
        });
        String stat = StatLock.waintUnitTimeOut(spath, old,serverIp, -1);
        String node =  serverIp+","+ detail.getServerName() + "," + stat;
        log.info(InstanceDetail.Key.action + " result==> " + node);
        return node;
    }

    public static void updateLibrary() {
        ZookeeperUtil.execute(new Function<CuratorFramework, Void>() {
            @Override
            public Void apply(CuratorFramework client) {
                final String path = basePath + "/lib";
                try {
                    Stat stat = client.checkExists().forPath(path);
                    JSONObject json = new JSONObject();
                    json.put(InstanceDetail.Key.action.name(), Action.SERVER_INIT.getCommand());
                    log.info(json + "||" + path + "||" + stat.getVersion());
                    if (stat == null) {
                        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, json.toString().getBytes());
                        return null;
                    }

                    client.setData().forPath(path, json.toString().getBytes());

                } catch (Exception e) {
                    log.error("Exception",e);
                }

                return null;
            }
        });
    }
}
