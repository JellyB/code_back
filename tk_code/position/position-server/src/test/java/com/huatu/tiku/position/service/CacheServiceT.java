package com.huatu.tiku.position.service;

import com.huatu.tiku.position.PositionServerApplicationTest;
import com.huatu.tiku.position.biz.constant.JetCacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
public class CacheServiceT extends PositionServerApplicationTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void purgePositionFindPosition() {
        redisTemplate.convertAndSend(JetCacheConstant.PURGE_CACHE_CHANNEL, JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_NAME);
    }

    @Test
    public void purgeFindByParentId() {
        redisTemplate.convertAndSend(JetCacheConstant.PURGE_CACHE_CHANNEL,
                JetCacheConstant.AREA_CONTROLLER_FIND_BY_PARENT_ID_NAME + ":" + ".areaId." + "-1" + ".noLimit." + "true");
    }
}
