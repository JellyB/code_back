package com.huatu.tiku.springboot.redis.queue.core;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huatu.tiku.springboot.redis.queue.support.QueueJedisTemplate;
import redis.clients.jedis.JedisPool;

import java.util.function.Function;

/**
 * @author hanchao
 * @date 2017/10/31 16:46
 */
public class RedisQueueTemplate {
    private JedisPool jedisPool;
    private QueueJedisTemplate jedisTemplate;

    public RedisQueueTemplate(JedisPool jedisPool){
        this.jedisPool = jedisPool;
        this.jedisTemplate = new QueueJedisTemplate(jedisPool);
    }

    public void convertAndSend(String queue,Object... objects){
        convertAndSend(queue,o -> JSON.toJSONString(o),objects);
    }

    public void convertAndSend(String queue, Function<? super Object,String> mapper, Object ...objects){
        String[] messages = Lists.newArrayList(objects).stream().map(mapper).toArray(String[]::new);
        this.jedisTemplate.lpush(queue,messages);
    }
}
