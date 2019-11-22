package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Nature {

    PLACE("省考"),
    NATION("国考");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private Nature(String text) {
        this.value=name();
        this.text = text;
    }
}
