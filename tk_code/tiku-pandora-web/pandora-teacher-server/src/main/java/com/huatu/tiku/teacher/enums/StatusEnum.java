package com.huatu.tiku.teacher.enums;


import com.huatu.tiku.enumHelper.EnumUtils;
import com.huatu.tiku.enumHelper.IEnum;

/**
 * Created by duanxiangchao on 2018/5/3
 */
public enum StatusEnum implements IEnum<Integer> {

    DELETE(-1, "删除"),
    NORMAL(1, "正常");

    private int value;
    private String title;

    public static StatusEnum create(Integer value) {
        return EnumUtils.getEnum(StatusEnum.values(), value);
    }

    StatusEnum(int value, String title) {
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
