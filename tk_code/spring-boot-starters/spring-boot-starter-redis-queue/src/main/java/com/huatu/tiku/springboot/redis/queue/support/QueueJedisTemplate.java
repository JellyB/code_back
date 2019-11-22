package com.huatu.tiku.springboot.redis.queue.support;

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
        } catch(Exception e){
            log.error("execute jedis command error... ",e);
            throw e;
        }finally {
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
