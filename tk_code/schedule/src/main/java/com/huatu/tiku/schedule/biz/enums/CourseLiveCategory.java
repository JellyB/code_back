package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**面试的直播分类
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CourseLiveCategory {

    SK("授课"),LX("练习");
    /**
     * 值
     */
    private String value;
    /**
     * 显示字体
     */
    private String text;

    private CourseLiveCategory(String text) {
        this.value=name();
        this.text = text;
    }
}
