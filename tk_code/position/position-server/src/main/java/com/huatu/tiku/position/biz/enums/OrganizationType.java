package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrganizationType {

    OUTSIDE("编外"),
    INSIDE("编内");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private OrganizationType(String text) {
        this.value=name();
        this.text = text;
    }
}
