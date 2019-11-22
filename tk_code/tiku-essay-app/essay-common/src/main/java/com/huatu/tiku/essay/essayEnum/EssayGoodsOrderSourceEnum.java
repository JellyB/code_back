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
public enum EssayGoodsOrderSourceEnum {

    APP("APP下单"),
    ONLINE_COURSE("课程赠送"),
    OFFLINE_COURSE("批改充值");

    private String value;
}
