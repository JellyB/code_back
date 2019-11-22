package com.huatu.ztk.redisqueue.core;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * @author hanchao
 * @date 2017/8/23 17:15
 */
@Slf4j
public class QueueJedisTemplate {
    private Pool<Jedis> pool;
    public QueueJedisTemplate(Pool<Jedis> pool){
        this.pool = pool;
    }


    public <T> T execute(JedisCallback<T> callback){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return callback.doInJedis(jedis);
        } finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }


    public Long lpush(String key,String... values){
        return execute(jedis -> jedis.lpush(key,values));
    }


    interface JedisCallback<T> {
        T doInJedis(Jedis jedis);
    }


}
