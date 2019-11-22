package com.huatu.tiku.position.biz.listener;

import com.huatu.tiku.position.biz.constant.JetCacheConstant;
import com.huatu.tiku.position.biz.service.CachePurgeService;
import com.huatu.tiku.position.biz.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
public class JetCachePurgeListener implements MessageListener {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CachePurgeService purgeCacheService;

    @Autowired
    private PositionService positionService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("receives message :" + message.toString());

        String value[] = message.toString().split(":");

        if (JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_NAME.equals(value[0])) {
            Set<String> keys = stringRedisTemplate.opsForSet().members(JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_NAME);

            if (!keys.isEmpty()) {
                keys.forEach(key -> {
                    purgeCacheService.purgePositionFindPosition(key);
                });

                stringRedisTemplate.delete(JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_NAME);
            }
        } else if (JetCacheConstant.AREA_CONTROLLER_FIND_BY_PARENT_ID_NAME.equals(value[0])) {
            purgeCacheService.purgeFindByParentId(value[1]);
        } else if (JetCacheConstant.POSITION_SERVICE_FIND_POSITION_1_NAME.equals(value[0])) {
            positionService.purgePositionCache();
        }
    }
}