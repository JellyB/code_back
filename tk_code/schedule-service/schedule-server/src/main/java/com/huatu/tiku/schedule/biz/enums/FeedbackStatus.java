package com.huatu.tiku.schedule.biz.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**课时反馈审核状态
 * @author wangjian
 **/
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FeedbackStatus {

    DSH("待审核"),
//    TWO("二次反馈待审核"),
    WTG("审核未通过"),
//    TWOWTG("二次反馈未通过"),
    YSH("已审核"),
//    WC("反馈完成")
    ;

    /**
     * 值
     */
    private String value;

    /**
     * 显示字体
     */
    private String text;

    FeedbackStatus(String text) {
        this.value=name();
        this.text = text;
    }

}
