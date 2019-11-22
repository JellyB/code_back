package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayGoodsOrderRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 商品订单退款
 *
 * @author geek-s
 * @date 2019-07-09
 */
public interface EssayGoodsOrderRefundRepository extends JpaRepository<EssayGoodsOrderRefund, Long>, JpaSpecificationExecutor<EssayGoodsOrderRefund> {

    /**
     * 根据订单ID查询退款记录
     *
     * @param orderId 订单ID
     * @return 退款记录
     */
    List<EssayGoodsOrderRefund> findByGoodsOrderId(Long orderId);

    /**
     * 根据订单ID查询退款记录
     *
     * @param orderId 订单ID
     * @return 退款记录
     */
    EssayGoodsOrderRefund findTop1ByGoodsOrderIdOrderByIdDesc(Long orderId);
}
