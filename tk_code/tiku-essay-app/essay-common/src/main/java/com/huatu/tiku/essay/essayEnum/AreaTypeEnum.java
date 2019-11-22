package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * Created by duanxiangchao on 2019/7/17
 */
public enum  AreaTypeEnum implements IEnum<Integer> {

    PROVINCE(1, "省"),
    CITY(2, "市"),
    COUNTRY(3, "县");

    private Integer value;
    private String title;

    private AreaTypeEnum(Integer value, String title) {
        this.value = value;
        this.title = title;
    }

    public static AreaTypeEnum create(Integer value) {
        return (AreaTypeEnum)EnumUtils.getEnum(values(), value);
    }

    public Integer getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

}
