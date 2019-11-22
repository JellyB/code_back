package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * Created by duanxiangchao on 2019/7/9
 */
public enum TeacherStatusEnum implements IEnum<Integer> {

    TRIAL(1, "试用", false),
    ASSESSMENT(2, "考核", false),
    FORMAL(3, "正式", true),
    离职(4, "离职", false);

    private Integer value;
    private String title;
    private Boolean selected;

    private TeacherStatusEnum(Integer value, String title, Boolean selected) {
        this.value = value;
        this.title = title;
        this.selected = selected;
    }

    public static TeacherStatusEnum create(Integer value) {
        return (TeacherStatusEnum) EnumUtils.getEnum(values(), value);
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
