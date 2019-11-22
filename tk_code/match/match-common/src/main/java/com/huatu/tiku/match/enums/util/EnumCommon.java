package com.huatu.tiku.match.enums.util;


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
     * 默认值
     */
    default <T extends EnumCommon> T getDefault() {
        return null;
    }

    /**
     * 描述
     */
    default String desc() {
        return "";
    }

    /**
     * 判断 key 是否相等
     *
     * @return 相等 返回 true
     */
    default boolean valueEquals(int key) {
        return getKey() == key;
    }

    /**
     * 判断 key 是否相等
     *
     * @return 不相等 返回true
     */
    default boolean valueNotEquals(int key) {
        return !valueEquals(key);
    }

}
