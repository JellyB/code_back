package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Map;

/**
 * 学位
 *
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Degree {

    BX("无要求"), XS("学士"), SS("硕士"), BS("博士");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    Degree(String text) {
        this.value = name();
        this.text = text;
    }


    public static Degree findByIndex(Integer index) {
        for (Degree degree : Degree.values()) {
            if (index.equals(degree.ordinal())) {
                return degree;
            }
        }
        return null;
    }

    private static Map<String, String> DEGREE_DIC = new ImmutableMap.Builder<String, String>()
            .put("无要求", "无要求")
            .put("不限", "无要求")
            .put("学士", "学士")
            .put("硕士", "硕士")
            .put("博士", "博士")
            .build();

    public static Degree findByName(String value) {
        value = DEGREE_DIC.get(value);

        for (Degree degree : Degree.values()) {
            if (degree.text.equals(value)) {
                return degree;
            }
        }

        return null;
    }
}
