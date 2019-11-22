package com.huatu.tiku.position.biz.service;

public interface CachePurgeService {

    /**
     * 清空职位列表缓存
     *
     * @param key Key
     */
    void purgePositionFindPosition(String key);

    /**
     * 清空地区缓存
     *
     * @param key Key
     */
    void purgeFindByParentId(String key);

    /**
     * 清空职位列表缓存
     *
     * @param key Key
     */
    void purgePositionFindPosition1(String key);
}
