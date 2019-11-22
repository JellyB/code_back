package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**学校类型
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SchoolType {

    PRIVATE("民办"),
    PUBLIC("公办");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private SchoolType(String text) {
        this.value=name();
        this.text = text;
    }
}
