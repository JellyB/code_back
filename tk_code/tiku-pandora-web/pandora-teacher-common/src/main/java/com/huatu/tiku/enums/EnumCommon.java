package com.huatu.tiku.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by lijun on 2018/8/6
 */
public interface EnumCommon {

    /**
     * key
     */
    int getKey();

    /**
     * value
     */
    String getValue();

    /**
     * 描述
     */
    default String desc() {
        return StringUtils.EMPTY;
    }


    /**
     * 判断 key 是否相等
     * 相等 返回 true
     */
    default boolean is(int key) {
        return getKey() == key;
    }

    /**
     * 判断 key 是否相等
     *
     * @param key 待比价值
     * @return 不相等 返回true
     */
    default boolean not(int key) {
        return !is(key);
    }

}
