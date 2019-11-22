package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderListDto;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderDetailWrapperVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderListVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderPreRefundVO;
import com.huatu.tiku.essay.vo.resp.EssayGoodsOrderInfoForCourseVo;

/**
 * 商品订单
 *
 * @author geek-s
 * @date 2019-07-08
 */
public interface EssayGoodsOrderService {

    /**
     * 列表
     *
     * @param query    查询条件
     * @param page     页数
     * @param pageSize 条数
     * @return 订单列表
     */
    PageUtil<AdminEssayGoodsOrderListVO> list(AdminEssayGoodsOrderListDto query, Integer page, Integer pageSize);

    /**
     * 详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    AdminEssayGoodsOrderDetailWrapperVO detail(Long id);

    /**
     * 预申请退款
     *
     * @param orderId       订单ID
     * @param orderDetailId 订单明细ID
     * @return 退款信息
     */
    AdminEssayGoodsOrderPreRefundVO preRefund(Long orderId, Long orderDetailId);

    /**
     * PHP课程赠送订单信息
     *
     * @param courseOrderId 课程订单ID
     * @return 订单信息
     */
    EssayGoodsOrderInfoForCourseVo infoForCourse(Long courseOrderId);
}
