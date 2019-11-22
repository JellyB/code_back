package com.huatu.tiku.essay.web.controller.admin.v1;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderListDto;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderSourceEnum;
import com.huatu.tiku.essay.service.EssayGoodsOrderService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderDetailWrapperVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderListVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderPreRefundVO;
import com.huatu.tiku.essay.vo.admin.AdminOptionVO;
import com.huatu.tiku.essay.vo.resp.EssayGoodsOrderInfoForCourseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品订单
 *
 * @author geek-s
 * @date 2019-07-08
 */
@RestController
@RequestMapping("end/v1/goodsOrder")
@Slf4j
public class EssayGoodsOrderControllerV1 {

    @Autowired
    private EssayGoodsOrderService goodsOrderService;

    /**
     * 订单列表
     *
     * @param query    查询条件
     * @param page     页数
     * @param pageSize 条数
     * @return 订单列表
     */
    @GetMapping
    public PageUtil<AdminEssayGoodsOrderListVO> list(AdminEssayGoodsOrderListDto query,
                                                     @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return goodsOrderService.list(query, page, pageSize);
    }

    /**
     * 订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("{id}")
    public AdminEssayGoodsOrderDetailWrapperVO detail(@PathVariable Long id) {
        return goodsOrderService.detail(id);
    }

    /**
     * 预申请退款
     *
     * @param goodsOrderId       订单ID
     * @param goodsOrderDetailId 订单明细ID
     * @return 订单详情
     */
    @GetMapping("preRefund")
    public AdminEssayGoodsOrderPreRefundVO preRefund(Long goodsOrderId, Long goodsOrderDetailId) {
        Assert.notNull(goodsOrderId, "订单ID不能为空");

        return goodsOrderService.preRefund(goodsOrderId, goodsOrderDetailId);
    }

    /**
     * 订单状态字典
     *
     * @return 订单状态字典
     */
    @GetMapping("bizStatusDic")
    public List<AdminOptionVO> bizStatusDic() {
        List<AdminOptionVO> optionVOS = Lists.newArrayListWithExpectedSize(EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.values().length);

        for (EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum goodsOrderBizStatusEnum : EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.values()) {
            optionVOS.add(AdminOptionVO.builder()
                    .text(goodsOrderBizStatusEnum.getDescription())
                    .value(goodsOrderBizStatusEnum.getBizStatus())
                    .build());
        }

        return optionVOS;
    }

    /**
     * PHP课程赠送订单信息
     *
     * @param orderId 订单ID
     * @return 扣除金额
     */
    @GetMapping("infoForCourse")
    public EssayGoodsOrderInfoForCourseVo infoForCourse(Long orderId) {
        Assert.notNull(orderId, "订单ID不能为空");

        return goodsOrderService.infoForCourse(orderId);
    }

    /**
     * 订单来源字典
     *
     * @return 订单来源字典
     */
    @GetMapping("sourceDic")
    public List<AdminOptionVO> sourceDic() {
        List<AdminOptionVO> optionVOS = Lists.newArrayListWithExpectedSize(EssayGoodsOrderSourceEnum.values().length);

        for (EssayGoodsOrderSourceEnum sourceEnum : EssayGoodsOrderSourceEnum.values()) {
            optionVOS.add(AdminOptionVO.builder()
                    .text(sourceEnum.getValue())
                    .value(sourceEnum.ordinal())
                    .build());
        }

        return optionVOS;
    }
}




