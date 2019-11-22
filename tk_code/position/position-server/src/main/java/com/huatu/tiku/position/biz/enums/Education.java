package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Map;

/**
 * 学历
 *
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Education {

    ZK("专科"), BK("本科"), SS("硕士研究生"), BS("博士研究生");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    Education(String text) {
        this.value = name();
        this.text = text;
    }

    private static Map<String, String> EDUCATION_DIC = new ImmutableMap.Builder<String, String>()
            .put("高中（中专）", "专科")
            .put("专科", "专科")
            .put("大专", "专科")
            .put("本科", "本科")
            .put("硕士研究生", "硕士研究生")
            .put("硕研", "硕士研究生")
            .put("博士研究生", "博士研究生")
            .put("博研", "博士研究生")
            .build();

    public static Education findByName(String value) {
        value = EDUCATION_DIC.get(value);

        for (Education education : Education.values()) {
            if (education.text.equals(value)) {
                return education;
            }
        }

        return null;
    }
}
