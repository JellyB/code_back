package com.huatu.tiku.position.biz.service.impl;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.huatu.tiku.position.biz.constant.JetCacheConstant;
import com.huatu.tiku.position.biz.service.CachePurgeService;
import org.springframework.stereotype.Service;

@Service
public class CachePurgeServiceImpl implements CachePurgeService {

    @CacheInvalidate(name = JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_NAME, key = "#key")
    public void purgePositionFindPosition(String key) {
    }

    @CacheInvalidate(name = JetCacheConstant.AREA_CONTROLLER_FIND_BY_PARENT_ID_NAME, key = "#key")
    public void purgeFindByParentId(String key) {
    }

    @CacheInvalidate(name = JetCacheConstant.POSITION_SERVICE_FIND_POSITION_1_NAME, key = "#key")
    public void purgePositionFindPosition1(String key) {
    }
}
