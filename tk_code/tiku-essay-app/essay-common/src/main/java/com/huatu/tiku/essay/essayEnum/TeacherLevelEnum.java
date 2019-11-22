package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * Created by duanxiangchao on 2019/7/9
 */
public enum TeacherLevelEnum implements IEnum<Integer> {


    FAMOUS_TEACHER(1, "名师", true),
    VIP(2, "VIP总监", false),
    CHIEF_INSPECTOR(3, "总监", false),
    SENIOR(4, "高级", false);

    private Integer value;
    private String title;
    private Boolean selected;

    private TeacherLevelEnum(Integer value, String title, Boolean selected) {
        this.value = value;
        this.title = title;
        this.selected = selected;
    }

    public static TeacherLevelEnum create(Integer value) {
        return (TeacherLevelEnum) EnumUtils.getEnum(values(), value);
    }

    public Integer getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

    public Boolean getSelected() {
        return this.selected;
    }

}
