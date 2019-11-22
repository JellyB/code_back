package com.huatu.tiku.essay.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderListDto;
import com.huatu.tiku.essay.entity.EssayGoodsOrder;
import com.huatu.tiku.essay.entity.EssayGoodsOrderDetail;
import com.huatu.tiku.essay.repository.EssayGoodsOrderDetailRepository;
import com.huatu.tiku.essay.repository.EssayGoodsOrderRepository;
import com.huatu.tiku.essay.service.EssayGoodsOrderService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderDetailVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderDetailWrapperVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderListVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderPreRefundVO;
import com.huatu.tiku.essay.vo.resp.EssayGoodsOrderInfoForCourseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class EssayGoodsOrderServiceImpl implements EssayGoodsOrderService {

    @Autowired
    private EssayGoodsOrderRepository goodsOrderRepository;

    @Autowired
    private EssayGoodsOrderDetailRepository goodsOrderDetailRepository;

    @Override
    public PageUtil<AdminEssayGoodsOrderListVO> list(AdminEssayGoodsOrderListDto goodsOrderListDto, Integer page, Integer pageSize) {
        Page<EssayGoodsOrder> goodsOrders = goodsOrderRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = Lists.newArrayList();

            if (goodsOrderListDto.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), goodsOrderListDto.getUserId()));
            }
            if (goodsOrderListDto.getGmtCreateBegin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("gmtCreate"), goodsOrderListDto.getGmtCreateBegin()));
            }
            if (goodsOrderListDto.getGmtCreateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("gmtCreate"), goodsOrderListDto.getGmtCreateEnd()));
            }
            if (goodsOrderListDto.getPayTimeBegin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("payTime"), goodsOrderListDto.getPayTimeBegin()));
            }
            if (goodsOrderListDto.getPayTimeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("payTime"), goodsOrderListDto.getPayTimeEnd()));
            }
            if (goodsOrderListDto.getBizStatus() != null) {
                predicates.add(cb.equal(root.get("bizStatus"), goodsOrderListDto.getBizStatus()));
            }
            if (!Strings.isNullOrEmpty(goodsOrderListDto.getOrderNumStr())) {
                predicates.add(cb.equal(root.get("orderNumStr"), goodsOrderListDto.getOrderNumStr()));
            }
            if (!Strings.isNullOrEmpty(goodsOrderListDto.getMobile())) {
                predicates.add(cb.equal(root.get("mobile"), goodsOrderListDto.getMobile()));
            }
            if (!Strings.isNullOrEmpty(goodsOrderListDto.getName())) {
                predicates.add(cb.equal(root.get("name"), goodsOrderListDto.getName()));
            }

            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        }, new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "id"));

        List<AdminEssayGoodsOrderListVO> goodsOrderListVOS = Lists.newArrayList();

        goodsOrders.getContent().forEach(goodsOrder -> {
            AdminEssayGoodsOrderListVO goodsOrderListVO = new AdminEssayGoodsOrderListVO();
            BeanUtils.copyProperties(goodsOrder, goodsOrderListVO);

            goodsOrderListVO.setTotalMoney((double) goodsOrder.getTotalMoney() / 100);
            goodsOrderListVO.setRealMoney((double) goodsOrder.getRealMoney() / 100);

            if (goodsOrder.getSource() != null) {
                goodsOrderListVO.setSource(goodsOrder.getSource().ordinal());
            }

            goodsOrderListVOS.add(goodsOrderListVO);
        });

        PageUtil result = PageUtil.builder()
                .result(goodsOrderListVOS)
                .next(goodsOrders.getTotalElements() > page * pageSize ? 1 : 0)
                .total(goodsOrders.getTotalElements())
                .totalPage(goodsOrders.getTotalPages())
                .build();

        return result;
    }

    @Override
    public AdminEssayGoodsOrderDetailWrapperVO detail(Long id) {
        EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(id);

        AdminEssayGoodsOrderDetailWrapperVO goodsOrderDetailVO = new AdminEssayGoodsOrderDetailWrapperVO();

        BeanUtils.copyProperties(goodsOrder, goodsOrderDetailVO);

        goodsOrderDetailVO.setTotalMoney((double) goodsOrder.getTotalMoney() / 100);
        goodsOrderDetailVO.setRealMoney((double) goodsOrder.getRealMoney() / 100);

        if (goodsOrder.getSource() != null) {
            goodsOrderDetailVO.setSource(goodsOrder.getSource().ordinal());
        }

        List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository.findByRecordId(id);

        List<AdminEssayGoodsOrderDetailVO> goodsOrderDetailVOS = Lists.newArrayListWithExpectedSize(goodsOrderDetails.size());

        int totalAmount = 0;
        int restAmount = 0;
        int usableAmount = 0;

        for (EssayGoodsOrderDetail goodsOrderDetail : goodsOrderDetails) {
            // 订单有效期（显示最长有效期）
            if (goodsOrderDetailVO.getExpireDate() == null || goodsOrderDetailVO.getExpireDate().before(goodsOrderDetail.getExpireDate())) {
                goodsOrderDetailVO.setExpireDate(goodsOrderDetail.getExpireDate());
            }

            int goodsOrderDetailTotalAmount = goodsOrderDetail.getCount() * goodsOrderDetail.getUnit();
            totalAmount += goodsOrderDetailTotalAmount;
            restAmount += goodsOrderDetail.getNum();
            //判断订单是否过期
            boolean isExpired = goodsOrderDetail.getExpireFlag() == 0 || (goodsOrderDetail.getExpireFlag() == 1 && goodsOrderDetail.getExpireDate().after(new Date())); 
            boolean isUsable = (goodsOrderDetail.getBizStatus() == EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() ||
                    goodsOrderDetail.getBizStatus() == EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());

            if (isUsable && isExpired) {
                usableAmount += restAmount;
            }

            AdminEssayGoodsOrderDetailVO goodsOrderDetailVOTemp = new AdminEssayGoodsOrderDetailVO();

            BeanUtils.copyProperties(goodsOrderDetail, goodsOrderDetailVOTemp);

            goodsOrderDetailVOTemp.setPrice((double) goodsOrderDetail.getPrice() / 100);
            goodsOrderDetailVOTemp.setTotalMoney((double) goodsOrderDetail.getCount() * goodsOrderDetail.getPrice() / 100);
            goodsOrderDetailVOTemp.setTotalAmount(goodsOrderDetailTotalAmount);
            goodsOrderDetailVOTemp.setUsedAmount(goodsOrderDetailTotalAmount - goodsOrderDetail.getNum());
            goodsOrderDetailVOTemp.setRestAmount(goodsOrderDetail.getNum());

            goodsOrderDetailVOS.add(goodsOrderDetailVOTemp);
        }

        goodsOrderDetailVO.setTotalAmount(totalAmount);
        goodsOrderDetailVO.setUsedAmount(totalAmount - restAmount);
        goodsOrderDetailVO.setUsableAmount(usableAmount);
        goodsOrderDetailVO.setRestAmount(restAmount);
        goodsOrderDetailVO.setOrderDetails(goodsOrderDetailVOS);

        return goodsOrderDetailVO;
    }

    @Override
    public AdminEssayGoodsOrderPreRefundVO preRefund(Long orderId, Long orderDetailId) {
        EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(orderId);

        Assert.isTrue(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() == goodsOrder.getBizStatus()
                        || EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus() == goodsOrder.getBizStatus()
                , "订单状态错误");

        String goodsName = null;
        double refundMoney;

        if (orderDetailId != null) {
            EssayGoodsOrderDetail goodsOrderDetail = goodsOrderDetailRepository.findOne(orderDetailId);

            Assert.isTrue(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() == goodsOrder.getBizStatus()
                            || EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus() == goodsOrder.getBizStatus()
                    , "订单明细状态错误");

            Assert.isTrue(goodsOrderDetail.getExpireDate().after(new Date()), "订单已过期");

            goodsName = goodsOrderDetail.getGoodsName();

            refundMoney = new BigDecimal(goodsOrder.getRealMoney())
                    .multiply(new BigDecimal(goodsOrderDetail.getPrice() * goodsOrderDetail.getCount()))
                    .multiply(new BigDecimal(goodsOrderDetail.getNum()))
					.divide(new BigDecimal(goodsOrder.getTotalMoney() == 0 ? 1 : goodsOrder.getTotalMoney()).multiply(new BigDecimal(goodsOrderDetail.getCount() * goodsOrderDetail.getUnit())), 10, BigDecimal.ROUND_HALF_UP)
                    .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } else {
            List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository.findByRecordId(orderId);

            BigDecimal totalRefundMoney = new BigDecimal(0);

            for (EssayGoodsOrderDetail goodsOrderDetailTemp : goodsOrderDetails) {
                Assert.isTrue(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() == goodsOrder.getBizStatus()
                                || EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus() == goodsOrder.getBizStatus()
                        , "订单明细状态错误");

				if ((goodsOrderDetailTemp.getExpireFlag() == 0) || (goodsOrderDetailTemp.getExpireFlag() == 1
						&& goodsOrderDetailTemp.getExpireDate().after(new Date()))) {
					totalRefundMoney = totalRefundMoney.add(new BigDecimal(goodsOrder.getRealMoney())
							.multiply(new BigDecimal(goodsOrderDetailTemp.getPrice() * goodsOrderDetailTemp.getCount()))
							.multiply(new BigDecimal(goodsOrderDetailTemp.getNum()))
							.divide(new BigDecimal(goodsOrder.getTotalMoney()).multiply(
									new BigDecimal(goodsOrderDetailTemp.getCount() * goodsOrderDetailTemp.getUnit())),
									10, BigDecimal.ROUND_HALF_UP));
				}
            }

            refundMoney = totalRefundMoney.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        return AdminEssayGoodsOrderPreRefundVO.builder()
                .orderNumStr(goodsOrder.getOrderNumStr())
                .goodsName(goodsName)
                .payType(goodsOrder.getPayType())
                .refundMoney(refundMoney)
                .build();
    }

    @Override
    public EssayGoodsOrderInfoForCourseVo infoForCourse(Long courseOrderId) {
        EssayGoodsOrder goodsOrder = goodsOrderRepository.findOneByCourseOrderId(courseOrderId);
		if (goodsOrder == null) {
			throw new BizException(EssayErrors.ORDER_ID_ERROR);
		}
        // 总金额
        BigDecimal totalMoney = new BigDecimal(goodsOrder.getRealMoney());
        // 可退金额
        BigDecimal refundMoney = new BigDecimal(0);

        if (EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() == goodsOrder.getBizStatus()
                || EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus() == goodsOrder.getBizStatus()) {
            List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository.findByRecordId(goodsOrder.getId());

            for (EssayGoodsOrderDetail goodsOrderDetail : goodsOrderDetails) {
                if (EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() == goodsOrderDetail.getBizStatus()
                        || EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus() == goodsOrderDetail.getBizStatus()) {
                    if (goodsOrderDetail.getExpireDate().after(new Date())) {
                        refundMoney = refundMoney.add(new BigDecimal(goodsOrder.getRealMoney())
                                .multiply(new BigDecimal(goodsOrderDetail.getPrice() * goodsOrderDetail.getCount()))
                                .multiply(new BigDecimal(goodsOrderDetail.getNum()))
								.divide(new BigDecimal(goodsOrder.getTotalMoney() == 0 ? 1 : goodsOrder.getTotalMoney()).multiply(new BigDecimal(goodsOrderDetail.getCount() * goodsOrderDetail.getUnit())), 10, BigDecimal.ROUND_HALF_UP));
                    }
                }
            }
        }

        // 不可退金额（课程中需扣除金额）
        BigDecimal deductdMoney = totalMoney.subtract(refundMoney).divide(new BigDecimal(1), 2, BigDecimal.ROUND_HALF_UP);

        return EssayGoodsOrderInfoForCourseVo.builder()
                .deductMoney(deductdMoney)
                .build();
    }
}
