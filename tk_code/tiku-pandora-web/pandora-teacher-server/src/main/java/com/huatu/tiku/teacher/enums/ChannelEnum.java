package com.huatu.tiku.teacher.enums;


import com.huatu.tiku.enumHelper.EnumUtils;
import com.huatu.tiku.enumHelper.IEnum;

/**
 * Created by duanxiangchao on 2018/5/2
 */
public enum ChannelEnum implements IEnum<Integer> {

    TEACHER(1, "教师"),
    STUDENT(2, "学员");

    private int value;
    private String title;

    public static ChannelEnum create(Integer value) {
        return EnumUtils.getEnum(ChannelEnum.values(), value);
    }

    ChannelEnum(int value, String title) {
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
