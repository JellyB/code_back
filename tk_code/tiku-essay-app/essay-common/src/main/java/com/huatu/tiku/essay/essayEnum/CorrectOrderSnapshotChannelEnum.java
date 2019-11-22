package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * 订单记录操作人员类型枚举
 *
 * @author zhangchong
 */
public enum CorrectOrderSnapshotChannelEnum implements IEnum<Integer> {

    SYSTEM(0, "后台"), ADMIN(1, "管理员"), TEACHER(2, "老师"), MEMBER(3, "学员");

    private int value;
    private String title;

    public static CorrectOrderSnapshotChannelEnum create(Integer value) {
        return (CorrectOrderSnapshotChannelEnum) EnumUtils.getEnum(values(), value);
    }

    private CorrectOrderSnapshotChannelEnum(int value, String title) {
        this.value = value;
        this.title = title;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

}
