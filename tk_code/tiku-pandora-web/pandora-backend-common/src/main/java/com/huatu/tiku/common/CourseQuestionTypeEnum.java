package com.huatu.tiku.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 课绑题相关枚举
 */
public enum CourseQuestionTypeEnum {
    ;

    /**
     * 课绑题 类型
     */
    @AllArgsConstructor
    @Getter
    public enum CourseQuestionType {
        RECORD_COURSE_EXERCISE(1, "录播课后练习"),
        LIVE_BREAK_POINT_QUESTION(2, "直播课随堂练习"),
        RECORD_BREAK_POINT_QUESTION(3, "录播课随堂练习"),
        LIVE_COURSE_EXERCISE(4, "直播课后练习");

        private int code;
        private String name;
    }


    /**
     * 课程类型
     */
    @AllArgsConstructor
    @Getter
    public enum CourseType {
        RECORD(1, "录播课"),
        LIVE(2, "直播课");
        private int code;
        private String name;
    }

    /**
     * 课程类型转换
     */
    @AllArgsConstructor
    @Getter
    public enum CourseTypeConvert {

        RECORD(1, "录播课", 0),
        LIVE(2, "直播课", 1);

        private int code;
        private String name;
        private int phpCode;



        public static int getCodeByPHPCode(int code) {
            Optional<CourseQuestionTypeEnum.CourseTypeConvert> any = Stream.of(CourseQuestionTypeEnum.CourseTypeConvert.values()).filter(category -> category.getPhpCode() == code).findAny();
            if (any.isPresent()) {
                return any.get().getCode();
            }
            return -1;
        }


    }



}
