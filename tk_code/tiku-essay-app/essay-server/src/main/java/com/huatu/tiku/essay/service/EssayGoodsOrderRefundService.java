package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderRefundDto;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderRefundListDto;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderRefundStatusEnum;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderRefundListVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderRefundRecordVO;

import java.util.List;

/**
 * 商品订单退款记录
 *
 * @author geek-s
 * @date 2019-07-08
 */
public interface EssayGoodsOrderRefundService {

    /**
     * 申请退款
     *
     * @param goodsOrderRefundDto 退款信息
     */
    void refund(AdminEssayGoodsOrderRefundDto goodsOrderRefundDto);

    /**
     * 列表
     *
     * @param query    查询条件
     * @param page     页数
     * @param pageSize 条数
     * @return 订单列表
     */
    PageUtil<AdminEssayGoodsOrderRefundListVO> list(AdminEssayGoodsOrderRefundListDto query, Integer page, Integer pageSize);

    /**
     * 审核
     *
     * @param id           记录ID
     * @param remark       备注
     * @param currStatus   当前状态
     * @param targetStatus 目标状态
     */
    void audit(Long id, String remark, EssayGoodsOrderRefundStatusEnum currStatus, EssayGoodsOrderRefundStatusEnum targetStatus);

    /**
     * 订单退款记录
     *
     * @param orderId 订单ID
     * @return 退款记录
     */
    List<AdminEssayGoodsOrderRefundRecordVO> listByOrderId(Long orderId);

    /**
     * PHP赠送批改退款
     *
     * @param courseOrderId 课程订单ID
     * @param userName      用户名
     */
    void preRefundByCourseOrderId(Long courseOrderId, String userName);

    /**
     * PHP赠送批改退款
     *
     * @param courseOrderId 课程订单ID
     * @param userName      用户名
     */
    void refundByCourseOrderId(Long courseOrderId, String userName);

    /**
     * PHP赠送批改退款
     *
     * @param courseOrderId 课程订单ID
     * @param userName      用户名
     */
    void cancelRefundByCourseOrderId(Long courseOrderId, String userName);
}
