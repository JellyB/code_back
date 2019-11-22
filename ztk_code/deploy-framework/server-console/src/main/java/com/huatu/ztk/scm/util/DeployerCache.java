package com.huatu.ztk.scm.util;

import org.apache.commons.io.FileUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * server和其最后部署人员的对应关系cache
 * Created by shaojieyue
 * Time 2014-04-04 16:15
 */
public class DeployerCache {
    private static final Logger logger = LoggerFactory.getLogger(DeployerCache.class);
    private static ConcurrentNavigableMap<String,String> map = null;
    private static DB db = null;
    static {
        //Configure and open database using builder pattern.
        //All options are available with code auto-completion.
        File dbFile = null;
        try {
            dbFile = new File("/tmp/mapdb/", "server_deployer_db");
            if(!dbFile.exists()){
                FileUtils.touch(dbFile);
            }
        } catch (IOException e) {
            logger.error("er",e);
        }
        logger.info("mapdb open file :"+dbFile);
        db = DBMaker.newFileDB(dbFile)
                .closeOnJvmShutdown()
                .encryptionEnable("123456")
                .make();

        //open an collection, TreeMap has better performance then HashMap
        map = db.getTreeMap("server_deployer");
//
//        map.put(1,"one");
//        map.put(2,"two");
//        //map.keySet() is now [1,2] even before commit
//
//        db.commit();  //persist changes into disk
//
//        map.put(3,"three");
//        //map.keySet() is now [1,2,3]
//        db.rollback(); //revert recent changes
//        //map.keySet() is now [1,2]
//
//        db.close();
    }

    /**
     * 获取server部署人员
     * @param serverName
     * @return
     */
    public static String getDeployer(String serverName){

        String deployer = map.get(serverName);
        return deployer;
    }

    /**
     * 设置server部署人员
     * @param serverName
     * @param deployer
     */
    public static void setDeployer(String serverName,String deployer){
        map.put(serverName, deployer);
        //persist changes into disk
        db.commit();
    }
}
