package com.huatu.tiku.course.service.cache;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by lijun on 2018/7/5
 */
@Component
public class OrderCacheQPS {

    public static ErrorResult DEFAULT_RESULT = ErrorResult.create(5000001, "哎哟。。人还真多啊，小主快快返回上一级再用力挤挤吧。。。");


    private static final int DEFAULT_QPS = 10;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Integer> valueOperations;

    /**
     * 立即购买按钮
     */
    public void orderPreInfoQPS() {
        String key = OrderCacheKey.orderPrevInfo();
        orderQPS(key);
    }

    /**
     * 立即购买按钮
     */
    public void orderPreInfoQPSRelease() {
        String key = OrderCacheKey.orderPrevInfo();
        orderPreInfoQPSRelease(key);
    }

    /**
     * 提交订单
     */
    public void orderCreateQPS() {
        String key = OrderCacheKey.orderCreate();
        orderQPS(key);
    }

    /**
     * 提交订单
     */
    public void orderCreateQPSRelease() {
        String key = OrderCacheKey.orderCreate();
        orderPreInfoQPSRelease(key);
    }

    /**
     * 订单 qps
     */
    private void orderQPS(String key) {
        Integer num = valueOperations.get(key);
        if (null != num) {
            if (num <= 0) {
                throw new BizException(DEFAULT_RESULT);
            } else {
                valueOperations.increment(key, -1);
            }
        } else {//避免第一次 获取到 空值
            valueOperations.set(key, DEFAULT_QPS);
        }
    }

    /**
     * 释放
     */
    private void orderPreInfoQPSRelease(String key) {
        Integer num = valueOperations.get(key);
        if (num != null) {
            valueOperations.increment(key, 1);
        }
    }

}
