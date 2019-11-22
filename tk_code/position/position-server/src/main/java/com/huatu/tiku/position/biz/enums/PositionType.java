package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**职位类型
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PositionType {

    GWY("公务员"),
    SYDW("事业单位"),
    JS("教师");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private PositionType(String text) {
        this.value=name();
        this.text = text;
    }
}
