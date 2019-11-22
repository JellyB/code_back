package com.huatu.tiku.teacher.enums;


import com.huatu.tiku.enumHelper.EnumUtils;
import com.huatu.tiku.enumHelper.IEnum;

/**
 *考察方式——暂时保留（作为标签存在，不在作为试题的属性）
 * Created by duanxiangchao on 2018/5/10
 */
public enum InvestigationMethodEnum implements IEnum<Integer> {

    MEMORIZE(1, "识记类"),
    UNDERSTAND(2, "理解类");

    private int value;
    private String title;

    public static InvestigationMethodEnum create(Integer value) {
        return EnumUtils.getEnum(InvestigationMethodEnum.values(), value);
    }

    InvestigationMethodEnum(int value, String title) {
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
