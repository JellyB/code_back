package com.huatu.tiku.course.test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.entity.CourseLiveBackLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.assertj.core.util.Lists;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-10 2:15 PM
 **/
public class GuavaCacheTest {

    private static final List<String> chars = Lists.newArrayList("A", "B", "C", "D", "E");

    public static void main(String[] args) throws Exception {

        Cache<String, Object> logCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).maximumSize(100).build();
        logCache.put("A", "A");
        logCache.put("B", "B");
        logCache.put("C", "C");

        for (int i = 0; i < chars.size(); i++) {
            String key = chars.get(i);
            Object object = logCache.get(key, new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    if("D".equals(key)){
                        return "DDDD";
                    }else{
                        return "EEEE";
                    }
                }
            });
        }
        System.err.println(logCache.asMap());

        for (int i = 0; i < chars.size(); i++) {
            System.err.println("--------------" + logCache.getIfPresent(chars.get(i)));
        }
    }
}
