package com.huatu.tiku.course.common;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-09-07 11:07 PM
 **/

@NoArgsConstructor
@Getter
public enum  BuildTypeEnum {


    SINGLE_QUESTION(0 , "单题"),

    PAPER(1, "套题");
    private int type;
    private String text;

    BuildTypeEnum(int type, String text) {
        this.type = type;
        this.text = text;
    }
}
