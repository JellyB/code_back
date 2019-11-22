package com.huatu.tiku.teacher.enums;


import com.huatu.tiku.enumHelper.EnumUtils;
import com.huatu.tiku.enumHelper.IEnum;

/**
 * Created by duanxiangchao on 2018/5/10
 */
public enum ExamTypeEnum implements IEnum<Integer> {

    ORDINARY_TEACHING(1, "普通招教"),
    SPECIAL_TEACHER(2, "特岗教师"),
    INSTITUTION_D_TYPE(3, "事业单位D类"),
    TEACHER_CERTIFICATION(4, "教师资格证");

    private int value;
    private String title;

    public static ExamTypeEnum create(Integer value) {
        return EnumUtils.getEnum(ExamTypeEnum.values(), value);
    }

    ExamTypeEnum(int value, String title) {
        this.value = value;
        this.title = title;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    public String toString() {
        return EnumUtils.toJSONString(this);
    }

}
