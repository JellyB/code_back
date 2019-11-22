package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;

/**
 * Created by duanxiangchao on 2019/7/29
 */
public enum SettlementStatusEnum implements IEnum<Integer> {

    WAIT_SETTLEMENT(0, "未结算"),
    SETTLEMENT_FINISH(1, "已结算");

    private Integer value;
    private String title;

    private SettlementStatusEnum(Integer value, String title) {
        this.value = value;
        this.title = title;
    }

    public static SettlementStatusEnum create(Integer value) {
        return (SettlementStatusEnum)EnumUtils.getEnum(values(), value);
    }

    public Integer getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

}
