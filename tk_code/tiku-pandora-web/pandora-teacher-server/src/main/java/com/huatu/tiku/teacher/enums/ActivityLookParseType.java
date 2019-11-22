package com.huatu.tiku.teacher.enums;

import com.huatu.tiku.enums.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/14
 * @描述
 */
@AllArgsConstructor
@Getter
public enum ActivityLookParseType implements EnumCommon {

    HAND_EXAM_PAPER_LOOK(1, "交卷后立即查看"),
    EXAM_END_LOOK(2, "考试后立即查看");

    private Integer code;
    private String value;

    @Override
    public int getKey() {
        return this.code;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
