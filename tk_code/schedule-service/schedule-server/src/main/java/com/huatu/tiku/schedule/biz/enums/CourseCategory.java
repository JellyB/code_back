package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * 课程分类
 *
 * @author Geek-S
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CourseCategory {

    LIVE("直播"), XXK("线下课"), VIDEO("录播课"), SSK("双师课"), DMJZ("地面讲座");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    CourseCategory(String text) {
        this.value = name();
        this.text = text;
    }

}
