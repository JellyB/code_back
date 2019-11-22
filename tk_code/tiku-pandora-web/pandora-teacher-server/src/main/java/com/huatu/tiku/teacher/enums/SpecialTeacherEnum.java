package com.huatu.tiku.teacher.enums;

import com.huatu.tiku.enums.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/22
 * @描述
 */
@AllArgsConstructor
@Getter
public enum SpecialTeacherEnum implements EnumCommon {

    SPECIAL(1,"特岗教师"),
    NOT_SPECIAL(0, "非特岗教师");
    private int key;
    private String value;

    @Override
    public int getKey() {
        return this.key;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
