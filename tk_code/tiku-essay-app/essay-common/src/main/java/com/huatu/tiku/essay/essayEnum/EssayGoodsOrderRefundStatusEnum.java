package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单退款审核状态
 *
 * @author geek-s
 * @date 2019-07-09
 */
@Getter
@AllArgsConstructor
public enum EssayGoodsOrderRefundStatusEnum {

    TODO("待审核"),
    PASS1("一级已通过"),
    DENY1("一级已拒绝"),
    PASS2("二级已通过"),
    DENY2("二级已拒绝");

    private String value;
}
