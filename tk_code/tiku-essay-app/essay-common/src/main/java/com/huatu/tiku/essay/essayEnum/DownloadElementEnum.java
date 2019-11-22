package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/28
 * @描述
 */
@AllArgsConstructor
@Getter
public enum DownloadElementEnum {

    QUESTION_REMARK(1, "【本题阅卷】"),
    QUESTION_DE_REMARK(2, "【扣分项】"),
    USER_ANSWER(3, "【我的作答】"),
    PAPER_REMARK(4, "【综合评价】");
    private Integer code;
    private String value;


}
