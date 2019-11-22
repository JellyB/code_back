package com.huatu.tiku.essay.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.google.common.collect.Lists;
import com.huatu.common.CommonErrors;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.encrypt.EncryptUtil;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.service.ZtkUserService;
import com.huatu.tiku.essay.service.impl.correct.UserOrderUtil;
import com.huatu.tiku.essay.util.pay.AliPayConfig;
import com.huatu.tiku.essay.util.pay.AliPayConfigNew;
import com.huatu.tiku.essay.vo.admin.UserAccountDetailVO;
import com.huatu.tiku.essay.vo.admin.UserCorrectGoodsRewardVO;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.util.file.Crypt3Des;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.pay.RequestHandler;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.util.pay.WXPayUtil;
import com.huatu.tiku.essay.vo.user.ZtkUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by x6 on 2017/11/21.
 */
@Service
@Slf4j
public class UserCorrectGoodsServiceImpl implements UserCorrectGoodsService {

    public static final String SECRET = "!@#$%^&*()qazxswedc";
    public static final int PAY_BY_COIN_SUCCESS = 1;
    public static final int PAY_BY_COIN_FAIL = -4;

    @Autowired
    EssayRewardRecordRepository essayRewardRecordRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository;
    @Autowired
    EssayCorrectGoodsRepository essayCorrectGoodsRepository;
    @Autowired
    EssayGoodsOrderRepository essayGoodsOrderRepository;
    @Autowired
    EssayGoodsOrderDetailRepository essayGoodsOrderDetailRepository;
    @Autowired
    EssayPayDetailRepository essayPayDetailRepository;
    @Autowired
    PayReturnRepository payReturnRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private WeChatPayRepository weChatPayRepository;
    @Autowired
    private WeChatPrePayRepository weChatPrePayRepository;
    @Autowired
    private EssayCorrectFreeUserRepository essayCorrectFreeUserRepository;
    @Autowired
    private EssayErrorUserRepository essayErrorUserRepository;
    @Value("${pay_by_coin_url}")
    private String payByCoinUrl;

    @Value("${available_coin_url}")
    private String availableCoinUrl;

    @Value("${find_userid_by_username}")
    private String findUserIdByUserName;
    @Value("${ALI_NOTIFY_URL}")
    private String ALI_NOTIFY_URL;
    
    @Value("${ALI_NOTIFY_URL_V2}")
    private String ALI_NOTIFY_URL_V2;
    
    @Value("${WX_NOTIFY_URL}")
    private String WX_NOTIFY_URL;
    @Value("${login_url}")
    private String loginUrl;
    @Value("${max_correct_time}")
    private Integer maxCorrectTime;
    /**
     * 订单超时关闭的时间
     */
    @Value("${order_close_time}")
    private Integer orderCloseTime;

    @Value("${get_user_account_url}")
    private String getUserAccountUrl;

    @Autowired
    private ZtkUserService ztkUserService;
    
    AlipayClient alipayClient = new DefaultAlipayClient(
            "https://openapi.alipay.com/gateway.do", AliPayConfigNew.app_id,
            AliPayConfigNew.private_key, AliPayConfigNew.format, AliPayConfigNew.input_charset,
            AliPayConfigNew.ali_public_key, AliPayConfigNew.sign_type);
    

    @Override
    public ResponseVO findByUserIdAndBizStatusAndStatus(int userId) {
        ResponseVO vo = new ResponseVO();
        //单题剩余批改次数（初始化为0）
        int singleNum = 0;
        //套题剩余批改次数（初始化为0）
        int multiNum = 0;
        //议论文剩余批改次数
        int argumentationNum = 0;
        if(userId == 0){
            vo.setMaxCorrectTimes(maxCorrectTime);
            vo.setSingleNum(singleNum);
            vo.setMultiNum(multiNum);
            vo.setArgumentationNum(argumentationNum);
            return vo;
        }
        List<EssayUserCorrectGoods> userCorrectGoodsList = essayUserCorrectGoodsRepository.findByUserIdAndBizStatusAndStatus(userId, UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus(), UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isNotEmpty(userCorrectGoodsList)) {
            for (EssayUserCorrectGoods userCorrectGoods : userCorrectGoodsList) {
                if (userCorrectGoods != null) {
                    if (QuestionTypeConstant.SINGLE_QUESTION == userCorrectGoods.getType()) {
                        singleNum = userCorrectGoods.getUsefulNum();
                    } else if (QuestionTypeConstant.PAPER == userCorrectGoods.getType()) {
                        multiNum = userCorrectGoods.getUsefulNum();
                    } else if (QuestionTypeConstant.ARGUMENTATION == userCorrectGoods.getType()) {
                        argumentationNum = userCorrectGoods.getUsefulNum();
                    }
                }
            }
        }
        List<EssayCorrectFreeUser> freeList = essayCorrectFreeUserRepository.findByUserIdAndStatusAndBizStatus
                (userId, EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
        //填充结果（无限次批改剩余时间）
        if (CollectionUtils.isNotEmpty(freeList)) {
            String endDate = DateFormatUtils.format(freeList.get(0).getEndTime(), "yyyy-MM-dd");
            vo.setEndDate(endDate);
        }
        //填充结果（剩余批改总数，每道题目最大可批改次数，单题批改次数，套题批改次数，议论文批改次数）
        vo.setCorrectSum(singleNum + multiNum + argumentationNum);
        vo.setMaxCorrectTimes(maxCorrectTime);
        vo.setSingleNum(singleNum);
        vo.setMultiNum(multiNum);
        vo.setArgumentationNum(argumentationNum);
        return vo;

    }


    @Override
    public OrderResponseVO createOrder(int userId, OrderCreateVO orderCreateVO, int terminal, String userName) {

        //前端计算的总金额
        int payType = orderCreateVO.getPayType();
        Long orderId = orderCreateVO.getOrderId();
        // 填充手机号
        ZtkUserVO ztkUserVO = ztkUserService.getById(userId);

        orderCreateVO.setName(ztkUserVO.getName());
        orderCreateVO.setMobile(ztkUserVO.getMobile());

        EssayGoodsOrder goodsOrder = null;
        if (orderId == null || orderId == 0) {
            goodsOrder = UserOrderUtil.createOrder(userId, orderCreateVO, terminal,
                    essayCorrectGoodsRepository::findByIdIn,
                    essayGoodsOrderRepository::save,
                    essayGoodsOrderDetailRepository::save);
        } else {
            goodsOrder = essayGoodsOrderRepository.findOne(orderId);
        }
        if(null == goodsOrder){
            throw new BizException(EssayErrors.ORDER_ERROR);
        }
        StringBuilder orderNumStr = new StringBuilder(goodsOrder.getOrderNumStr());
        long orderNum = goodsOrder.getId();
        int total = goodsOrder.getTotalMoney();


        //判断支付类型，进行不同操作
        if (PayConstant.ALI_PAY == payType) {
            //支付宝支付
            //金额转成:元
            StringBuilder moneySum = new StringBuilder();
            if (0 == total % 100) {
                moneySum.append(total / 100);
            } else {
                moneySum.append(total / (double) 100);
            }
//            String orderString = getOrderString("华图在线-购买课程", "华图在线-购买课程", orderNumStr.toString(), moneySum.toString(), ALI_NOTIFY_URL);

            OrderResponseVO orderResponseVO = OrderResponseVO.builder()
                    //订单编号
                    .orderNum(orderNum + "")
                    .orderNumStr(orderNumStr.toString())
                    //总金额
                    .moneySum(moneySum.toString())
                    //描述
                    .description("华图在线-批改商品" + orderNumStr.toString())
                    //标题
                    .title("华图在线-批改商品")
//                    .orderStr(orderString)
                    .notifyUrl(ALI_NOTIFY_URL)
                    .build();
			if (orderCreateVO.getNewPay() != null && orderCreateVO.getNewPay() == 1) {
				// 使用新支付宝支付
				String orderString = getOrderString("华图在线-批改商品"+orderNumStr, "华图在线-批改商品", orderNum + "", moneySum.toString(),
						ALI_NOTIFY_URL_V2);
				log.info("签名字符串为:{}", orderString);
				orderResponseVO.setOrderStr(orderString);
				orderResponseVO.setNotifyUrl(ALI_NOTIFY_URL_V2);
			}
            return orderResponseVO;
        } else if (PayConstant.WE_CHAT_PAY == payType) {
            //微信支付
            try {
                //获取预约号
                OrderResponseVO vo = unifiedOrder(orderNum, total, orderNumStr.toString());
                return vo;
            } catch (Exception e) {
                log.info("微信支付异常");
                e.printStackTrace();
            }
        } else if (PayConstant.COIN_PAY == payType) {
            //调用服务：金币支付（1元对应100金币）
            int payByCoin = payByCoin(orderNum, userName, total);
            if (PAY_BY_COIN_SUCCESS == payByCoin) {
                //更新商品库存&&用户批改次数
                int i = updateUserCorrectGoods(orderNum);
            } else if (PAY_BY_COIN_FAIL == payByCoin) {
                log.info("当前账户图币余额不足");
                throw new BizException(EssayErrors.LOW_COIN);
            } else {
                log.info("图币支付异常");
                throw new BizException(CommonErrors.SERVICE_INTERNAL_ERROR);
            }
            return OrderResponseVO.builder()
                    .orderNum(orderNum + "")
                    .build();
        } else {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        return null;
    }

    @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(value = 1000))
    public int payByCoin(long orderNum, String userName, int total) {


        //进入金币支付环节
        String url = payByCoinUrl;
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String encodeUserName = "";
        try{
            encodeUserName  = URLEncoder.encode(userName, "UTF-8");
        }catch (UnsupportedEncodingException e){
            encodeUserName = userName;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("gold=" + total);
        sb.append("&ordernum=" + orderNum);
        sb.append("&timestamp=" + timestamp);
        sb.append("&username=" + encodeUserName);
        sb.append(SECRET);
        log.info(sb.toString());
        String sign = EncryptUtil.md5(sb.toString());

        StringBuffer sbSign = new StringBuffer();
        sbSign.append("username=" + encodeUserName);
        sbSign.append("&ordernum=" + orderNum);
        sbSign.append("&gold=" + total);
        sbSign.append("&timestamp=" + timestamp);
        sbSign.append("&sign=" + sign);
        log.info("sbSign:{}", sbSign);
        String p = Crypt3Des.encryptMode(sbSign.toString());

        /**
         * 1 :支付成功
         * -4 ：账户金币余额不足
         */
        log.info("金币支付：发送get请求，url = {}", url + "?p=" + p);
        ResponseEntity<ResponseMsg> forEntity = restTemplate.getForEntity(url + "?p=" + p, ResponseMsg.class);
        ResponseMsg body = forEntity.getBody();
        if (null != body) {
            log.info("get 请求发送成功");
            return body.getCode();
        } else {
            return -1;
        }

    }

    @Override
    public long countDetail(int userId, Date date) {

        long count = essayGoodsOrderRepository.countByUserIdAndBizStatusAndStatusAndGmtModifyGreaterThan
                (userId, EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus(), EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus(), date);

        return count;
    }

    @Override
    public long saveWeChatPay(WeChatPay weChatPay) {
        weChatPay = weChatPayRepository.save(weChatPay);
        return weChatPay.getId();
    }

    @Override
    public List<EssayGoodsOrderVO> detail(Pageable pageable, int userId, Date date) {

        List<EssayGoodsOrder> detailList = essayGoodsOrderRepository.findByUserIdAndBizStatusAndStatusAndGmtModifyGreaterThan
                (pageable, userId, EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus(), EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus(), date);
        LinkedList<EssayGoodsOrderVO> list = new LinkedList<>();
        for (EssayGoodsOrder detail : detailList) {
            EssayGoodsOrderVO vo = new EssayGoodsOrderVO();

            BeanUtils.copyProperties(detail, vo);
            StringBuilder payMsg = new StringBuilder();
            /**  收支类型  0收入  1支出 **/
            if (0 == vo.getIncomeType()) {
                payMsg.append("+");
            } else if (1 == vo.getIncomeType()) {
                payMsg.append("-");
            }
            vo.setPayTime(detail.getGmtModify());
            /* 0 支付宝  1微信  2金币    */
            if (PayConstant.ALI_PAY == vo.getPayType() || PayConstant.WE_CHAT_PAY == vo.getPayType()) {
                if (0 == vo.getTotalMoney() % 100) {
                    payMsg.append(vo.getTotalMoney() / 100).append("元");
                } else {
                    payMsg.append(vo.getTotalMoney() / (double) 100).append("元");
                }

            } else if (PayConstant.COIN_PAY == vo.getPayType()) {
                payMsg.append(vo.getTotalMoney());
                payMsg.append("图币");
            }
            vo.setPayMsg(payMsg.toString());
            list.add(vo);
        }
        return list;
    }

    @Override
    public ResponseVO check(int userId, int type) {
        int exist = 1;

        List<EssayUserCorrectGoods> list = essayUserCorrectGoodsRepository.findByUserIdAndBizStatusAndStatusAndType(userId, UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus(), UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus(), type);

        if (CollectionUtils.isNotEmpty(list) && list.get(0) != null) {
            exist = 0 < list.get(0).getUsefulNum() ? 0 : 1;
        } else {
            exist = 1;
        }

        List<EssayCorrectFreeUser> freeUsers = essayCorrectFreeUserRepository.findByUserIdAndStatusAndBizStatus
                (userId, EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
        if (CollectionUtils.isNotEmpty(freeUsers)) {
            exist = 0;
        }
        ResponseVO vo = ResponseVO.builder().exist(exist).build();
        return vo;
    }

    @Override
    public long savePayReturn(PayReturn payReturn) {
        payReturn = payReturnRepository.save(payReturn);

        return payReturn.getId();
    }

    @Override
    public long countByIdAndPayTypeAndBizStatus(long id, int payType, int bizStatus) {
        return essayGoodsOrderRepository.countByIdAndPayTypeAndBizStatus(id, payType, bizStatus);
    }


    //查询用户剩余金币数量
    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(value = 1000))
    public UserAccountDetailVO coin(String userName) {

        //进入查询用户余额环节
        String url = availableCoinUrl;
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        int action = 1;//1账户余额
        StringBuffer sb = new StringBuffer();

        sb.append("action=" + action);
        sb.append("&username=" + userName);
        sb.append(SECRET);
        log.info(sb.toString());
        String sign = EncryptUtil.md5(sb.toString().toLowerCase());

        StringBuffer sbSign = new StringBuffer();
        sbSign.append("username=" + userName);
        sbSign.append("&action=" + action);
        sbSign.append("&sign=" + sign);
        String p = Crypt3Des.encryptMode(sbSign.toString());

        log.info("查询用户余额：发送get请求，url = {}", url + "?p=" + p);
        ResponseEntity<ResponseMsg> forEntity = restTemplate.getForEntity(url + "?p=" + p, ResponseMsg.class);
        ResponseMsg body = forEntity.getBody();

        if (null != body) {
            log.info("get 请求发送成功");
            String data = Crypt3Des.decryptMode(body.getData().toString());

            UserAccountVO userAccountVO = JSON.parseObject(data, UserAccountVO.class);
            return userAccountVO.getUserCountres();
        } else {
            return null;
        }
    }


    /**
     * 将订单涉及的批改次数添加到用户批改次数表中
     * @param orderId
     * @return
     */
    @Override
    public int updateUserCorrectGoods(long orderId) {

        //修改订单表
        EssayGoodsOrder order = essayGoodsOrderRepository.findOne(orderId);
        order.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus());//修改订单状态为已支付
        order.setRealMoney(order.getTotalMoney());//修改实际支付金额
        order.setPayTime(new Date());
        essayGoodsOrderRepository.save(order);

        List<EssayGoodsOrderDetail> detailList = essayGoodsOrderDetailRepository.findByRecordId(orderId);

        for (EssayGoodsOrderDetail detail : detailList) {
            EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.create(detail.getGoodsType());
            if (null == goodsTypeEnum) {
                throw new IllegalArgumentException("无效的商品类型，异常事物回滚");
            }
            long goodsId = detail.getGoodsId();
            int buyCount = detail.getCount();
            //更新商品销售数量
            EssayCorrectGoods good = essayCorrectGoodsRepository.findOne(goodsId);
            good.setSalesNum(good.getSalesNum() + buyCount);
            essayCorrectGoodsRepository.save(good);
            // 更新订单明细状态
            detail.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus());
        }
        essayGoodsOrderDetailRepository.save(detailList);
        UserOrderUtil.updateUserCorrectTime(order,
                detailList,
                essayUserCorrectGoodsRepository,
                getAllDetails
        );
        // 更新订单明细状态

//        updateUserCorrectTimes(queNum, mulNum, argNum, userId);
        return 1;
    }

    /**
     * 修改
     */
    Function<EssayUserCorrectGoods,Integer> updateSelective = (essayUserCorrectGoods ->
            essayUserCorrectGoodsRepository.modifyByUserIdAndGoodsType(essayUserCorrectGoods.getUserId(),
            essayUserCorrectGoods.getType(),
            essayUserCorrectGoods.getTotalNum(),
            essayUserCorrectGoods.getUsefulNum(),
            essayUserCorrectGoods.getSpecialNum(),
            essayUserCorrectGoods.getIsLimitNum(),
            essayUserCorrectGoods.getExpireTime()));
    /**
     * 用户批改总表查询
     */
    BiFunction<Integer, Integer, List<EssayUserCorrectGoods>> getUserGoods = ((userId,goodsType)->
            essayUserCorrectGoodsRepository.findByUserIdAndBizStatusAndStatusAndType(userId,
            UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus(),
            UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus(),
            goodsType));

    /**
     * 查询用户所有订单详情
     */
    BiFunction<Integer, Integer, List<EssayGoodsOrderDetail>> getAllDetails = ((userId,goodsType)->{
        List<EssayGoodsOrderDetail> byUserIdAndGoodsType = essayGoodsOrderDetailRepository.findByUserIdAndGoodsType(userId, goodsType);
        return byUserIdAndGoodsType;
    });
    private void updateUserCorrectTimes(int queNum, int mulNum, int argNum, int userId) {
        int queModify = 0;
        int mulModify = 0;
        int argModify = 0;

        if (0 != queNum) {
            log.info(">>>>>>更新用户批改次数表<<<<<<");
            //查询用户批改次数
            long count = essayUserCorrectGoodsRepository.countByUserIdAndTypeAndStatusAndBizStatus(userId, 0, UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus(), UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus());
            if (0 == count) {
                EssayUserCorrectGoods userCorrectGoods = EssayUserCorrectGoods.builder()
                        .totalNum(queNum)
                        .usefulNum(queNum)
                        .type(0)
                        .userId(userId)
                        .isLimitNum(1)
                        .build();
                userCorrectGoods.setStatus(UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus());
                userCorrectGoods.setBizStatus(UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus());
                try {
                    essayUserCorrectGoodsRepository.save(userCorrectGoods);
                } catch (Exception e) {
                    throw new IllegalArgumentException("订单支付回调失败 ，异常回滚事物");
                }

            } else {
                queModify = essayUserCorrectGoodsRepository.modifyUsefulNumAndTotalNumByUserIdAndType(queNum, userId, 0);
                if (1 != queModify) {
                    throw new IllegalArgumentException("订单支付回调失败 ，异常回滚事物");
                }
            }

        }
        if (0 != mulNum) {
            long count = essayUserCorrectGoodsRepository.countByUserIdAndTypeAndStatusAndBizStatus(userId, 1, UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus(), UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus());
            if (0 == count) {
                EssayUserCorrectGoods userCorrectGoods = EssayUserCorrectGoods.builder()
                        .totalNum(mulNum)
                        .usefulNum(mulNum)
                        .type(1)
                        .userId(userId)
                        .isLimitNum(1)
                        .build();
                userCorrectGoods.setStatus(UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus());
                userCorrectGoods.setBizStatus(UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus());
                try {
                    essayUserCorrectGoodsRepository.save(userCorrectGoods);
                } catch (Exception e) {
                    throw new IllegalArgumentException("订单支付回调失败 ，异常回滚事物");
                }
            } else {
                mulModify = essayUserCorrectGoodsRepository.modifyUsefulNumAndTotalNumByUserIdAndType(mulNum, userId, 1);
                if (1 != mulModify) {
                    throw new IllegalArgumentException("订单支付回调失败 ，异常回滚事物");
                }
            }
        }

        if (0 != argNum) {
            log.info(">>>>>>更新用户批改次数表<<<<<<");
            //查询用户批改次数
            long count = essayUserCorrectGoodsRepository.countByUserIdAndTypeAndStatusAndBizStatus(userId, 2, UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus(), UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus());
            if (0 == count) {
                EssayUserCorrectGoods userCorrectGoods = EssayUserCorrectGoods.builder()
                        .totalNum(argNum)
                        .usefulNum(argNum)
                        .type(2)
                        .userId(userId)
                        .isLimitNum(1)
                        .build();
                userCorrectGoods.setStatus(UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus());
                userCorrectGoods.setBizStatus(UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus());
                try {
                    essayUserCorrectGoodsRepository.save(userCorrectGoods);
                } catch (Exception e) {
                    throw new IllegalArgumentException("订单支付回调失败 ，异常回滚事物");
                }

            } else {
                argModify = essayUserCorrectGoodsRepository.modifyUsefulNumAndTotalNumByUserIdAndType(argNum, userId, 2);
                if (0 == argModify) {
                    throw new IllegalArgumentException("订单支付回调失败 ，异常回滚事物");
                }
            }

        }
    }

    @Override
    public void shutReturn(long orderId) {
        //根据id查询订单，置为异常状态
        EssayGoodsOrder order = essayGoodsOrderRepository.findOne(orderId);
        order.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYEXCEPTION.getBizStatus());//修改订单状态为：支付异常
        essayGoodsOrderRepository.save(order);
        List<EssayGoodsOrderDetail> orderDetails = essayGoodsOrderDetailRepository.findByRecordId(orderId);
    }

    private String getServerIp() throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();//获取的是本地的IP地址 //PC-20140317PXKX/192.168.0.121
        String hostAddress = address.getHostAddress();//192.168.0.121
        return hostAddress;
    }

    /**
     * 获取微信预约号
     */
    @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(value = 1000))
    public OrderResponseVO unifiedOrder(Long orderNo, int totalMoney, String orderNumStr) throws Exception {
        log.info(">>>>>>>>>>>>>>>>>>>>微信支付调用接口<<<<<<<<<<<<<<<<<<<<<<<<");

        //appId 应用id
        String appId = SystemConstant.APP_ID;
        //商户ID
        String mch_id = SystemConstant.MCH_ID;
        //设备号（默认为WEB，非必填项）
        String device_info = "WEB";
        // 随机数
        String nonce_str = UUID.randomUUID().toString().replaceAll("-", "");
        //附加数据（验证回调是微信的请求）
        String attach = UUID.randomUUID().toString().replaceAll("-", "");
        // 签名
        String sign = "";
        // 商品描述根据情况修改（必须）
        String body = "华图在线-批改商品" + orderNumStr;
        //商户订单号
        String out_trade_no = getOutTradeNo(orderNo);

        int total_fee = totalMoney;
        // 订单生成的机器 IP
        String spbill_create_ip = getServerIp();
        //支付ID
        String partnerKey = SystemConstant.PARTNER_KEY;//在微信商户平台pay.weixin.com里自己生成的那个key
        // 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。（回调链接）
        String notify_url = WX_NOTIFY_URL;
        //交易类型
        String trade_type = "APP";

        //appSecret
        String appSecret = SystemConstant.APP_SECRET;


        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", appId);
        packageParams.put("attach", attach);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("body", body);
        packageParams.put("out_trade_no", getOutTradeNo(orderNo));

        // 这里写的金额为1分到时修改
        packageParams.put("total_fee", total_fee + "");
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url + "/" + attach);
        packageParams.put("trade_type", trade_type);
        RequestHandler reqHandler = new RequestHandler(null, null);
        reqHandler.init(appId, appSecret, partnerKey);

        //参数签名
        sign = reqHandler.createSign(packageParams);
        String xml = "<xml>" + "<appid>" + appId + "</appid>" + "<attach>" + attach + "</attach>" + "<mch_id>"
                + mch_id + "</mch_id>" + "<nonce_str>" + nonce_str
                + "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>"
                + "<body><![CDATA[" + body + "]]></body>"
                + "<out_trade_no>"
                + out_trade_no
                + "</out_trade_no>"
                +
                "<total_fee>"
                + total_fee
                + "</total_fee>"
                + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>"
                + "<notify_url>" + notify_url + "/" + attach + "</notify_url>"
                + "<trade_type>" + trade_type + "</trade_type>"
                + "</xml>";
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>统一支付接口获取预支付订单，请求参数" + xml + "<<<<<<<<<<<<<<<<<<<<<<");

        String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        //预支付订单id
        String prepay_id = "";
        xml = new String(xml.getBytes("UTF-8"), "ISO-8859-1");
        try {
            String jsonStr = restTemplate.postForObject(createOrderURL, xml, String.class);

            Map map = WXPayUtil.xmlToMap(jsonStr);
            jsonStr = new String(jsonStr.getBytes("ISO-8859-1"), "UTF-8");
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>" + "统一支付接口获取预支付订单，返回数据" + jsonStr + "<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            String return_code = (String) map.get("return_code");
            String return_msg = (String) map.get("return_msg");
            if (return_code.equals("FAIL")) {

                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>" + "统一支付接口获取预支付订单出错,返回业务码:" + return_code +
                        "错误信息" + return_msg + "<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            }
            prepay_id = (String) map.get("prepay_id");
            if (null == prepay_id || ("").equals(prepay_id)) {
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>" + "统一支付接口获取预支付订单出错,返回业务码:" + return_code + "<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            }
        } catch (Exception e1) {
            e1.printStackTrace();

        }
        SortedMap<String, String> finalPackage = new TreeMap<String, String>();

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        finalPackage.put("appid", appId);
        finalPackage.put("partnerid", mch_id);
        finalPackage.put("timestamp", timestamp);
        finalPackage.put("noncestr", nonce_str);
        finalPackage.put("prepayid", prepay_id);
        finalPackage.put("package", "Sign=WXPay");
        String finalsign = reqHandler.createSign(finalPackage);

        //保存预约号等信息

        OrderResponseVO vo = OrderResponseVO.builder()
                .appId(appId)
                .orderNum(orderNo+"")
                .partnerId(mch_id)
                .timestamp(timestamp)
                .nonceStr(nonce_str)
                .prepayId(prepay_id)
                .packageValue("Sign=WXPay")
                .sign(finalsign)
                .build();

        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>返回成功<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        //存入数据库
        WeChatPrePay weChatPrePay = new WeChatPrePay();
        weChatPrePay.setAttach(attach);
        weChatPrePay.setNonceStr(vo.getNonceStr());
        weChatPrePay.setOrderNum(orderNo);
        weChatPrePay.setOutTradeNo(out_trade_no);
        weChatPrePayRepository.save(weChatPrePay);
        log.info(">>>>>>>>>>>>>>>>>>>>attach已存入数据库值为:" + attach + "<<<<<<<<<<<<<<<<<<<<");
        return vo;
    }


    /**
     * 获取APP支付订单信息
     */
    public String getOrderString(String body, String subject, String outtradeno,
                                 String totalAmount, String notifyUrl) {
       
        // 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        // SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody(body);
        model.setSubject(subject);
        model.setOutTradeNo(outtradeno);
        // 暂时使用过默认值
        // model.setTimeoutExpress("30m");
        model.setTotalAmount(totalAmount);
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        try {
            // 这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient
                    .sdkExecute(request);
            // 就是orderString
            // 可以直接给客户端请求，无需再做处理。
            return response.getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝APP支付信息生成失败", e);
        }

        return null;
    }

    /**
     * 微信去获取预约号的outTradeNo
     *
     * @param orderId
     * @return
     */
    private String getOutTradeNo(Long orderId) {
        SimpleDateFormat date = new SimpleDateFormat("HHmmss");
        long temp = orderId % 100000L;

        DecimalFormat df = new DecimalFormat("00000");
        String tempStr = df.format(temp);
        String format = date.format(new Date());
        String outTradeNo = format + tempStr;

        return outTradeNo;
    }

    /**
     * 根据用户名 更新用户批改次数
     *
     * @return
     */
    @Override
    public Object updateCorrectTimesByUser(UpdateCorrectTimesOrderVO updateVO, int userId, long orderId) {

        boolean flag = false;

        //为用户添加相应批改次数
        int queNum = updateVO.getQueNum();
        int mulNum = updateVO.getMulNum();
        int argNum = updateVO.getArgNum();
        updateUserCorrectTimes(queNum, mulNum, argNum, userId);

        //插入用户明细数据
        EssayPayDetail essayPayDetail = EssayPayDetail.builder()
                .incomeType(PayConstant.INCOME_TYPE_OUT)
                .orderId(orderId)
                .payType(PayConstant.FREE_GIFT_OF_COURSE)
                .payMoney(0)
                .userId(userId)
                .build();

        essayPayDetail = essayPayDetailRepository.save(essayPayDetail);
        if (essayPayDetail.getId() != 0) {
            flag = true;
        }
        return flag;
    }


    /**
     * 批改免费的白名单中加入用户V1（结束时间默认2018年4月30日 23:59:59）
     *
     * @param vo
     * @return
     */
    @Override
    public boolean addFreeUser(UpdateCorrectTimesOrderVO vo, int userId, long orderId) {
        boolean flag = false;

        List<EssayCorrectFreeUser> freeUsers = essayCorrectFreeUserRepository.findByUserIdAndStatusAndBizStatus
                (userId, EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
        if (CollectionUtils.isNotEmpty(freeUsers)) {
            //用户已存在在白名单中，请勿重复添加
            log.warn("用户已存在在白名单中,请勿重复添加。userId:{}", userId);
//            throw new BizException(EssayErrors.FREE_USER_EXIST);
        } else {
            //将用户信息插入表中
            EssayCorrectFreeUser freeUser = EssayCorrectFreeUser.builder().orderId(orderId)
                    .userId(userId)
                    .startTime(new Date())
                    //暂时写死  2018年4月30日 23:59:59
                    .endTime(new Date(1525103999000L))
                    .build();
            freeUser.setStatus(EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus());
            freeUser.setBizStatus(EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
            essayCorrectFreeUserRepository.save(freeUser);
            if (freeUser.getId() != 0) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 白名单加入用户（自定义结束时间）
     *
     * @param updateVO
     * @param userId
     * @param orderId
     * @return
     */
    @Override
    public boolean addFreeUserV2(UpdateCorrectTimesOrderVO updateVO, int userId, Long orderId) {
        boolean flag = false;

        List<EssayCorrectFreeUser> freeUsers = essayCorrectFreeUserRepository.findByUserIdAndStatusAndBizStatus
                (userId, EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
        //将用户信息插入表中
        EssayCorrectFreeUser freeUser = EssayCorrectFreeUser.builder().orderId(orderId)
                .userId(userId)
                .startTime(new Date())
                //暂时写死  2018年4月30日 23:59:59
                .endTime(new Date(updateVO.getEndTime()))
                .build();
        freeUser.setStatus(EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus());
        freeUser.setBizStatus(EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
        essayCorrectFreeUserRepository.save(freeUser);
        if (freeUser.getId() != 0) {
            flag = true;
        }
        return flag;
    }


    /**
     * 从批改免费的白名单中移除用户
     *
     * @param vo
     * @return
     */
    @Override
    public boolean delFreeUser(UpdateCorrectTimesOrderVO vo, int userId, long orderId) {
        boolean flag = false;

        List<EssayCorrectFreeUser> freeUsers = essayCorrectFreeUserRepository.findByUserIdAndStatusAndBizStatus
                (userId, EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());

        if (CollectionUtils.isEmpty(freeUsers)) {
            //用户不在白名单中,删除失败
            log.warn("用户不在白名单中,删除失败。userId:{}", userId);
//            throw new BizException(EssayErrors.FREE_USER_NOT_EXIST);
        } else {
            //将用户信息状态置为删除状态
            EssayCorrectFreeUser freeUser = freeUsers.get(0);
            freeUser.setStatus(EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.DELETED.getStatus());
            essayCorrectFreeUserRepository.save(freeUser);
            flag = true;
        }
        return flag;
    }

    @Override
    public Object delCorrectTimesByUser(UpdateCorrectTimesOrderVO updateVO, int userId, long orderId) {
        boolean flag = false;

        //为用户添加相应批改次数
        int queNum = 0 - updateVO.getQueNum();
        int mulNum = 0 - updateVO.getMulNum();
        int argNum = 0 - updateVO.getArgNum();
        updateUserCorrectTimes(queNum, mulNum, argNum, userId);

        //插入用户明细数据
        EssayPayDetail essayPayDetail = EssayPayDetail.builder()
                .incomeType(PayConstant.INCOME_TYPE_OUT)
                .orderId(orderId)
                .payType(PayConstant.DEL_FOR_CANCEL_COURSE)
                .payMoney(0)
                .userId(userId)
                .build();

        essayPayDetail = essayPayDetailRepository.save(essayPayDetail);
        if (essayPayDetail.getId() != 0) {
            flag = true;
        }
        return flag;
    }


    //根据用户昵称查询用户id
    @Override
    public int findIdByName(String userName) {
        //根据userName查询userId(调用user服务)
        String url = findUserIdByUserName;
        log.info("根据userName查询userId(调用user服务)：发送get请求，url = {}", url + "?userName=" + userName);
        ResponseEntity<ResponseMsg> forEntity = restTemplate.getForEntity(url + "?userName=" + userName, ResponseMsg.class);
        ResponseMsg body = forEntity.getBody();
        int userId = 0;
        if (null != body && null != body.getData()) {
            log.info("get 请求发送成功");
            userId = Integer.parseInt(body.getData().toString());
        } else {
            log.warn("查询用户名失败。userName：{}", userName);
//            throw new BizException(EssayErrors.USER_NAME_ERROR);
        }

        return userId;
    }

    @Override
    public int fakeLogin(String userName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        headers.add("cv", "6.1.1");
        headers.add("terminal", "1");
        //根据userName查询userId(调用user服务)
        String url = loginUrl + "?password=huatuessay20180226&account=" + userName;
        log.info("根据用户名，模拟登录，同步用户信息(调用user服务)：发送post请求，url = {}", url);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(null, headers);
        ResponseEntity<ResponseMsg> resp = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ResponseMsg.class);

        ResponseMsg body = resp.getBody();
        int userId = 0;
        if (null != body || null != body.getData()) {
            log.info("post 请求发送成功,模拟登录成功");

        } else {
            log.warn("post 请求发送失败,模拟登录失败。userName：{}", userName);
        }
        return userId;
    }

    @Override
    public boolean importCourse() {
        //1.先读取文件，转换成bean（根据需要改文件名称）
        List<ImportVO> beans = readExcelToCopyBean("C:\\Users\\x6\\Desktop\\交接文档\\123321.xls");

        log.info("beans.size = {}", beans.size());
        LinkedList<String> nameList = new LinkedList<>();


        //2.遍历list，查询用户id，修改用户的批改次数
        int i = 0;
        for (ImportVO vo : beans) {
            i++;
            //根据userName查询userId(调用user服务)
            String url = "http://192.168.100.212/u/v1/users/userId";
//            log.info("根据userName查询userId(调用user服务)：发送get请求，url = {}", url + "?userName=" + vo.getUserName());
            ResponseEntity<ResponseMsg> forEntity = restTemplate.getForEntity(url + "?userName=" + vo.getUserName(), ResponseMsg.class);
            ResponseMsg body = forEntity.getBody();
            int userId = 0;
            if (null != body.getData()) {
                log.info("第{}次get 请求发送成功", i);
                userId = Integer.parseInt(body.getData().toString());
            } else {
                nameList.add(vo.getUserName());
                ErrorUser build = ErrorUser.builder()
                        .userName(vo.getUserName())
                        .strOrder(vo.getStrOrder())
                        .phone(vo.getPhone())
//                      .count(vo.getCount())
                        .build();
                essayErrorUserRepository.save(build);
                log.warn("用户名称查询不到对应用户，userName:{}", vo.getUserName());
            }

            if (userId > 0) {
                List<EssayCorrectFreeUser> freeList = essayCorrectFreeUserRepository.findByUserIdAndStatusAndBizStatus
                        (userId, EssayCorrectFreeUserConstant.EssayCorrectFreeUserStatusEnum.NORMAL.getStatus(), EssayCorrectFreeUserConstant.EssayCorrectFreeUserBizStatusEnum.ONLINE.getBizStatus());
                //白名单中没有用户信息时，插入记录
                if (CollectionUtils.isEmpty(freeList)) {
                    EssayCorrectFreeUser build = EssayCorrectFreeUser.builder()
                            .endTime(new Date(1525103999000L))
                            .startTime(new Date(System.currentTimeMillis()))
                            .userId(userId)
                            .orderId(888888)
                            .build();
                    build.setBizStatus(1);
                    build.setStatus(1);
                    build.setCreator("zx0211pm");
                    essayCorrectFreeUserRepository.save(build);
                }
            }

//            updateUserCorrectTimes(0, 42, userId);
//
//            //插入用户明细数据
//            EssayPayDetail essayPayDetail = EssayPayDetail.builder()
//                    .incomeType(PayConstant.INCOME_TYPE_OUT)
//                    .orderId(vo.getOrderId())
//                    .payType(PayConstant.FREE_GIFT_OF_COURSE)
//                    .payMoney(0)
//                    .userId(userId)
//                    .build();
//
//            essayPayDetail = essayPayDetailRepository.save(essayPayDetail);

        }
        log.info(nameList + "");

        return true;
    }


    public static List<ImportVO> readExcelToCopyBean(String path) {
        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(new File(path));
            List<ImportVO> beans = readExcel1(wb, 0, 0, 20100);
            return beans;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<ImportVO> readExcel1(Workbook wb, int sheetIndex, int startReadLine, int tailLine) {
        Sheet sheet = wb.getSheetAt(sheetIndex);
        Row row = null;
        List<ImportVO> questionBeanList = Lists.newLinkedList();
        for (int i = startReadLine; i < sheet.getLastRowNum(); i++) {
//            if(i>TOTAL_LINE){
//                break;
//            }
            row = sheet.getRow(i);  //行
            ImportVO bean = new ImportVO();

            bean.setIndex(i);

            for (int l = 0; l < 5; l++) {     //单元格
                //判断是否具有合并单元格
                try {
                    Cell c = row.getCell(l);
                    String value = getCellValue(c).replaceAll("\r\n", "").trim();
                    if (value.contains(".")) {
                        int index = value.indexOf(".");
                        value = value.substring(0, index);
                    }
                    if (value == null || "".equals(value)) {
                        continue;
                    }
                    switch (l) {
                        case 0:
                            bean.setStrOrder(value);
                            break;
                        case 3:
                            bean.setUserName("0" + value);
                            break;
//                        case 4:
//                            bean.setPhone(value);
//                            break;
                    }
                } catch (Exception e) {
//                    continue;
                    log.error("第{}行的第{}列单元格内容格式错误", i + 1, l + 1);
                    e.printStackTrace();
                }
            }

            questionBeanList.add(bean);
        }
        return questionBeanList;
    }


    /**
     * 获取单元格的值
     *
     * @param cell
     * @return
     */
    public static String getCellValue(Cell cell) {

        String cellValue = "";
        if (cell == null) {
            return "";
        }else{
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    Boolean val1 = cell.getBooleanCellValue();
                    cellValue = val1.toString();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    DecimalFormat df = new DecimalFormat("0");
                    cellValue = df.format(cell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_BLANK:
                    break;
                default:
                    throw new BizException(EssayErrors.ERROR_CELL_TYPE);
            }

        }
        return cellValue;
    }


    /**
     * 订单列表
     *
     * @param type
     * @param pageable
     * @return
     */
    @Override
    public PageUtil<EssayOrderVO> orderList(int userId, int type, Pageable pageable) {
        Specification specification = queryOrderSpecific(userId, type);
        Page<EssayGoodsOrder> page = essayGoodsOrderRepository.findAll(specification, pageable);

        List<EssayGoodsOrder> resultList = page.getContent();
        LinkedList<EssayOrderVO> essayOrderVOS = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(resultList)) {
            List<EssayCorrectGoods> allGoods = essayCorrectGoodsRepository.findAll();
            for (EssayGoodsOrder order : resultList) {
                EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum bizStatusEnum = EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.create(order.getBizStatus());
                if(null == bizStatusEnum){
                    continue;
                }
                //查询订单信息
                EssayOrderVO vo = EssayOrderVO.builder()
                        .id(order.getId())
                        .orderNumStr(order.getOrderNumStr())
                        .payType(order.getPayType())
                        .realMoney(order.getRealMoney())
                        .totalMoney(order.getTotalMoney())
                        .userId(order.getUserId())
                        .bizStatus(order.getBizStatus())
                        .bizStatusName(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.getName(order.getBizStatus()))
                        .createTime(order.getGmtCreate())
                        .payTime((bizStatusEnum.equals(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED)) ? order.getGmtModify() : null)
                        .cancelTime((bizStatusEnum.equals(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.CANCEL)||
                                bizStatusEnum.equals(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.TIMEOUT))
                                ? order.getGmtModify() : null)
                        .closeTime(bizStatusEnum.equals(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.INIT) ? (getDayBefore(order.getGmtCreate(), -orderCloseTime)) : null)
                        .build();


                if (bizStatusEnum.equals(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.INIT)) {
                    long now = System.currentTimeMillis();
                    long end = vo.getCloseTime().getTime();
                    vo.setLeftTime((end - now) / 1000);
                }

                //查询订单商品
                List<EssayGoodsOrderDetail> goodsList = essayGoodsOrderDetailRepository.findByRecordId(order.getId());
                if (CollectionUtils.isNotEmpty(goodsList)) {
                    LinkedList<OrderGoodsVO> orderGoodsVOS = new LinkedList<>();
                    for (EssayGoodsOrderDetail orderGoods : goodsList) {
                        for (EssayCorrectGoods goods : allGoods) {
                            if (orderGoods.getGoodsId() == goods.getId()) {
                                OrderGoodsVO goodsVO = OrderGoodsVO.builder()
                                        .count(orderGoods.getCount())
                                        .goodsId(orderGoods.getGoodsId())
                                        .name(goods.getName())
                                        .correctMode(CorrectModeEnum.create(orderGoods.getCorrectMode()).getMode())
                                        .expireFlag(orderGoods.getExpireFlag())
                                        .expireDate(orderGoods.getExpireTime())
                                        .build();
                                orderGoodsVOS.add(goodsVO);
                            }
                        }
                    }
                    vo.setGoodsList(orderGoodsVOS);
                }
                essayOrderVOS.add(vo);
            }
        }
        long totalElements = page.getTotalElements();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        PageUtil p = PageUtil.builder()
                .result(essayOrderVOS)
                .next(totalElements > (pageNumber + 1) * pageSize ? 1 : 0)
                .total(totalElements)
                .totalPage((0 == totalElements % pageSize) ? (totalElements / pageSize) : (totalElements / pageSize + 1))
                .build();
        return p;
    }


    /**
     * 根据订单状态查询列表
     * PAYED(1, "支付成功"), CANCEL(2, "取消支付"),PAYEXCEPTION(3, "支付异常"),TIMEOUT(4, "超时取消"),INIT(0, "初始状态（未支付）");
     *
     * @param userId
     * @param bizStatus
     * @return
     */
    private Specification queryOrderSpecific(int userId, int bizStatus) {
        Specification querySpecific = new Specification<EssayGoodsOrder>() {
            @Override
            public Predicate toPredicate(Root<EssayGoodsOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum bizStatusEnum = EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.create(bizStatus);
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                if(null == bizStatusEnum){      //未知状态，按全部状态返回
                    predicates.add(criteriaBuilder.notEqual(root.get("bizStatus"), EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYEXCEPTION.getBizStatus()));
                }else{
                    switch (bizStatusEnum){
                        case INIT:
                        case PAYED:
                        case BACKED:
                        case BACKED_REJECT:
                            predicates.add(criteriaBuilder.equal(root.get("bizStatus"), bizStatusEnum.getBizStatus()));
                            break;
                        case PAYEXCEPTION:      //支付异常，按全部查询方式查询
                        case PRE_BACK:
                            predicates.add(criteriaBuilder.notEqual(root.get("bizStatus"), EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYEXCEPTION.getBizStatus()));
                            break;
                        case TIMEOUT:
                        case CANCEL:
                            List<Integer> cancelList = Lists.newArrayList(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.CANCEL.getBizStatus(),
                                    EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.TIMEOUT.getBizStatus());
                            predicates.add((root.get("bizStatus").in(cancelList)));
                    }
                }
                predicates.add(criteriaBuilder.equal(root.get("status"), EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus()));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }


    /**
     * 订单删除
     *
     * @param id
     * @return
     */
    @Override
    public Map delOrder(long id) {
        int upToDel = essayGoodsOrderRepository.upToDel(id);

        Map<String, Object> map = new HashMap<>();
        map.put("flag", 1 == upToDel);
        return map;
    }

    @Override
    public void closeOrder() {
        Date date = new Date();

        Date dayBefore = getDayBefore(date, orderCloseTime);
        essayGoodsOrderRepository.updateOrderToClose(dayBefore);

    }

    @Override
    public Map cancelOrder(long id) {
        int upToDel = essayGoodsOrderRepository.upToCancel(id);

        Map<String, Object> map = new HashMap<>();
        map.put("flag", 1 == upToDel);
        return map;
    }

    /**
     * 获得指定日期的前n天
     *
     * @param date
     * @return
     * @throws Exception
     */
    public static Date getDayBefore(Date date, int n) {
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day - n);

        return c.getTime();
    }

    /**
     * 根据用户账户信息赠送批改
     */
    @Override
    public List<String> reward(UserCorrectGoodsRewardVO vo) {
        int queNum = vo.getQueNum();
        int mulNum = vo.getMulNum();
        int argNum = vo.getArgNum();
        List<String> accountList = vo.getAccountList();

        List<String> errorList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(accountList)) {
            accountList.forEach(account -> {
                try {
                    int userId = getUserIdByAccount(account);
                    log.info("userId:{}", userId);
                    updateUserCorrectTimes(queNum, mulNum, argNum, userId);
                } catch (Exception e) {
                    e.printStackTrace();
                    errorList.add(account);
                }
            });

        }

        String source = "";
        if (StringUtils.isNotEmpty(vo.getUrl())) {
            source = vo.getUrl();
        } else {
            source = vo.getAccountList().toString();
        }
        EssayRewardRecord record = EssayRewardRecord.builder()
                .argNum(vo.getArgNum())
                .mulNum(vo.getMulNum())
                .queNum(vo.getQueNum())
                .errorList(CollectionUtils.isNotEmpty(errorList) ? errorList.toString() : "")
                .source(source)
                .remark(vo.getRemark())
                .build();
        record.setCreator(vo.getCreator());
        record.setGmtCreate(new Date());
        essayRewardRecordRepository.save(record);

        return errorList;
    }

    @Override
    public List<String> preHandleFile(String path) {
        List<String> list = new LinkedList<>();
        try {
            //创建Excel工作薄
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3*1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            InputStream inputStream = conn.getInputStream();
            Workbook work = WorkbookFactory.create(inputStream);

            if (null == work) {
                throw new Exception("创建Excel工作薄为空！");
            }
            Sheet sheet = null;
            Row row = null;
            Cell cell = null;

            for (int i = 0; i < work.getNumberOfSheets(); i++) {
                sheet = work.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }

                for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                    row = sheet.getRow(j);
                    if (row == null || row.getFirstCellNum() == j) {
                        continue;
                    }

                    cell = row.getCell(0);
                    String cellValue = getCellValue(cell);
                    if(StringUtils.isNotEmpty(cellValue)){
                        list.add(cellValue);
                    }
                }
            }
            work.close();
            return list;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Object getRewardList(int page, int pageSize) {
        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtCreate");
        Page<EssayRewardRecord> all = essayRewardRecordRepository.findAll(pageable);
        return all;
    }

//    @Override
//    public Object mockReward() {
//
//        List<EssayPaperAnswer> answerList = essayPaperAnswerRepository.findByPaperBaseIdAndType
//                (563L, AdminPaperConstant.MOCK_PAPER);
//
//        for(EssayPaperAnswer answer:answerList){
//            updateUserCorrectTimes(10, 10, 10, answer.getUserId());
//
//        }
//
//        return null;
//    }

    private int getUserIdByAccount(String account) {

        //拼接头部信息
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(null, headers);

        String url = getUserAccountUrl + account;
        //发送请求
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        Map body = resp.getBody();
        Map data = (Map) body.get("data");
        Integer userId = (Integer) data.get("id");
        return userId;
    }


//    private int getUserIdByPhone(String phone) {
//
//        //拼接头部信息
//        HttpHeaders headers = new HttpHeaders();
//        MediaType contentType = MediaType.parseMediaType("application/json");
//        headers.setContentType(contentType);
//        headers.add("Content-Type", "application/json");
//        headers.add("secret", MD5(phone));
//        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(null, headers);
//
//        String url = "http://192.168.100.22:11453/u/v2/users/getUserInfoByMobileForPHP/" + phone;
//        //发送请求
//        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
//        Map body = resp.getBody();
//        Map data = (Map) body.get("data");
//        Integer userId = (Integer) data.get("id");
//        return userId;
//    }
//
//
//    public static String MD5(String key) {
//        char hexDigits[] = {
//                'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'H', 'U'
//        };
//        try {
//            byte[] btInput = key.getBytes();
//            // 获得MD5摘要算法的 MessageDigest 对象
//            MessageDigest mdInst = MessageDigest.getInstance("MD5");
//            // 使用指定的字节更新摘要
//            mdInst.update(btInput);
//            // 获得密文
//            byte[] md = mdInst.digest();
//            // 把密文转换成十六进制的字符串形式
//            int j = md.length;
//            char str[] = new char[j * 2];
//            int k = 0;
//            for (int i = 0; i < j; i++) {
//                byte byte0 = md[i];
//                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
//                str[k++] = hexDigits[byte0 & 0xf];
//            }
//            return new String(str);
//        } catch (Exception e) {
//            return null;
//        }
//    }


}
