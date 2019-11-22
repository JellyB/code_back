package com.huatu.tiku.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by huangqingpeng on 2018/8/8.
 */
public enum SubjectInfoEnum {
    ;

    @AllArgsConstructor
    @Getter
    public enum SubjectTypeEnum implements EnumCommon {

        CATEGORY(1, "考试类型"),
        SUBJECT(2, "学科"),
        GRADE(3, "学段"),;

        private int code;
        private String name;

        @Override
        public int getKey() {
            return this.getCode();
        }

        @Override
        public String getValue() {
            return this.getName();
        }

    }


    @AllArgsConstructor
    @Getter
    public enum SubjectLevel implements EnumCommon {

        LEVEL_ONE(1, "科目类别"),
        LEVEL_TWO(2, "科目");

        private int code;
        private String name;

        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.getName();
        }
    }


    @AllArgsConstructor
    @Getter
    public enum TeacherSubjectEnum implements EnumCommon {

        teacher_zhongxue(200100053, "教师资格证-中学"),
        teacher_xiaoxue(200100048, "教师资格证-小学");

        private int code;
        private String name;

        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.getName();
        }
    }


}
