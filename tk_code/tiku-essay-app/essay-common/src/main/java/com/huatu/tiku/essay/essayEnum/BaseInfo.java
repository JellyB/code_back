package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 系统基础的 常量信息
 * Created by lijun on 2018/8/6
 */
public final class BaseInfo {
    /**
     * 查询时候全部对应的值
     */
    //字符串类型对应的默认值
    public static final String SEARCH_DEFAULT = "-1";
    //数字对应的默认值
    public static final Integer SEARCH_DEFAULT_INTEGER = -1;
    public static final Long SEARCH_DEFAULT_LONG = -1L;


    //String类型对应的
    public static final int SEARCH_DEFAULT_INT_VALUE = Integer.valueOf(SEARCH_DEFAULT);
    //搜索框为空，对应的
    public static final String SEARCH_INPUT_DEFAULT = StringUtils.EMPTY;

    /**
     * 是否是默认值
     */
    public static boolean isNotDefaultSearchValue(Number value) {
        return null != value && !String.valueOf(value).equals(SEARCH_DEFAULT);
    }

    /**
     * 是否是默认值
     */
    public static boolean isNotDefaultSearchValue(String value) {
        return null != value && !value.trim().equals(SEARCH_DEFAULT) && !value.trim().equals(SEARCH_INPUT_DEFAULT);
    }
}
