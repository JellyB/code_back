package com.huatu.tiku.essay.annotation.api;

import com.huatu.tiku.essay.annotation.entity.CacheBean;

import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/2 16:17
 * @Description
 */
public interface CacheApi {
    String get(String key);
    void set(String key,Object value,int expireMin);
    void set(String key,Object value,int expireMin,String desc);
    Long remove(String key);
    Long remove(String... keys);
    Long removeByPre(String pre);
    List<CacheBean> getBeanByPre(String pre);
    List<CacheBean> listAll();
    Boolean isEnabled();
    String addSys(String key);
}
