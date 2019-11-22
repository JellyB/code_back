package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款去向
 *
 * @author geek-s
 * @date 2019-07-18
 */
@Getter
@AllArgsConstructor
public enum EssayGoodsOrderRefundDestEnum {

    USER("退给学员");

    private String value;

    public static EssayGoodsOrderRefundDestEnum of(Integer value) {
        for (EssayGoodsOrderRefundDestEnum goodsOrderRefundDestEnum : values()) {
            if (goodsOrderRefundDestEnum.ordinal() == value) {
                return goodsOrderRefundDestEnum;
            }
        }

        return null;
    }
}
