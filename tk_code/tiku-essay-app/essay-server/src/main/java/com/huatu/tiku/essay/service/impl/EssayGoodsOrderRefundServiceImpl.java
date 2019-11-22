package com.huatu.tiku.essay.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ht.base.start.security.service.UserOption;
import com.ht.base.user.module.security.UserInfo;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderRefundDto;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderRefundListDto;
import com.huatu.tiku.essay.entity.EssayGoodsOrder;
import com.huatu.tiku.essay.entity.EssayGoodsOrderDetail;
import com.huatu.tiku.essay.entity.EssayGoodsOrderRefund;
import com.huatu.tiku.essay.entity.PayReturn;
import com.huatu.tiku.essay.entity.WeChatPay;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderRefundDestEnum;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderRefundStatusEnum;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderRefundTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderSourceEnum;
import com.huatu.tiku.essay.repository.EssayGoodsOrderDetailRepository;
import com.huatu.tiku.essay.repository.EssayGoodsOrderRefundRepository;
import com.huatu.tiku.essay.repository.EssayGoodsOrderRepository;
import com.huatu.tiku.essay.repository.PayReturnRepository;
import com.huatu.tiku.essay.repository.WeChatPayRepository;
import com.huatu.tiku.essay.service.AliPayService;
import com.huatu.tiku.essay.service.EssayGoodsOrderRefundService;
import com.huatu.tiku.essay.service.PHPCoinService;
import com.huatu.tiku.essay.service.WxPayService;
import com.huatu.tiku.essay.service.correct.UserCorrectGoodsServiceV4;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderRefundListVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderRefundRecordVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EssayGoodsOrderRefundServiceImpl implements EssayGoodsOrderRefundService {

	@Autowired
	private EssayGoodsOrderRefundRepository goodsOrderRefundRepository;

	@Autowired
	private EssayGoodsOrderRepository goodsOrderRepository;

	@Autowired
	private UserOption userOption;

	@Autowired
	private EssayGoodsOrderDetailRepository goodsOrderDetailRepository;

	@Autowired
	private WxPayService wxPayService;

	@Autowired
	private WeChatPayRepository weChatPayRepository;

	@Autowired
	private AliPayService aliPayService;

	@Autowired
	private PHPCoinService phpCoinService;

	@Autowired
	private UserCorrectGoodsServiceV4 userCorrectGoodsServiceV4;

	@Autowired
	private PayReturnRepository payReturnRepository;

	@Override
	public PageUtil<AdminEssayGoodsOrderRefundListVO> list(AdminEssayGoodsOrderRefundListDto goodsOrderRefundListDto,
			Integer page, Integer pageSize) {
		Page<EssayGoodsOrderRefund> goodsOrderRefunds = goodsOrderRefundRepository.findAll((root, query, cb) -> {
			List<Predicate> predicates = Lists.newArrayList();

			if (goodsOrderRefundListDto.getBizStatus() != null) {
				predicates.add(cb.equal(root.get("bizStatus"), goodsOrderRefundListDto.getBizStatus()));
			}
			if (goodsOrderRefundListDto.getGmtCreateBegin() != null) {
				predicates.add(
						cb.greaterThanOrEqualTo(root.get("gmtCreate"), goodsOrderRefundListDto.getGmtCreateBegin()));
			}
			if (goodsOrderRefundListDto.getGmtCreateEnd() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("gmtCreate"), goodsOrderRefundListDto.getGmtCreateEnd()));
			}
			if (!Strings.isNullOrEmpty(goodsOrderRefundListDto.getOrderNumStr())) {
				predicates.add(cb.equal(root.get("orderNumStr"), goodsOrderRefundListDto.getOrderNumStr()));
			}

			// 排除课程退单
			predicates.add(cb.lessThan(root.get("payType"), 3));

			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		}, new PageRequest(page - 1, pageSize));

		List<AdminEssayGoodsOrderRefundListVO> goodsOrderRefundListVOS = Lists.newArrayList();

		goodsOrderRefunds.getContent().forEach(goodsOrderRefund -> {
			AdminEssayGoodsOrderRefundListVO goodsOrderListVO = new AdminEssayGoodsOrderRefundListVO();

			BeanUtils.copyProperties(goodsOrderRefund, goodsOrderListVO);

			goodsOrderListVO.setRealMoney((double) goodsOrderRefund.getRealMoney() / 100);
			goodsOrderListVO.setRefundMoney((double) goodsOrderRefund.getRefundMoney() / 100);
			goodsOrderListVO.setRefundType(goodsOrderRefund.getRefundType().ordinal());

			goodsOrderRefundListVOS.add(goodsOrderListVO);
		});

		PageUtil result = PageUtil.builder().result(goodsOrderRefundListVOS)
				.next(goodsOrderRefunds.getTotalElements() > page * pageSize ? 1 : 0)
				.total(goodsOrderRefunds.getTotalElements()).totalPage(goodsOrderRefunds.getTotalPages()).build();

		return result;
	}

	@Transactional
	@Override
	public void audit(Long id, String remark, EssayGoodsOrderRefundStatusEnum currStatus,
			EssayGoodsOrderRefundStatusEnum targetStatus) {
		EssayGoodsOrderRefund goodsOrderRefund = goodsOrderRefundRepository.findOne(id);

		if (currStatus.ordinal() != goodsOrderRefund.getBizStatus()) {
			throw new BizException(ErrorResult.create(1000125, "状态错误"));
		}
		goodsOrderRefund.setBizStatus(targetStatus.ordinal());

		UserInfo userInfo = userOption.getUserInfo();

		goodsOrderRefund.setModifier(userInfo.getName());

		if (targetStatus == EssayGoodsOrderRefundStatusEnum.PASS1) {
			goodsOrderRefund.setRemark1(remark);

			goodsOrderRefund.setOperator1Id(userInfo.getId());
			goodsOrderRefund.setOperator1(userInfo.getName());
		} else if (targetStatus == EssayGoodsOrderRefundStatusEnum.DENY1) {
			goodsOrderRefund.setRemark1(remark);

			goodsOrderRefund.setOperator1Id(userInfo.getId());
			goodsOrderRefund.setOperator1(userInfo.getName());

			if (goodsOrderRefund.getGoodsOrderDetailId() == null) {
				EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(goodsOrderRefund.getGoodsOrderId());
				goodsOrder.setBizStatus(
						EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());
				goodsOrder.setModifier(userInfo.getName());
				goodsOrder.setModifierId(userInfo.getId());

				List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository
						.findByRecordId(goodsOrder.getId());
				for (EssayGoodsOrderDetail goodsOrderDetail : goodsOrderDetails) {
					goodsOrderDetail.setModifier(userInfo.getName());
					goodsOrderDetail.setModifierId(userInfo.getId());
				}
				updateGoodsDetailBizStatus(goodsOrderDetails,
						EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT);
				goodsOrderRepository.save(goodsOrder);

			} else {
				EssayGoodsOrderDetail goodsOrderDetail = goodsOrderDetailRepository
						.findOne(goodsOrderRefund.getGoodsOrderDetailId());
				goodsOrderDetail.setBizStatus(
						EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());
				goodsOrderDetail.setModifier(userInfo.getName());
				goodsOrderDetail.setModifierId(userInfo.getId());

				goodsOrderDetailRepository.save(goodsOrderDetail);
				userCorrectGoodsServiceV4.resetUserCorrectTime(new Long(goodsOrderDetail.getUserId()).intValue(),
						EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));

				// 如果只有一条明细，更新订单状态
				List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository
						.findByRecordId(goodsOrderRefund.getGoodsOrderId());

				if (goodsOrderDetails.size() == 1) {
					EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(goodsOrderRefund.getGoodsOrderId());

					goodsOrder.setBizStatus(
							EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());
					goodsOrder.setModifier(userInfo.getName());
					goodsOrder.setModifierId(userInfo.getId());

					goodsOrderRepository.save(goodsOrder);
				}
			}
		} else if (targetStatus == EssayGoodsOrderRefundStatusEnum.PASS2) {
			goodsOrderRefund.setRemark2(remark);
			goodsOrderRefund.setOperator2Id(userInfo.getId());
			goodsOrderRefund.setOperator2(userInfo.getName());

			if (goodsOrderRefund.getGoodsOrderDetailId() == null) {
				EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(goodsOrderRefund.getGoodsOrderId());
				List<EssayGoodsOrderDetail> details = goodsOrderDetailRepository.findByRecordId(goodsOrder.getId());
				if (!CollectionUtils.isEmpty(details)) {
					for (EssayGoodsOrderDetail detail : details) {
						detail.setModifier(userInfo.getName());
						detail.setModifierId(userInfo.getId());
					}
					updateGoodsDetailBizStatus(details, EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED);
				}
				goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED.getBizStatus());
				goodsOrder.setModifier(userInfo.getName());
				goodsOrder.setModifierId(userInfo.getId());

				goodsOrderRepository.save(goodsOrder);
			} else {
				EssayGoodsOrderDetail goodsOrderDetail = goodsOrderDetailRepository
						.findOne(goodsOrderRefund.getGoodsOrderDetailId());
				goodsOrderDetail
						.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED.getBizStatus());
				goodsOrderDetail.setModifier(userInfo.getName());
				goodsOrderDetail.setModifierId(userInfo.getId());

				goodsOrderDetailRepository.save(goodsOrderDetail);
				userCorrectGoodsServiceV4.resetUserCorrectTime(new Long(goodsOrderDetail.getUserId()).intValue(),
						EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));

				List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository
						.findByRecordId(goodsOrderDetail.getRecordId());

				boolean noBackedFlag = goodsOrderDetails.stream().filter(goodsOrderDetailTemp -> goodsOrderDetailTemp
						.getBizStatus() != EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED.getBizStatus())
						.findAny().isPresent();

				if (!noBackedFlag) {
					EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(goodsOrderRefund.getGoodsOrderId());
					goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED.getBizStatus());
					goodsOrder.setModifier(userInfo.getName());
					goodsOrder.setModifierId(userInfo.getId());

					goodsOrderRepository.save(goodsOrder);
				}
			}

			if (EssayGoodsOrderRefundTypeEnum.AUTO == goodsOrderRefund.getRefundType()) {
				// 支付类型(0 支付宝 1 微信 2 金币)
				if (goodsOrderRefund.getPayType() == 0) {
					int newPay = 0;// 默认老支付宝方式
					List<PayReturn> payReturnList = payReturnRepository
							.findByOutTradeNo(goodsOrderRefund.getGoodsOrderId() + "");
					if (!CollectionUtils.isEmpty(payReturnList)) {
						newPay = payReturnList.get(0).getNewPay() == null ? 0 : payReturnList.get(0).getNewPay();
					}
					aliPayService.refund(goodsOrderRefund.getGoodsOrderId() + "", goodsOrderRefund.getRefundMoney(),
							newPay);
				} else if (goodsOrderRefund.getPayType() == 1) {
					WeChatPay weChatPay = weChatPayRepository.findByOrderId(goodsOrderRefund.getGoodsOrderId());

					wxPayService.refund(weChatPay.getOutTradeNo(), weChatPay.getTransactionId(),
							goodsOrderRefund.getRefundMoney(), goodsOrderRefund.getRealMoney());
				} else if (goodsOrderRefund.getPayType() == 2) {
					phpCoinService.refund(goodsOrderRefund.getGoodsOrderId(), goodsOrderRefund.getGoodsOrderDetailId(),
							goodsOrderRefund.getRefundMoney());
				}
			}
		} else if (targetStatus == EssayGoodsOrderRefundStatusEnum.DENY2) {
			goodsOrderRefund.setRemark2(remark);
			goodsOrderRefund.setOperator2Id(userInfo.getId());
			goodsOrderRefund.setOperator2(userInfo.getName());

			if (goodsOrderRefund.getGoodsOrderDetailId() == null) {
				EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(goodsOrderRefund.getGoodsOrderId());
				goodsOrder.setBizStatus(
						EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());
				goodsOrder.setModifier(userInfo.getName());
				goodsOrder.setModifierId(userInfo.getId());

				goodsOrderRepository.save(goodsOrder);

				List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository
						.findByRecordId(goodsOrder.getId());
				goodsOrderDetails.forEach(goodsOrderDetail -> {
					goodsOrderDetail.setBizStatus(
							EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());
					goodsOrderDetail.setModifier(userInfo.getName());
					goodsOrderDetail.setModifierId(userInfo.getId());

					userCorrectGoodsServiceV4.resetUserCorrectTime(new Long(goodsOrderDetail.getUserId()).intValue(),
							EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));
				});

				goodsOrderDetailRepository.save(goodsOrderDetails);
			} else {
				EssayGoodsOrderDetail goodsOrderDetail = goodsOrderDetailRepository
						.findOne(goodsOrderRefund.getGoodsOrderDetailId());
				goodsOrderDetail.setBizStatus(
						EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());
				goodsOrderDetail.setModifier(userInfo.getName());
				goodsOrderDetail.setModifierId(userInfo.getId());

				goodsOrderDetailRepository.save(goodsOrderDetail);
				userCorrectGoodsServiceV4.resetUserCorrectTime(new Long(goodsOrderDetail.getUserId()).intValue(),
						EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));

				// 如果只有一条明细，更新订单状态
				List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository
						.findByRecordId(goodsOrderRefund.getGoodsOrderId());

				if (goodsOrderDetails.size() == 1) {
					EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(goodsOrderRefund.getGoodsOrderId());

					goodsOrder.setBizStatus(
							EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());
					goodsOrder.setModifier(userInfo.getName());
					goodsOrder.setModifierId(userInfo.getId());

					goodsOrderRepository.save(goodsOrder);
				}
			}
		}

		goodsOrderRefundRepository.save(goodsOrderRefund);
	}

	@Transactional
	@Override
	public void refund(AdminEssayGoodsOrderRefundDto goodsOrderRefundDto) {
		EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(goodsOrderRefundDto.getGoodsOrderId());

		Assert.isTrue(EssayGoodsOrderSourceEnum.APP.equals(goodsOrder.getSource()), "订单来源错误");
		Assert.isTrue(
				EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() == goodsOrder.getBizStatus()
						|| EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT
								.getBizStatus() == goodsOrder.getBizStatus(),
				"订单状态异常");

		EssayGoodsOrderRefund goodsOrderRefund = new EssayGoodsOrderRefund();

		goodsOrderRefund.setGoodsOrderId(goodsOrderRefundDto.getGoodsOrderId());
		goodsOrderRefund.setGoodsOrderDetailId(goodsOrderRefundDto.getGoodsOrderDetailId());
		goodsOrderRefund.setOrderNumStr(goodsOrder.getOrderNumStr());
		goodsOrderRefund.setUserId(goodsOrder.getUserId());
		goodsOrderRefund.setPayType(goodsOrder.getPayType());
		goodsOrderRefund.setPayTime(goodsOrder.getPayTime());
		goodsOrderRefund.setRefundType(EssayGoodsOrderRefundTypeEnum.of(goodsOrderRefundDto.getRefundType()));
		goodsOrderRefund.setRefundMoney((int) (goodsOrderRefundDto.getRefundMoney() * 100));
		goodsOrderRefund.setRemark(goodsOrderRefundDto.getRemark());
		goodsOrderRefund.setRealMoney(goodsOrder.getRealMoney());
		goodsOrderRefund.setRefundDest(EssayGoodsOrderRefundDestEnum.of(goodsOrderRefundDto.getRefundDest()));
		goodsOrderRefund.setName(goodsOrder.getName());

		UserInfo userInfo = userOption.getUserInfo();
		goodsOrderRefund.setCreatorId(userInfo.getId());
		goodsOrderRefund.setCreator(userInfo.getName());
		goodsOrderRefund.setModifier(userInfo.getName());

		if (goodsOrderRefundDto.getGoodsOrderDetailId() == null) {
			List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository
					.findByRecordId(goodsOrder.getId());
			updateGoodsDetailBizStatus(goodsOrderDetails,
					EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK);
//            goodsOrderDetails.forEach(goodsOrderDetail -> {
//                goodsOrderDetail.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK.getBizStatus());
//
//                // 更新次数
//                userCorrectGoodsServiceV4.resetUserCorrectTime(goodsOrder.getUserId(), EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));
//            });
//
//            goodsOrderDetailRepository.save(goodsOrderDetails);

			goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK.getBizStatus());

			goodsOrderRepository.save(goodsOrder);

			if (goodsOrderDetails.size() == 1) {
				goodsOrderRefund.setGoodsName(goodsOrderDetails.get(0).getGoodsName());
			}
		} else {
			EssayGoodsOrderDetail goodsOrderDetail = goodsOrderDetailRepository
					.findOne(goodsOrderRefundDto.getGoodsOrderDetailId());
			goodsOrderDetail.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK.getBizStatus());

			goodsOrderDetailRepository.save(goodsOrderDetail);

			// 更新次数
			userCorrectGoodsServiceV4.resetUserCorrectTime(goodsOrder.getUserId(),
					EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));

			goodsOrderRefund.setGoodsName(goodsOrderDetail.getGoodsName());
		}

		goodsOrderRefundRepository.save(goodsOrderRefund);

		// 如果只有一条明细，更新订单状态
		List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository.findByRecordId(goodsOrder.getId());

		if (goodsOrderDetails.size() == 1) {
			goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK.getBizStatus());

			goodsOrderRepository.save(goodsOrder);
		}
	}

	private void updateGoodsDetailBizStatus(List<EssayGoodsOrderDetail> goodsOrderDetails,
			EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum goodsOrderBizStatusEnum) {
		goodsOrderDetails.parallelStream().forEach(i -> i.setBizStatus(goodsOrderBizStatusEnum.getBizStatus()));
		goodsOrderDetailRepository.save(goodsOrderDetails);
		int userId = new Long(goodsOrderDetails.get(0).getUserId()).intValue();
		goodsOrderDetails.stream().map(EssayGoodsOrderDetail::getGoodsType)
				.map(EssayCorrectGoodsConstant.GoodsTypeEnum::create).distinct().forEach(i -> {
					userCorrectGoodsServiceV4.resetUserCorrectTime(userId, i);
				});
	}

	@Override
	public List<AdminEssayGoodsOrderRefundRecordVO> listByOrderId(Long orderId) {
		List<EssayGoodsOrderRefund> goodsOrderRefunds = goodsOrderRefundRepository.findByGoodsOrderId(orderId);

		Collections.reverse(goodsOrderRefunds);

		List<AdminEssayGoodsOrderRefundRecordVO> goodsOrderRefundRecordVOS = Lists.newArrayList();

		Set<String> recordSet = Sets.newHashSet();
		if (CollectionUtils.isNotEmpty(goodsOrderRefunds)) {
			goodsOrderRefunds.forEach(goodsOrderRefund -> {
				String signature = "" + goodsOrderRefund.getGoodsOrderId() + goodsOrderRefund.getGoodsOrderDetailId();
				if (!recordSet.contains(signature)) {
					recordSet.add(signature);

					AdminEssayGoodsOrderRefundRecordVO goodsOrderRefundRecordVO = AdminEssayGoodsOrderRefundRecordVO
							.builder().gmtModify(goodsOrderRefund.getGmtModify())
							.modifier(goodsOrderRefund.getModifier()).bizStatus(goodsOrderRefund.getBizStatus())
							.refundMoney((double) goodsOrderRefund.getRefundMoney() / 100)
							.goodsName(goodsOrderRefund.getGoodsName()).build();

					if (goodsOrderRefund.getGoodsOrderDetailId() != null) {
						EssayGoodsOrderDetail goodsOrderDetail = goodsOrderDetailRepository
								.findOne(goodsOrderRefund.getGoodsOrderDetailId());
						goodsOrderRefundRecordVO.setBizStatus(goodsOrderDetail.getBizStatus());
					} else {
						EssayGoodsOrder goodsOrder = goodsOrderRepository.findOne(goodsOrderRefund.getGoodsOrderId());
						goodsOrderRefundRecordVO.setBizStatus(goodsOrder.getBizStatus());
					}

					if (goodsOrderRefund.getBizStatus() == EssayGoodsOrderRefundStatusEnum.TODO.ordinal()) {
						goodsOrderRefundRecordVO.setRemark(goodsOrderRefund.getRemark());
					} else if (goodsOrderRefund.getBizStatus() == EssayGoodsOrderRefundStatusEnum.PASS1.ordinal()
							|| goodsOrderRefund.getBizStatus() == EssayGoodsOrderRefundStatusEnum.DENY1.ordinal()) {
						goodsOrderRefundRecordVO.setRemark(goodsOrderRefund.getRemark1());
					} else if (goodsOrderRefund.getBizStatus() == EssayGoodsOrderRefundStatusEnum.PASS2.ordinal()
							|| goodsOrderRefund.getBizStatus() == EssayGoodsOrderRefundStatusEnum.DENY2.ordinal()) {
						goodsOrderRefundRecordVO.setRemark(goodsOrderRefund.getRemark2());
					}

					goodsOrderRefundRecordVOS.add(goodsOrderRefundRecordVO);
				}
			});
		}
		return goodsOrderRefundRecordVOS;
	}

	@Transactional
	@Override
	public void preRefundByCourseOrderId(Long courseOrderId, String userName) {
		EssayGoodsOrder goodsOrder = goodsOrderRepository.findOneByCourseOrderId(courseOrderId);
		// Assert.isTrue(null != goodsOrder, "订单不存在");
		if (null == goodsOrder) {
			throw new BizException(ErrorResult.create(1000123, "订单不存在"));
		}
		if (!goodsOrder.getName().equals(userName)) {
			throw new BizException(ErrorResult.create(1000124, "订单与用户不匹配"));
		}
		Assert.isTrue(
				EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() == goodsOrder.getBizStatus()
						|| EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT
								.getBizStatus() == goodsOrder.getBizStatus(),
				"订单状态异常");

		EssayGoodsOrderRefund goodsOrderRefund = new EssayGoodsOrderRefund();

		goodsOrderRefund.setGoodsOrderId(goodsOrder.getId());
		goodsOrderRefund.setOrderNumStr(goodsOrder.getOrderNumStr());
		goodsOrderRefund.setUserId(goodsOrder.getUserId());
		goodsOrderRefund.setPayType(goodsOrder.getPayType());
		goodsOrderRefund.setPayTime(goodsOrder.getPayTime());
		goodsOrderRefund.setRefundType(EssayGoodsOrderRefundTypeEnum.AUTO);
		goodsOrderRefund.setRefundMoney(0);
		goodsOrderRefund.setRemark("PHP课程退款");
		goodsOrderRefund.setRealMoney(goodsOrder.getRealMoney());
		goodsOrderRefund.setName(userName);

		goodsOrderRefundRepository.save(goodsOrderRefund);

		goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK.getBizStatus());

		goodsOrderRepository.save(goodsOrder);

		List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository.findByRecordId(goodsOrder.getId());
		updateGoodsDetailBizStatus(goodsOrderDetails, EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK);
//        goodsOrderDetails.forEach(goodsOrderDetail -> {
//            goodsOrderDetail.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK.getBizStatus());
//
//            // 更新次数
//            userCorrectGoodsServiceV4.resetUserCorrectTime(goodsOrder.getUserId(), EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));
//        });
//
//        goodsOrderDetailRepository.save(goodsOrderDetails);
	}

	@Transactional
	@Override
	public void refundByCourseOrderId(Long courseOrderId, String userName) {
		EssayGoodsOrder goodsOrder = goodsOrderRepository.findOneByCourseOrderId(courseOrderId);

		if (null == goodsOrder) {
			throw new BizException(ErrorResult.create(2000501, "订单不存在"));
		}
		if (!goodsOrder.getName().equals(userName)) {
			throw new BizException(ErrorResult.create(2000502, "订单与用户不匹配"));
		}
		if (EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK.getBizStatus() != goodsOrder.getBizStatus()) {
			throw new BizException(ErrorResult.create(2000502, "订单状态异常"));
		}

		EssayGoodsOrderRefund goodsOrderRefund = goodsOrderRefundRepository
				.findTop1ByGoodsOrderIdOrderByIdDesc(goodsOrder.getId());
		goodsOrderRefund.setBizStatus(EssayGoodsOrderRefundStatusEnum.PASS1.ordinal());

		goodsOrderRefundRepository.save(goodsOrderRefund);

		List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository.findByRecordId(goodsOrder.getId());
		updateGoodsDetailBizStatus(goodsOrderDetails, EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED);

		goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED.getBizStatus());

		goodsOrderRepository.save(goodsOrder);
//
//        goodsOrderDetails.forEach(goodsOrderDetail -> {
//            goodsOrderDetail.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED.getBizStatus());
//
//            // 更新次数
//            userCorrectGoodsServiceV4.resetUserCorrectTime(goodsOrder.getUserId(), EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));
//        });
//
//        goodsOrderDetailRepository.save(goodsOrderDetails);
	}

	@Transactional
	@Override
	public void cancelRefundByCourseOrderId(Long courseOrderId, String userName) {
		EssayGoodsOrder goodsOrder = goodsOrderRepository.findOneByCourseOrderId(courseOrderId);
		if (goodsOrder == null) {
			throw new BizException(ErrorResult.create(2000501, "订单不存在"));
		}
		if (!goodsOrder.getName().equals(userName)) {
			throw new BizException(ErrorResult.create(2000502, "订单与用户不匹配"));
		}
		if (EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PRE_BACK.getBizStatus() != goodsOrder.getBizStatus()) {
			throw new BizException(ErrorResult.create(2000502, "订单状态异常"));
		}

		EssayGoodsOrderRefund goodsOrderRefund = goodsOrderRefundRepository
				.findTop1ByGoodsOrderIdOrderByIdDesc(goodsOrder.getId());
		goodsOrderRefund.setBizStatus(EssayGoodsOrderRefundStatusEnum.DENY1.ordinal());

		goodsOrderRefundRepository.save(goodsOrderRefund);

		List<EssayGoodsOrderDetail> goodsOrderDetails = goodsOrderDetailRepository.findByRecordId(goodsOrder.getId());
		updateGoodsDetailBizStatus(goodsOrderDetails,
				EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT);

		goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());

		goodsOrderRepository.save(goodsOrder);
//        goodsOrderDetails.forEach(goodsOrderDetail -> {
//            goodsOrderDetail.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.BACKED_REJECT.getBizStatus());
//
//            // 更新次数
//            userCorrectGoodsServiceV4.resetUserCorrectTime(goodsOrder.getUserId(), EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsOrderDetail.getGoodsType()));
//        });

//        goodsOrderDetailRepository.save(goodsOrderDetails);

	}
	
}
