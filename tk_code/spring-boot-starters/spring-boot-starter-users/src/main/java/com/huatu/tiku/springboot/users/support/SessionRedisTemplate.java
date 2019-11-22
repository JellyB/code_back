package com.huatu.tiku.springboot.users.support;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author hanchao
 * @date 2017/8/23 17:15
 */
@Slf4j
public class SessionRedisTemplate {
    private Pool<Jedis> pool;
    public SessionRedisTemplate(Pool<Jedis> pool){
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


    /**
     * redis get
     * @param key
     * @return
     */
    public String get(String key){
        return execute(jedis -> jedis.get(key));
    }
    /**
     * redis hget
     * @param key
     * @param field
     * @return
     */
    public String hget(String key,String field){
        return execute(jedis -> jedis.hget(key,field));
    }

    public Map<String,String> hgetAll(String key){
        return execute(jedis -> jedis.hgetAll(key));
    }

    /**
     * redis hset
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hset(String key,String field,String value){
        return execute(jedis -> jedis.hset(key,field,value));
    }

    /**
     * redis set
     * @param key
     * @param value
     * @return
     */
    public String set(String key,String value){
        return execute(jedis -> jedis.set(key,value));
    }

    /**
     * 设置失效时间
     * @param key
     * @param expire
     * @param unit
     * @return
     */
    public Long expire(String key, int expire, TimeUnit unit){
        if(unit != null) {
            return execute(jedis -> jedis.expire(key,(int) TimeUnit.SECONDS.convert(expire, unit)));
        }
        return execute(jedis -> jedis.expire(key,expire));
    }


    public interface JedisCallback<T> {
        T doInJedis(Jedis jedis);
    }
}
