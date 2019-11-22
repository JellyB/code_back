package com.huatu.tiku.course.spring.conf.aspect.mapParam;

import java.util.HashMap;

/**
 * 存储参数
 * Created by lijun on 2018/7/6
 */
public class LocalMapParamHandler {

    private static ThreadLocal<HashMap<String,Object>> MAP_PARAM = new ThreadLocal<>();

    /**
     * 本地线程 设置参数
     */
    public static void set(HashMap<String,Object> map){
        MAP_PARAM.set(map);
    }

    /**
     * 本地线程 获取参数
     */
    public static HashMap<String,Object> get(){
        return MAP_PARAM.get();
    }

    /**
     * 清空所有数据
     */
    public static void clean(){
        MAP_PARAM.remove();
    }
}
