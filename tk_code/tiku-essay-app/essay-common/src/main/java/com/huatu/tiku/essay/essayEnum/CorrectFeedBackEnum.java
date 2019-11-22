package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/11
 * @描述
 */
@AllArgsConstructor
@Getter
public enum CorrectFeedBackEnum {

    YES(1, "已经反馈"),
    NO(0, "尚未反馈");

    private int code;
    private String name;
}
