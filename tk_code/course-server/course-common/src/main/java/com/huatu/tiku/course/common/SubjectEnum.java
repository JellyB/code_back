package com.huatu.tiku.course.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-27 4:27 PM
 **/

@Getter
@AllArgsConstructor
public enum SubjectEnum {

    XC(1, "行测"),
    SL(2, "申论");

    public static SubjectEnum create(int type){
        for (SubjectEnum subjectEnum : SubjectEnum.values()) {
            if(subjectEnum.getCode() == type){
                return subjectEnum;
            }
        }
        return null;
    }

    private int code;

    private String text;
}
