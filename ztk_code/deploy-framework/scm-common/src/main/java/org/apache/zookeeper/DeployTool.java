package org.apache.zookeeper;

import java.util.List;

/**
 * Created by shaojieyue
 * Time 2014-03-21 10:28
 */
public class DeployTool {
    public static void main(String[] args) throws Exception {
        delAllInit();
    }

    public static void delAllInit() throws Exception {
        List<String> list = ZookeeperUtil.getZkClient().getChildren().forPath(ZookeeperUtil.basePath + "/init");
        if(list ==null){
            return;
        }
        for(String str:list){
            String path = ZookeeperUtil.basePath + "/init/"+str;
            ZookeeperUtil.delete(path);
            System.out.println("delete "+path);
        }
    }
}
