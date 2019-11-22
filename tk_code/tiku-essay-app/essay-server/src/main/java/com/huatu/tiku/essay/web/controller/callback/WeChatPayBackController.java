package com.huatu.tiku.essay.web.controller.callback;

import com.github.wxpay.sdk.WXPayUtil;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.constant.status.PayConstant;
import com.huatu.tiku.essay.entity.WeChatPay;
import com.huatu.tiku.essay.entity.WeChatPrePay;
import com.huatu.tiku.essay.repository.WeChatPrePayRepository;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.util.pay.PayCommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by x6 on 2017/11/30.
 */
@RestController
@Slf4j
@RequestMapping("api/v1/weChatPayBack")
/**
 * 微信支付回调
 */
public class WeChatPayBackController {

    @Autowired
    UserCorrectGoodsService userCorrectGoodsService;
    @Autowired
    WeChatPrePayRepository weChatPrePayRepository;

//    @LogPrint
    @PostMapping(value = "sync/{attach}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object weChatPaySync(HttpServletRequest request,
                                HttpServletResponse response,
                                @PathVariable String attach
    ) throws Exception {

        log.info(">>>>>>>>>>>>>>>>>>>>进入微信支付回调接口<<<<<<<<<<<<<<<<<<<<<<<<");

        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        IOUtils.copy(inStream, outStream);

        String result = new String(outStream.toByteArray(), "utf-8");//获取微信调用notify_url的返回信息

        Map<String, String> map = WXPayUtil.xmlToMap(result);
        WeChatPay weChatPay = new WeChatPay();

        log.info(">>>>>>>>>>>>开始验证attach<<<<<<<<<<");
        long orderId = 0;
        if (StringUtils.isNotEmpty(attach)) {
            log.info("attach值为:"+attach);

           WeChatPrePay weChatPrePay = weChatPrePayRepository.findByAttach(attach);
            if (weChatPrePay == null) {
                throw new BizException(PayError.PAY_ATTACH_ERROR);
            }
            if(StringUtils.isNotEmpty(map.get("out_trade_no"))){
                if(! map.get("out_trade_no").equals(weChatPrePay.getOutTradeNo()+"")){
                    throw new BizException(PayError.PAY_ATTACH_ERROR);
                }
            }
            log.info(">>>>>>>>>>>验证成功存在attach<<<<<<<<<<<<<");
            orderId = weChatPrePay.getOrderNum();
        } else {
            log.info(">>>>>>>>>>>attach为空<<<<<<<<<<");
            throw new BizException(PayError.PAY_ATTACH_ERROR);
        }

        // 订单ID
        weChatPay.setOrderId(orderId);

        if (StringUtils.isNotEmpty(map.get("appid"))) {
            weChatPay.setAppId(map.get("appid"));
        }
        if (StringUtils.isNotEmpty(map.get("bank_type"))) {
            weChatPay.setBankType(map.get("bank_type"));
        }
        if (StringUtils.isNotEmpty(map.get("cash_fee"))) {
            weChatPay.setCashFee(map.get("cash_fee"));
        }
        if (StringUtils.isNotEmpty(map.get("fee_type"))) {
            weChatPay.setFeeType(map.get("fee_type"));
        }
        if (StringUtils.isNotEmpty(map.get("is_subscribe"))) {
            weChatPay.setIsSubscribe(map.get("is_subscribe"));
        }
        if (StringUtils.isNotEmpty(map.get("mch_id"))) {
            weChatPay.setMchId(map.get("mch_id"));
        }
        if (StringUtils.isNotEmpty(map.get("nonce_str"))) {
            weChatPay.setNonceStr(map.get("nonce_str"));
        }

        if (StringUtils.isNotEmpty(map.get("out_trade_no"))) {
            weChatPay.setOutTradeNo(map.get("out_trade_no"));
        }
        if (StringUtils.isNotEmpty(map.get("result_code"))) {
            weChatPay.setResultCode(map.get("result_code"));
        }
        if (StringUtils.isNotEmpty(map.get("return_code"))) {
            weChatPay.setReturnCode(map.get("return_code"));
        }
        if (StringUtils.isNotEmpty(map.get("sign"))) {
            weChatPay.setSign(map.get("sign"));
        }
        if (StringUtils.isNotEmpty(map.get("time_end"))) {
            weChatPay.setTimeEnd(map.get("time_end"));
        }
        if (StringUtils.isNotEmpty(map.get("total_fee"))) {
            weChatPay.setTotalFee(map.get("total_fee"));
        }
        if (StringUtils.isNotEmpty(map.get("trade_type"))) {
            weChatPay.setTradeType(map.get("trade_type"));
        }
        if (StringUtils.isNotEmpty(map.get("transaction_id"))) {
            weChatPay.setTransactionId(map.get("transaction_id"));
        }
        if (null != map.get("coupon_fee") && !("").equals(map.get("coupon_fee"))) {
            weChatPay.setCouponFee(map.get("coupon_fee"));
        }
        if (null != map.get("coupon_count") && !("").equals(map.get("coupon_count"))) {
            weChatPay.setCouponCount(map.get("coupon_count"));
        }

        log.info(">>>>>>>>>>>>>>>>>>>>>>>将回调信息插入到mysql开始<<<<<<<<<<<<<<<<<<<");
        long l = userCorrectGoodsService.saveWeChatPay(weChatPay);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>将回调信息插入到mysql完成<<<<<<<<<<<<<<<<<<<");
        //回调状态值：成功
        if (map.get("result_code").equalsIgnoreCase("SUCCESS")) {

            int payType = PayConstant.WE_CHAT_PAY;
            long count = userCorrectGoodsService.countByIdAndPayTypeAndBizStatus(Long.parseLong(map.get("out_trade_no")), payType, EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus());
            log.info("========count======" + count);
            if (0 != count) {
                log.info(">>>>>>>>>>>>>>>>>>>>success已经操作过<<<<<<<<<<<<<<<<<<<<<<<<");

                response.getWriter().write(PayCommonUtil.setXML("SUCCESS", ""));
                System.out.println("-------------"
                        + PayCommonUtil.setXML("SUCCESS", ""));
            } else {
                log.info(">>>>>>>>>>>>>>>>>>>>付款成功<<<<<<<<<<<<<<<<<<<<<<<<");

                int updateUserCorrectGoods = 0;


                // 支付回调业务处理
                try {
                    updateUserCorrectGoods = userCorrectGoodsService.updateUserCorrectGoods(orderId);

                } catch (Exception e) {
                    e.printStackTrace();
                    e.getMessage();
                    log.info("微信支付回调异常" + orderId + e.getMessage());
                    userCorrectGoodsService.shutReturn(orderId);
                }
                if (0 == updateUserCorrectGoods) {
                    response.getWriter().write(
                            PayCommonUtil.setXML("SUCCESS", ""));
                    System.out.println("-------------"
                            + PayCommonUtil.setXML("SUCCESS", ""));
                } else {
                    log.info("支付回调未处理：" + orderId);
                }
            }
            response.getWriter().write(PayCommonUtil.setXML("SUCCESS", ""));
            System.out.println("-------------"
                    + PayCommonUtil.setXML("SUCCESS", ""));
        } else {
            response.getWriter().write(PayCommonUtil.setXML("FAIL", ""));
            System.out.println("-------------" + PayCommonUtil.setXML("FAIL", ""));
        }
        return null;
    }
}
