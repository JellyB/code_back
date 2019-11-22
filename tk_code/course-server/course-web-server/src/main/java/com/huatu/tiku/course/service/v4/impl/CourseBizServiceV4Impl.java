package com.huatu.tiku.course.service.v4.impl;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.FallbackCacheHolder;
import com.huatu.tiku.course.netschool.api.v4.AppServiceV4;
import com.huatu.tiku.course.service.cache.CacheUtil;
import com.huatu.tiku.course.service.cache.CourseCacheKey;
import com.huatu.tiku.course.service.v4.CourseBizServiceV4;
import com.huatu.tiku.course.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by lijun on 2018/7/5
 */
@Service
public class CourseBizServiceV4Impl implements CourseBizServiceV4 {

    @Autowired
    private CacheUtil cacheUtil;

    @Autowired
    private AppServiceV4 appServiceV4;

    @Override
    public Object collectionClasses(final HashMap map) {
        Supplier key = () -> CourseCacheKey.collectionClassesKeyV4(map);
        Supplier<Object> value = () -> {
            NetSchoolResponse netSchoolResponse = appServiceV4.collectionClasses(map);
            return ResponseUtil.build(netSchoolResponse);
        };
        Object result = cacheUtil.getCacheStringValue(key, value, 30, TimeUnit.SECONDS);
        //错处处理
        FallbackCacheHolder.put(key.get(), result);
        return result;
    }
}
