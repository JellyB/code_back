package com.huatu.tiku.match.enums.util;

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
                .map(constant -> {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("key", constant.getKey());
                            map.put("value", constant.getValue());
                            return map;
                        }
                )
                .collect(Collectors.toList());
    }

    /**
     * 枚举 code 转换成 名称
     *
     * @param code  code
     * @param clazz 枚举实体
     * @return 名称 默认值 getDefault 对应值，若不存在返回 StringUtils.EMPTY
     */
    public static <T extends EnumCommon> String valueOf(int code, Class<T> clazz) {
        Optional<String> optional = Arrays.stream(clazz.getEnumConstants())
                .filter(constant -> constant.getKey() == code)
                .map(T::getValue)
                .findAny();
        return optional.orElseGet(() -> {
            EnumCommon defaultValue = getDefaultValue(clazz);
            if (null != defaultValue) {
                return defaultValue.getValue();
            }
            return StringUtils.EMPTY;
        });
    }

    /**
     * 枚举 名称 转换成 code
     *
     * @param value 名称
     * @param clazz 枚举实体
     * @return code 默认值 getDefault 对应值，若不存在返回 NumberUtils.INTEGER_MINUS_ONE
     */
    public static <T extends EnumCommon> int valueOf(String value, Class<T> clazz) {
        Optional<Integer> optional = Arrays.stream(clazz.getEnumConstants())
                .filter(constant -> constant.getValue().equals(value))
                .map(T::getKey)
                .findAny();
        return optional.orElseGet(() -> {
            EnumCommon defaultValue = getDefaultValue(clazz);
            if (null != defaultValue) {
                return defaultValue.getKey();
            }
            return NumberUtils.INTEGER_MINUS_ONE;
        });
    }

    /**
     * 返回一个 enum
     *
     * @param code  值
     * @param clazz 枚举实体
     * @return 返回与value相同的枚举
     */
    public static <T extends EnumCommon> T create(int code, Class<T> clazz) {
        Optional<T> optional = Arrays.stream(clazz.getEnumConstants())
                .filter(constant -> constant.getKey() == code)
                .findAny();
        if (optional.isPresent()) {
            return optional.get();
        }
        return getDefaultValue(clazz);
    }

    /**
     * 返回一个 enum
     *
     * @param value 名称
     * @param clazz 枚举实体
     * @return 返回与value相同的枚举
     */
    public static <T extends EnumCommon> EnumCommon create(String value, Class<T> clazz) {
        Optional<T> optional = Arrays.stream(clazz.getEnumConstants())
                .filter(constant -> constant.getValue().equals(value))
                .findAny();
        if (optional.isPresent()) {
            return optional.get();
        }
        return getDefaultValue(clazz);
    }

    private static <T extends EnumCommon> T getDefaultValue(Class<T> clazz) {
        Optional<Object> defaultValueOptional = Arrays.stream(clazz.getEnumConstants())
                .map(T::getDefault)
                .findAny();
        if (defaultValueOptional.isPresent()) {
            return (T) defaultValueOptional.get();
        }
        return null;
    }
}
