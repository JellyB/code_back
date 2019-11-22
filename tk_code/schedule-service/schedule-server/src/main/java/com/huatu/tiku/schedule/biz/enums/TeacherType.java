package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

/**
 * 教师类型
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TeacherType {

    JS("讲师"), ZJ("助教"), XXS("学习师"), CK("场控"), ZCR("主持人")
    ,SYS("摄影师"),ZKS("质控师");
    /**
     * 值
     */
    private String value;
    /**
     * 显示字体
     */
    private String text;

    private TeacherType(String text) {
        this.value=name();
        this.text = text;
    }

}
