package com.huatu.ztk.knowledge.util;

import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author huangqingpeng
 * @title: DebugCacheUtil
 * @description: TODO
 * @date 2019-08-1514:52
 */
public class DebugCacheUtil {

    public static AtomicBoolean debug_flag = new AtomicBoolean(false);

    public static ConcurrentMap concurrentHashMap = Maps.newConcurrentMap();

    public static void showCacheContent(Cache cache, String questionPointCache){
        if(debug_flag.get() && null != cache){
            System.out.println("cache = " + questionPointCache);
            System.out.println("content = " + JsonUtil.toJson(cache.asMap()));
            concurrentHashMap.put(questionPointCache,cache.asMap());
        }
    }

    public static boolean changeDebugFlag(){
        boolean b = debug_flag.get();
        debug_flag.set(!b);
        return debug_flag.get();
    }

    public static void doInt(Process runnable) {
        if(!debug_flag.get()){
            return;
        }
        try {
            runnable.run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface Process {
        void run() throws Exception;
    }
}
