package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author huangqingpeng
 * @title: LabelFlagEnum
 * @description: 批注用户标识枚举
 * @date 2019-07-0914:13
 */
@AllArgsConstructor
@Getter
public enum LabelFlagEnum {
    TEACHING_AND_RESEARCH(1,"教研专用"),
    STUDENT_LOOK(2,"学员查看用"),
    TEACHER_CERTIFICATION(3,"老师审核模版批注专用"),
    Other(-1,"废弃渠道"),
    ;

    private int code;
    private String name;

    public static LabelFlagEnum create(int type) {
        for (LabelFlagEnum value : LabelFlagEnum.values()) {
            if(value.getCode() == type){
                return value;
            }
        }
        return Other;
    }
}
