package com.huatu.tiku.essay.essayEnum;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * Created by duanxiangchao on 2019/7/15
 */
public enum  YesNoEnum implements IEnum<Integer> {

    NO(0, "否"),
    YES(1, "是");

    private int value;
    private String title;

    public static YesNoEnum create(Integer value) {
        return (YesNoEnum)EnumUtils.getEnum(values(), value);
    }

    private YesNoEnum(int value, String title) {
        this.value = value;
        this.title = title;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

    public String toString() {
        return EnumUtils.toJSONString(this, new SerializerFeature[0]);
    }

    public boolean isYes() {
        return this.value == YES.getValue();
    }

}
