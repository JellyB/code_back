package com.huatu.tiku.springboot.redis.queue.core;

/**
 * @author hanchao
 * @date 2017/10/31 23:54
 */
public interface QueueListenerContainer {
    void addListener(RedisQueueListener redisQueueListener);
    void removeListener(RedisQueueListener redisQueueListener);
}
