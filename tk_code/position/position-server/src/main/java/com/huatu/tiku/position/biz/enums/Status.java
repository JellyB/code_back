package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**状态
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Status {

    ZC("正常"),
    JY("禁用");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private Status(String text) {
        this.value=name();
        this.text = text;
    }

}
