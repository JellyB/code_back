package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单来源
 *
 * @author geek-s
 * @date 2019-07-09
 */
@Getter
@AllArgsConstructor
public enum EssayGoodsOrderRefundTypeEnum {

    AUTO("原路返回"),
    OFFLINE("手动退款");

    private String value;

    public static EssayGoodsOrderRefundTypeEnum of(Integer value) {
        for (EssayGoodsOrderRefundTypeEnum goodsOrderRefundTypeEnum : values()) {
            if (goodsOrderRefundTypeEnum.ordinal() == value) {
                return goodsOrderRefundTypeEnum;
            }
        }

        return null;
    }
}
