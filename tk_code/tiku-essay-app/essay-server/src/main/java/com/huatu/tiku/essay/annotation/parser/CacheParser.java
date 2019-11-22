package com.huatu.tiku.essay.annotation.parser;

import java.lang.reflect.Type;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/2 16:06
 * @Description
 */
public interface CacheParser {
    /**
     * this is parse
     * @param value
     * @param returnType
     * @param origins
     * @return
     */
    Object parse(String value, Type returnType, Class<?>... origins);
}
