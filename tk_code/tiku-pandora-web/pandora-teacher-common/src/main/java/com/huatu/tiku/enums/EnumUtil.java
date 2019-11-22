package com.huatu.tiku.enums;

import com.huatu.common.utils.collection.HashMapBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/8/6
 * 枚举工具 建议使用 org.apache.commons.lang3.EnumUtils
 */
public class EnumUtil {

    /**
     * 转换过成List
     */
    public static <T extends EnumCommon> List<HashMap<String, Object>> asList(Class<T> clazz) {
        return Arrays.stream(clazz.getEnumConstants())
                .map(constant ->
                        HashMapBuilder.<String, Object>newBuilder()
                                .put("key", constant.getKey())
                                .put("value", constant.getValue())
                                .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * 枚举 code 转换成 名称
     *
     * @param code  code
     * @param clazz 枚举实体
     * @return 名称 默认值 StringUtils.EMPTY
     */
    public static <T extends EnumCommon> String valueOf(int code, Class<T> clazz) {
        Optional<String> optional = Arrays.stream(clazz.getEnumConstants())
                .filter(constant -> constant.getKey() == code)
                .map(T::getValue)
                .findAny();
        return optional.orElseGet(() -> StringUtils.EMPTY);
    }

    /**
     * 枚举 名称 转换成 code
     *
     * @param value 名称
     * @param clazz 枚举实体
     * @return code 默认值 NumberUtils.INTEGER_MINUS_ONE
     */
    public static <T extends EnumCommon> int valueOf(String value, Class<T> clazz) {
        Optional<Integer> optional = Arrays.stream(clazz.getEnumConstants())
                .filter(constant -> constant.getValue().equals(value))
                .map(T::getKey)
                .findAny();
        return optional.orElseGet(() -> NumberUtils.INTEGER_MINUS_ONE);
    }
}
