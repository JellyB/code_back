package com.huatu.ztk.paper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by huangqingpeng on 2019/3/19.
 */
@Getter
@AllArgsConstructor
public enum  CoursePaperType {
    COURSE_BREAKPOINT(1,"课中练习"),
    COURSE_EXERCISE(2,"课后练习"),
    ;
    private int code;
    private String value;

    public static CoursePaperType create(int type){
        for (CoursePaperType coursePaperType : CoursePaperType.values()) {
            if(coursePaperType.getCode() == type){
                return coursePaperType;
            }
        }
        return null;
    }
}
