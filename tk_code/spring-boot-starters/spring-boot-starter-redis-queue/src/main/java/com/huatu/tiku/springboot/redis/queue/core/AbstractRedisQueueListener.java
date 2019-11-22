package com.huatu.tiku.springboot.redis.queue.core;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author hanchao
 * @date 2017/10/31 16:58
 */
public abstract class AbstractRedisQueueListener<T> implements RedisQueueListener {
    private Type type;

    public AbstractRedisQueueListener() {
        Type superClass = getClass().getGenericSuperclass();
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    @Override
    public void consume(String message) {
        T t = JSON.parseObject(message,contentType());
        consumeContent(t);
    }

    public abstract void consumeContent(T t);

    public Type contentType(){
        return this.type;
    }
}
