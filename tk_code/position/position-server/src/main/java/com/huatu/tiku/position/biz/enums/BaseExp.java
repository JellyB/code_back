package com.huatu.tiku.position.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**服务基层工作经历
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum BaseExp {

    BX("无限制"),
    W("无基层项目工作经历"),
    CG("村官"),
    XB("大学生志愿服务西部"),
    TG("教师特设岗位计划"),
    SZYF("‘三支一扶’计划");

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    private BaseExp(String text) {
        this.value=name();
        this.text = text;
    }
}
