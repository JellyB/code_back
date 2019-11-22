package com.huatu.tiku.course.service.v5.impl;

import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v5.OrderServiceV5;
import com.huatu.tiku.course.service.cache.OrderCacheKey;
import com.huatu.tiku.course.service.v5.OrderServiceV5Biz;
import com.huatu.tiku.course.util.ResponseUtil;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by lijun on 2018/10/22
 */
@Service
public class OrderServiceV5BizImpl implements OrderServiceV5Biz {


    @Autowired
    private OrderServiceV5 orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Object bigGiftOrder(HashMap<String, Object> map) {

        NetSchoolResponse response = orderService.zeroOrder(map);
        Object result = ResponseUtil.build(response);
        SetOperations setOperations = redisTemplate.opsForSet();
        Integer classId = MapUtils.getInteger(map, "classId");
        String userName = MapUtils.getString(map, "userName");
        setOperations.add(OrderCacheKey.zeroOrder(classId), userName);
        //7 天缓存失效
        redisTemplate.expire(OrderCacheKey.zeroOrder(classId), 7L, TimeUnit.DAYS);
        return result;
    }

    @Override
    public boolean hasGetBigGiftOrder(Integer classId, final String userName) {
        SetOperations setOperations = redisTemplate.opsForSet();
        Boolean setOperationsMember = setOperations.isMember(OrderCacheKey.zeroOrder(classId), userName);
        if (!setOperationsMember) {
            HashMap<String, Object> map = HashMapBuilder.<String, Object>newBuilder()
                    .put("userName", userName)
                    .put("classId", classId)
                    .build();
            NetSchoolResponse response = orderService.hasGetBigGiftOrder(map);
            Object build = ResponseUtil.build(response);
            Boolean isPresent = ((HashMap<String, Boolean>) build).getOrDefault("isPresent", false);
            if (isPresent) {
                setOperations.add(OrderCacheKey.zeroOrder(classId), userName);
                //7 天缓存失效
                redisTemplate.expire(OrderCacheKey.zeroOrder(classId), 7L, TimeUnit.DAYS);
            }
        }
        return setOperations.isMember(OrderCacheKey.zeroOrder(classId), userName);
    }
}
