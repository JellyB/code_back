package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.WeChatPay;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by x6 on 2017/12/5.
 */
public interface WeChatPayRepository extends JpaRepository<WeChatPay, Long> {

    /**
     * 根据订单ID查询支付回调信息
     *
     * @param orderId 订单ID
     * @return 支付回调信息
     */
    WeChatPay findByOrderId(Long orderId);
}
