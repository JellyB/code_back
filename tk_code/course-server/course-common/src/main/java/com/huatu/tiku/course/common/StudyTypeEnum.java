package com.huatu.tiku.course.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-02-15 下午5:08
 **/
@AllArgsConstructor
@Getter
public enum  StudyTypeEnum {

    COURSE_WORK("courseWork", "课后作业", 1),
    PERIOD_TEST("periodTest", "阶段考试", 2);
    private String key;
    private String name;
    private int order;

    public static StudyTypeEnum create (String key){
        for(StudyTypeEnum studyTypeEnum : values()){
            if(studyTypeEnum.getKey().equals(key)){
                return studyTypeEnum;
            }
        }
        return null;
    }
}
