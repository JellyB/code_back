package com.huatu.tiku.course.util;

import com.google.common.collect.ImmutableMap;
import com.huatu.common.Result;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * java 系统中统一Http 请求返回结果集解析
 * Created by lijun on 2018/5/22
 */
@Slf4j
@Builder
public class HBaseApiResponseUtil {

    public static final Map<String, Object> EMPTY = ImmutableMap.<String, Object>builder().build();

    /**
     * 解析HbaseApi 返回结果
     *
     * @param o
     * @return
     */
    public static Object buildResult(Object o) {
        if (o == null) {
            return EMPTY;
        }
        HashMap<String, Object> result = (HashMap<String, Object>) o;
        if (isFailure(result)) {
            return EMPTY;
        }
        return (HashMap) result.get("data");
    }

    /**
     * 是否失败的响应
     *
     * @param result
     * @return
     */
    public static boolean isFailure(HashMap result) {
        return null == result.get("code")
                || Integer.valueOf(String.valueOf(result.get("code"))) != Result.SUCCESS_CODE
                ;
    }

}
