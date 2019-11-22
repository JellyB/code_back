package com.huatu.tiku.essay.web.controller.callback;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alipay.api.internal.util.AlipaySignature;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.huatu.common.spring.web.MediaType;
import com.huatu.common.utils.date.DateFormatUtil;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.constant.status.PayConstant;
import com.huatu.tiku.essay.entity.PayReturn;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.pay.AliPayConfigNew;

import lombok.extern.slf4j.Slf4j;

/**
 * 新支付宝支付回调
 * 
 * @author zhangchong
 *
 */
@RestController
@Slf4j
@RequestMapping("api/v2/aliPay")
public class ALiPayControllerV2 {
	@Autowired
	UserCorrectGoodsService userCorrectGoodsService;

	/**
	 * 支付宝回调
	 */
	@PostMapping(value = "sync", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Object aLiPaySync(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info(">>>>>>>>>>>>>>>>>>>>进入支付宝新帐号回调接口<<<<<<<<<<<<<<<<<<<<<<<<");
		Map requestParams = request.getParameterMap();
		if (requestParams == null) {
			return "参数有误";
		}
		// 处理支付宝请求参数
		Map<String, String> params = Maps.newHashMapWithExpectedSize(requestParams.size() * 3 / 4 + 1);
		for (Iterator<String> it = requestParams.keySet().iterator(); it.hasNext();) {
			String name = it.next();
			String[] values = (String[]) requestParams.get(name);
			List<String> list = Arrays.asList(values);
			String valueStr = Joiner.on(",").join(list);

			params.put(name, valueStr);
		}
		log.info("回调返回参数:{}", params.toString());
		// 保存回调信息
		savePayReturn(params);
		String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
		// 通知返回时验证签名
		log.info("new param是否为空:" + (null == params));
		boolean isLegal = AlipaySignature.rsaCheckV1(params, AliPayConfigNew.ali_public_key,
				AliPayConfigNew.input_charset, AliPayConfigNew.sign_type);
		if (isLegal) {
			if ("TRADE_FINISHED".equals(trade_status)) {
				log.info(">>>>>>>>>>>>>>>>>>>>TRADE_FINISHED<<<<<<<<<<<<<<<<<<<<<<<<");
				// 判断该笔订单是否在商户网站中已经做过处理
				// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
				// 如果有做过处理，不执行商户的业务程序

				// 注意：
				// 该种交易状态只在两种情况下出现
				// 1、开通了普通即时到账，买家付款成功后。
				// 2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
			} else if ("TRADE_SUCCESS".equals(trade_status)) {
				log.info(">>>>>>>>>>>>>>>>>>>>支付宝到账回调：" + params.get("out_trade_no").toString()
						+ "<<<<<<<<<<<<<<<<<<<<<<<<");
				int payType = PayConstant.ALI_PAY;
				// 判断订单确认已支付。根据订单id查询订单支付状态
				long count = userCorrectGoodsService.countByIdAndPayTypeAndBizStatus(
						Long.parseLong(params.get("out_trade_no").toString()), payType,
						EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus());
				log.info("========count======" + count);
				if (0 != count) {
					log.info(">>>>>>>>>>>>>>>>>>>>success已经操作过<<<<<<<<<<<<<<<<<<<<<<<<");
					// 通知支付宝已经成功，不用一直回调接口
					// 请不要修改或删除
					response.getWriter().println("SUCCESS");
				} else {
					log.info(">>>>>>>>>>>>>>>>>>>>TRADE_FINISHED<<<<<<<<<<<<<<<<<<<<<<<<");
					long orderId = Long.parseLong(params.get("out_trade_no").toString());
					int updateUserCorrectGoods = -1;
					try {
						// 支付回调业务处理
						// 支付成功，更新用户的批改次数和商品库存
						updateUserCorrectGoods = userCorrectGoodsService.updateUserCorrectGoods(orderId);
					} catch (Exception e) {
						e.printStackTrace();
						log.info("支付宝支付回调异常" + orderId);
						// 更改订单状态为异常
						userCorrectGoodsService.shutReturn(orderId);
					}
					if (1 == updateUserCorrectGoods) {
						log.info(">>>>>>>>>>>>>>>>>>>>success<<<<<<<<<<<<<<<<<<<<<<<<");
						// 通知支付宝已经成功，不用一直回调接口
						// 请不要修改或删除
						response.getWriter().println("SUCCESS");
					} else {
						log.info("支付回调未处理：" + orderId + "updateUserCorrectGoods" + updateUserCorrectGoods);
					}
				}
			}
			log.info(">>>>>>>>>>>>>>>>>>>>success<<<<<<<<<<<<<<<<<<<<<<<<");
			// 通知支付宝已经成功，不用一直回调接口
			// 请不要修改或删除
			response.getWriter().println("SUCCESS");
		} else {
			/**
			 * 验证失败，支付宝服务器会不断重发通知，直到超过24小时22分钟。一般情况下，
			 * 25小时以内完成8次通知(通知的间隔频率一般是：2m,10m,10m,1h,2h,6h,15h)
			 */
			response.getWriter().println("FAIL");
		}

		return null;

	}

	public void savePayReturn(Map<String, String> params) throws Exception {
		PayReturn payReturn = new PayReturn();
		// 通知时间
		payReturn.setNotifyTime(DateFormatUtil.DEFAULT_FORMAT.parse(params.get("notify_time").toString()));
		// 通知类型
		payReturn.setNotifyType(params.get("notify_type").toString());
		// 通知校验ID
		payReturn.setNotifyId(params.get("notify_id").toString());
		// 签名方式
		payReturn.setSignType(params.get("sign_type").toString());
		// 签名
		payReturn.setSign(params.get("sign").toString());
		// 商户网站唯一订单号
		payReturn.setOutTradeNo(params.get("out_trade_no").toString());
		// 商品名称
		payReturn.setSubject(params.get("subject").toString());
		// 支付类型
		payReturn.setPaymentType("1");
		// 支付宝交易号
		payReturn.setTradeNo(params.get("trade_no").toString());
		// 交易状态
		if (StringUtils.isNotEmpty(params.get("trade_status"))) {
			payReturn.setTradeStatus(params.get("trade_status").toString());
		}
		// 卖家支付宝用户号
		if (StringUtils.isNotEmpty(params.get("seller_id"))) {
			payReturn.setSellerId(params.get("seller_id").toString());
		}
		// 卖家支付宝账号
		if (StringUtils.isNotEmpty(params.get("seller_email"))) {
			payReturn.setSellerEmail(params.get("seller_email").toString());
		}
		// 买家支付宝用户号
		if (StringUtils.isNotEmpty(params.get("buyer_id"))) {
			payReturn.setBuyerId(params.get("buyer_id").toString());
		}
		// 买家支付宝账号 无 buyer_email改为buyer_logon_id
		if (StringUtils.isNotEmpty(params.get("buyer_logon_id"))) {
			payReturn.setBuyerEmail(params.get("buyer_logon_id").toString());
		}
		// 交易金额
		if (StringUtils.isNotEmpty(params.get("total_amount"))) {
			payReturn.setTotalFee(Integer.parseInt(params.get("total_amount").replace(".", "")));
		}
		// 购买数量 无
		if (StringUtils.isNotEmpty(params.get("quantity"))) {
			payReturn.setQuantity(Integer.parseInt(params.get("quantity").toString()));
		}
		// 商品单价 无
		if (StringUtils.isNotEmpty(params.get("price"))) {
			payReturn.setPrice(Integer.parseInt(params.get("price").replace(".", "")));
		}
		// 商品描述
		if (StringUtils.isNotEmpty(params.get("body"))) {
			payReturn.setBody(params.get("body").toString());
		}
		// 交易创建时间
		if (StringUtils.isNotEmpty(params.get("gmt_create"))) {
			payReturn.setGmtCreate(DateFormatUtil.DEFAULT_FORMAT.parse(params.get("gmt_create").toString()));
		}
		// 交易付款时间
		if (StringUtils.isNotEmpty(params.get("gmt_payment"))) {
			payReturn.setGmtPayment(DateFormatUtil.DEFAULT_FORMAT.parse(params.get("gmt_payment").toString()));
		}
		// 是否调整总价 无
		if (StringUtils.isNotEmpty(params.get("is_total_fee_adjust"))) {
			payReturn.setIsTotalFeeAdjust(params.get("is_total_fee_adjust").toString());
		}
		// 是否使用红包买家 无
		if (StringUtils.isNotEmpty(params.get("use_coupon"))) {
			payReturn.setUseCoupon(params.get("use_coupon").toString());
		}
		// 折扣 无
		if (StringUtils.isNotEmpty(params.get("discount"))) {
			payReturn.setDiscount(params.get("discount").toString());
		}
		//新支付
		payReturn.setNewPay(1);
		payReturn.setGmtRefund(new Date());
		log.info("插入支付回调信息:{}", payReturn.toString());
		log.info(">>>>>>>>>>>>>>>>>>>>>>>将回调信息插入到mysql开始<<<<<<<<<<<<<<<<<<<");
		long l = userCorrectGoodsService.savePayReturn(payReturn);
		log.info(">>>>>>>>>>>>>>>>>>>>>>>将回调信息插入到mysql完成<<<<<<<<<<<<<<<<<<<");
	}

}
