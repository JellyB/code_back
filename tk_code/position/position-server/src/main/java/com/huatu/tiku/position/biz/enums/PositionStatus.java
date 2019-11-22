package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**职位状态
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PositionStatus {

    WKS("报名未开始"),
    JXZ("报名进行中"),
    YJS("报名已结束");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private PositionStatus(String text) {
        this.value=name();
        this.text = text;
    }

}
