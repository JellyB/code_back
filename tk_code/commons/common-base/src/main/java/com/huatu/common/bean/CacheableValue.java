package com.huatu.common.bean;

/**
 * 这个作用是为了返回缓存里的数据时候增加两个属性，是否缓存，为了线上排查问题更方便
 * @author hanchao
 * @date 2017/9/29 15:15
 */
public class CacheableValue extends BaseValue {
    private boolean _cache;
    private long _cacheTimestamp;

    public boolean isCache() {
        return _cache;
    }

    public void setCache(boolean cache) {
        this._cache = cache;
    }

    public long getCacheTimestamp() {
        return _cacheTimestamp;
    }

    public void setCacheTimestamp(long cacheTimestamp) {
        this._cacheTimestamp = cacheTimestamp;
    }
}
