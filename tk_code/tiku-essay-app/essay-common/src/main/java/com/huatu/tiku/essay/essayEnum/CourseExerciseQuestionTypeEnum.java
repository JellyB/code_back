package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/28
 * @描述
 */
@Getter
@AllArgsConstructor
public enum CourseExerciseQuestionTypeEnum {

    single_question(1,"单题"),
    multi_question(2,"多题"),
    single_paper(3,"套卷");

    private int code;
    private String value;


}
