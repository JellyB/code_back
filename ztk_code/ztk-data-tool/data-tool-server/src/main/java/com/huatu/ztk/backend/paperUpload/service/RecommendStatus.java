package com.huatu.ztk.backend.paperUpload.service;

import com.huatu.ztk.backend.paperUpload.bean.PaperUploadRedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lenovo on 2017/6/12.
 */
@Service
public class RecommendStatus {
    private static Logger logger = LoggerFactory.getLogger(RecommendStatus.class);
    @Autowired
    private RedisTemplate redisTemplate;
    public void putAllRedisStatus(String time,String str1,String str2,String key){
        HashOperations ops = redisTemplate.opsForHash();
        String code_key = "code_"+time;
        String code_value = "message_"+time;
        Map statusMap = new HashMap();
        statusMap.put(code_key,str1);
        statusMap.put(code_value,str2);
        ops.putAll(key,statusMap);
    }
    public void deleteRedisStatus(String time,String key){
        HashOperations ops = redisTemplate.opsForHash();
        String code_key = "code_"+time;
        String code_value = "message_"+time;
        ops.delete(key,code_key);
        ops.delete(key,code_value);
    }
    public Map getRedisStatus(String time,String key){
        HashOperations ops = redisTemplate.opsForHash();
        String code_key = "code_"+time;
        String code_value = "message_"+time;
        Map statusMap = new HashMap();
        if(ops.get(key,code_key)!=null){
            statusMap.put(code_key,ops.get(key,code_key));
            logger.error("redis-{}-{} already existed : {}",key,code_key,ops.get(key,code_key));
        }
        if(ops.get(key,code_value)!=null){
            statusMap.put(code_value,ops.get(key,code_value));
            logger.error("redis-{}-{} already existed : {}",key,code_value,ops.get(key,code_value));
        }
        return statusMap;
    }
    public Map getRedisStatus(String key){
        HashOperations ops = redisTemplate.opsForHash();
        return ops.entries(key);
    }
}
