package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**政治面貌
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Political {

    BX("不限"),
    QZ("群众"),
    DY("中共党员"),
    MZD("民主党派"),
    GQT("共青团员"),
    QT("其他");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private Political(String text) {
        this.value=name();
        this.text = text;
    }
}