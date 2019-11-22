package com.huatu.ztk.monitor.common;

import com.google.common.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.google.common.cache.CacheBuilder.newBuilder;

/**
 * Created by linkang on 1/10/17.
 */
@Component
public class MonitorCache {
    public static final Cache<String, Integer> countCache =
            newBuilder()
                    .expireAfterWrite(MonitorConstants.SAVE_INTERVAL, TimeUnit.MINUTES)
                    .build();
}
