package com.huatu.tiku.position.biz.util;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author wangjian
 **/
public class MapUtil {

    public static Map of(Object k1, Object v1){
        Map<Object,Object> map= Maps.newHashMap();
        map.put(k1,v1);
        return map;
    }

    public static Map of(Object k1, Object v1,
                         Object k2, Object v2){
        Map<Object,Object> map= Maps.newHashMap();
        map.put(k1,v1);
        map.put(k2,v2);
        return map;
    }

    public static Map of(Object k1, Object v1,
                         Object k2, Object v2,
                         Object k3, Object v3){
        Map<Object,Object> map= Maps.newHashMap();
        map.put(k1,v1);
        map.put(k2,v2);
        map.put(k3,v3);
        return map;
    }

    public static Map of(Object k1, Object v1,
                         Object k2, Object v2,
                         Object k3, Object v3,
                         Object k4, Object v4){
        Map<Object,Object> map= Maps.newHashMap();
        map.put(k1,v1);
        map.put(k2,v2);
        map.put(k3,v3);
        map.put(k4,v4);
        return map;
    }

    public static Map of(Object k1, Object v1,
                         Object k2, Object v2,
                         Object k3, Object v3,
                         Object k4, Object v4,
                         Object k5, Object v5) {
        Map<Object, Object> map = Maps.newHashMap();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return map;
    }
        public static Map of(Object k1, Object v1,
                         Object k2, Object v2,
                         Object k3, Object v3,
                         Object k4, Object v4,
                         Object k5, Object v5,
                         Object k6, Object v6){
        Map<Object,Object> map= Maps.newHashMap();
        map.put(k1,v1);
        map.put(k2,v2);
        map.put(k3,v3);
        map.put(k4,v4);
        map.put(k5,v5);
        map.put(k6,v6);
        return map;
    }
}
