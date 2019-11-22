package com.huatu.tiku.course.service.cache;

import com.huatu.common.utils.cache.NullHolder;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存工具类
 * Created by lijun on 2018/6/20
 */
@Component
public class CacheUtil {

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> valueOperations;

    /**
     * 缓存 key - value 形式的数据
     *
     * @param keySupplier   生成key
     * @param valueSupplier 生成value
     * @return
     */
    public <T> T getCacheStringValue(
            Supplier<String> keySupplier,
            Supplier<T> valueSupplier,
            Integer time, TimeUnit timeUnit
    ) {
        String key = keySupplier.get();
        Object object = valueOperations.get(key);
        if (object != null) {
            if (object instanceof NullHolder) {
                return null;
            }
            return (T) object;
        }
        //获取源数据
        T cacheData = valueSupplier.get();
        if (objectIsBank(cacheData)) {//数据库不存在，使用默认值-防止接口被刷
            valueOperations.set(key, NullHolder.DEFAULT, 5, TimeUnit.SECONDS);
        } else {
            //TODO: 添加缓存时间字段
            //生成缓存数据
            valueOperations.set(key, cacheData, time, timeUnit);
        }
        return cacheData;
    }

    /**
     * 判断缓存的对象是否需要缓存原对象
     *
     * @param o
     * @return
     */
    private static boolean objectIsBank(Object o) {
        if (null == o) {
            return true;
        }
        if (o instanceof Collection) {
            return ((Collection) o).size() == 0;
        }
        return false;
    }
}
