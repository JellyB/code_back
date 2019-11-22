package com.huatu.tiku.teacher.enums;

import com.huatu.tiku.enums.EnumCommon;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/9/21
 * @描述 特殊科目信息存储
 */
@AllArgsConstructor
@NoArgsConstructor
public enum SubjectEnum implements EnumCommon {

    PUBLIC_FOUNDATION(2, "公基"),
    COMPREHENSIVE_APPLICATION(24, "综合应用");
    public int key;
    public String value;

    @Override
    public int getKey() {
        return this.key;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public static Boolean isExist(int code) {
        SubjectEnum[] subjectEnums = SubjectEnum.values();
        List<Integer> subjectKeyList = Arrays.stream(subjectEnums)
                .map(subjectEnum -> subjectEnum.getKey())
                .collect(Collectors.toList());
        if (subjectKeyList.contains(code)) {
            return true;
        }
        return false;
    }
}
