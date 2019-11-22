package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**性别
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Sex {

    BX("不限"),MAN("男"),WOMAN("女");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private Sex(String text) {
        this.value=name();
        this.text = text;
    }
}
