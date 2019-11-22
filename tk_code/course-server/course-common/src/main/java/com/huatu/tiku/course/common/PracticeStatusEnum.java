package com.huatu.tiku.course.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-25 10:46 AM
 **/
@AllArgsConstructor
@Getter
public enum PracticeStatusEnum {

    NONE(0, "没有"),
    AVAILABLE(1 , "可以获取查看"),
    MISSED_OR_UNFINISHED(2, "错过或未完成");

    private int code;
    private String value;

}
