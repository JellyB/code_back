package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/18
 * @描述 批改订单退回学员原因
 */
@AllArgsConstructor
@Getter
public enum CorrectReturnReasonEnum implements IEnum<Integer> {

    ONE(1, "字迹潦草"),

    TWO(2, "答非所问"),

    THREE(3, "作废该题");

    private int value;
    private String title;

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String getTitle() {
        return this.title;
    }
    
    public static CorrectReturnReasonEnum create(Integer value) {
        return (CorrectReturnReasonEnum)EnumUtils.getEnum(values(), value);
    }
}
