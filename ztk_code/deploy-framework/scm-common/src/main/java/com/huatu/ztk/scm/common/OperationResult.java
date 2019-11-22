package com.huatu.ztk.scm.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-9-11
 * Time: 上午11:32
 * To change this template use File | Settings | File Templates.
 */
public class OperationResult {
    private final static String instance_fmt = "log_%s_%s";
    private final static String deploy_fmt = "deploy_log_%s";

    public static final Cache<String,String> log_cache = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    private static final Logger logger = LoggerFactory.getLogger(OperationResult.class);


    public static void put(String serverName, String ip, String log) {
        String key = String.format(instance_fmt, serverName, ip);
        log_cache.put(key, log);
    }

    public static String get(String serverName, String ip) {
        String key = String.format(instance_fmt, serverName, ip);
        return (String) log_cache.getIfPresent(key);
    }

    public static void putByProjectName(String projectName, String log) {
        String key = String.format(deploy_fmt, projectName);
//        System.out.println(client.getStats());
        log_cache.put(key, log);
    }

    public static void append(String projectName,String appendLog){
        String key = String.format(deploy_fmt, projectName);
        String log = (String) log_cache.getIfPresent(key);
        log_cache.put(key, log + appendLog);
    }

    public static void endLog(String projectName){
        String key = String.format(deploy_fmt, projectName);
        String log = (String) log_cache.getIfPresent(key);
        log_cache.put(key, log + "##LogEnd##");
    }

    public static String get(String projectName) {
        String key = String.format(deploy_fmt, projectName);
        return (String) log_cache.getIfPresent(key);

    }

    public static void main(String[] args) {
        put("a", "", "test data");
        System.out.println(get("a", ""));
    }

}
