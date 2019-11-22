package com.huatu.tiku.essay.service;

/**
 * PHP金币接口
 *
 * @author geek-s
 * @date 2019-07-08
 */
public interface PHPCoinService {

    /**
     * 退款
     *
     * @param orderId       订单ID
     * @param orderDetailId 订单明细ID
     * @param money         退款金额（金币数）
     */
    void refund(Long orderId, Long orderDetailId, Integer money);
}
