package com.huatu.tiku.essay.service.impl.correct;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.constant.status.UserCorrectGoodsConstant;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.entity.EssayGoodsOrder;
import com.huatu.tiku.essay.entity.EssayGoodsOrderDetail;
import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderSourceEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayGoodsOrderDetailRepository;
import com.huatu.tiku.essay.repository.EssayUserCorrectGoodsRepository;
import com.huatu.tiku.essay.service.correct.UserCorrectGoodsServiceV4;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.vo.resp.OrderCreateVO;
import com.huatu.tiku.essay.vo.resp.OrderGoodsVO;
import com.huatu.tiku.essay.vo.resp.goods.GoodsOrderDetailVO;
import com.huatu.ztk.commons.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.method.P;

/**
 * @author huangqingpeng
 * @title: UserOrderUtil
 * @description: 用户订单相关业务逻辑处理工具
 * @date 2019-07-1017:13
 */
@Slf4j
public class UserOrderUtil {

    /**
     * 创建新的订单
     *
     * @param userId
     * @param orderCreateVO
     * @param terminal
     */
    public static EssayGoodsOrder createOrder(int userId, OrderCreateVO orderCreateVO, int terminal,
                                              Function<List<Long>, List<EssayCorrectGoods>> findGoodsInfo,
                                              Consumer<EssayGoodsOrder> saveOrder,
                                              Consumer<List<EssayGoodsOrderDetail>> saveOrderDetail) {

        List<OrderGoodsVO> goods = orderCreateVO.getGoods();
        if (CollectionUtils.isEmpty(goods)) {
            throw new BizException(EssayErrors.ORDER_ERROR);
        }
        List<Long> goodsIds = goods.stream().map(OrderGoodsVO::getGoodsId).collect(Collectors.toList());
        List<EssayCorrectGoods> goodsList = findGoodsInfo.apply(goodsIds);
        if (CollectionUtils.isEmpty(goodsList)) {
            throw new BizException(EssayErrors.ORDER_ERROR);
        }
        Map<Long, EssayCorrectGoods> goodsMap = goodsList.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        int realPrice = 0;
        int realMoney = 0;
        List<EssayGoodsOrderDetail> details = Lists.newArrayList();
        for (OrderGoodsVO good : goods) {
            Long goodsId = good.getGoodsId();
            EssayCorrectGoods essayCorrectGoods = goodsMap.get(goodsId);
            if (null == essayCorrectGoods) {
                continue;
            }
            realPrice += essayCorrectGoods.getActivityPrice() * good.getCount();
            realMoney += essayCorrectGoods.getPrice() * good.getCount();
            if (essayCorrectGoods.getInventory() < good.getCount()) {
                throw new BizException(EssayErrors.LOW_INVENTORY);
            }
            int expireDate = essayCorrectGoods.getExpireDate();
            long todayEndMillions = DateUtil.getTodayEndMillions();
            EssayGoodsOrderDetail build = EssayGoodsOrderDetail.builder()
                    .goodsId(essayCorrectGoods.getId())
                    .goodsName(essayCorrectGoods.getName())
                    .count(good.getCount())
                    .userId(userId)
                    .price(essayCorrectGoods.getActivityPrice())
                    .unit(essayCorrectGoods.getNum())
                    .correctMode(essayCorrectGoods.getCorrectMode())
                    .goodsType(essayCorrectGoods.getType())
                    .isLimitNum(essayCorrectGoods.getIsLimitNum())
                    .expireFlag(essayCorrectGoods.getExpireFlag())
                    .expireTime(expireDate)
                    .num(good.getCount() * essayCorrectGoods.getNum())
                    .goodsType(essayCorrectGoods.getType())
                    .build();
            if(essayCorrectGoods.getExpireFlag() == 1){
                build.setExpireDate(new Date(todayEndMillions + TimeUnit.DAYS.toMillis(expireDate)));
            }
            build.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.INIT.getBizStatus());
            build.setStatus(EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus());
            details.add(build);
        }
        if (realPrice != orderCreateVO.getTotal()) {
            throw new BizException(EssayErrors.PAYMENT_ERROR);
        }

        //生成订单编号(年月日时分秒+四位随机数)
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
        Random r = new Random();
        StringBuilder orderNumStr = new StringBuilder();
        orderNumStr.append("ht").append(date.format(new Date())).append(r.nextInt(10000));
        EssayGoodsOrder order = EssayGoodsOrder.builder()
                .realMoney(realMoney)
                .totalMoney(orderCreateVO.getTotal())
                .incomeType(1)
                .payType(orderCreateVO.getPayType())
                .userId(userId)
                .terminal(terminal)
                .orderNumStr(orderNumStr.toString())
                .mobile(orderCreateVO.getMobile())
                .name(orderCreateVO.getName())
                .build();
        order.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.INIT.getBizStatus());
        order.setStatus(EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus());
        order.setSource(EssayGoodsOrderSourceEnum.APP);
        saveOrder.accept(order);
        if (!details.isEmpty()) {
            details.forEach(i -> i.setRecordId(order.getId()));
            saveOrderDetail.accept(details);
        }
        return order;
    }


    /**
     * 根据订单信息修改用户批改次数
     *
     * @param order
     * @param details
     */
    public static void updateUserCorrectTime(EssayGoodsOrder order,
                                             List<EssayGoodsOrderDetail> details,
                                             EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository,
                                             BiFunction<Integer, Integer, List<EssayGoodsOrderDetail>> getAllDetails) {
        for (EssayGoodsOrderDetail detail : details) {
            int goodsType = detail.getGoodsType();
            int userId = new Long(detail.getUserId()).intValue();
            EssayUserCorrectGoods oldUserCorrectGoods = essayUserCorrectGoodsRepository.findByUserIdAndStatusAndType(userId,
                    EssayStatusEnum.NORMAL.getCode(), goodsType);
            EssayUserCorrectGoods userCorrectGoods = createUserCorrectGoods(order.getUserId(), goodsType, getAllDetails);
            if (null != oldUserCorrectGoods) {
                userCorrectGoods.setId(oldUserCorrectGoods.getId());
            }
            essayUserCorrectGoodsRepository.save(userCorrectGoods);
        }
    }

    /**
     * 按照订单详情重新生成订单数据
     *
     * @param userId
     * @param goodsType
     * @param getAllDetails
     * @return
     */
    public static EssayUserCorrectGoods createUserCorrectGoods(int userId,
                                                               int goodsType,
                                                               BiFunction<Integer, Integer, List<EssayGoodsOrderDetail>> getAllDetails) {

        List<EssayGoodsOrderDetail> details = getAllDetails.apply(userId, goodsType);
        EssayUserCorrectGoods build = initEssayUserCorrectGoods(userId, goodsType);
        if (CollectionUtils.isEmpty(details)) {
            return build;
        }
        for (EssayGoodsOrderDetail detail : details) {
            assemblingUserCorrectGoods(build, detail);
        }
        build.setStatus(UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus());
        build.setBizStatus(UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus());
        return build;
    }

    private static EssayUserCorrectGoods initEssayUserCorrectGoods(int userId, int goodsType) {
        return EssayUserCorrectGoods.builder()
                .userId(userId)
                .type(goodsType)
                .totalNum(0)
                .usefulNum(0)
                .specialNum(0)
                .isLimitNum(1)
                .expireTime(null)
                .build();
    }

    /**
     * 用户详细订单增量数据组装
     *
     * @param build
     * @param detail
     */
    private static void assemblingUserCorrectGoods(EssayUserCorrectGoods build, EssayGoodsOrderDetail detail) {
        if (EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus() != detail.getStatus()||
                !EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.available(detail.getBizStatus())) {
            return;
        }
        Date nowDate = new Date();
        int expireFlag = detail.getExpireFlag();
        if (expireFlag == 0 || (expireFlag != 0 && nowDate.compareTo(detail.getExpireDate()) < 0)) {        //有效数据筛选
            build.setTotalNum(build.getTotalNum() + detail.getUnit() * detail.getCount());
            build.setUsefulNum(build.getUsefulNum() + detail.getNum());
            if (null != detail.getSpecialId() && detail.getSpecialId() > 0) {
                build.setSpecialNum(build.getSpecialNum() + detail.getNum());
            }
            if (detail.getIsLimitNum() == 0) {
                build.setIsLimitNum(0);
            }
            if (detail.getExpireFlag() == 1 &&
                    (null == build.getExpireTime() || detail.getExpireDate().compareTo(build.getExpireTime()) < 0)) {
                build.setExpireTime(detail.getExpireDate());
            }
        }
    }

    /**
     * 扣除批改次数实现
     *
     * @param goodsTypeEnum
     * @param userId
     * @param essayUserCorrectGoodsRepository
     * @param userCorrectGoodsServiceV4
     * @param specialId                       //特殊批改次数专用试题ID或套题ID
     */
    public static Long reduceUserCorrectTimes(EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum,
                                              int userId,
                                              EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository,
                                              UserCorrectGoodsServiceV4 userCorrectGoodsServiceV4,
                                              long specialId) {
        Map<Long, Long> reduceMap = Maps.newHashMap();
        //批改总次数不存在||总表数据过期||专用次数大于0，优先使用专用次数 则直接去detail表中查询扣除数据
        List<EssayGoodsOrderDetail> orderDetails = userCorrectGoodsServiceV4.updateCorrectOrderDetailTimes(userId, goodsTypeEnum, specialId, reduceMap);
        //生成新的用户次数表数据
        EssayUserCorrectGoods userCorrectGoods = createUserCorrectGoods(userId, goodsTypeEnum.getType(), (Integer a, Integer b) -> orderDetails);
        List<EssayUserCorrectGoods> userCorrectTimes = essayUserCorrectGoodsRepository.findByUserIdAndBizStatusAndStatusAndType(userId,
                UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus(),
                EssayStatusEnum.NORMAL.getCode(),
                goodsTypeEnum.getType());
        if (CollectionUtils.isNotEmpty(userCorrectTimes)) {
            userCorrectGoods.setId(userCorrectTimes.get(0).getId());
        }
        essayUserCorrectGoodsRepository.save(userCorrectGoods);
        return reduceMap.getOrDefault(specialId, 0L);
    }

    /**
     * 返还详情实现
     *
     * @param correctOrder
     * @param essayGoodsOrderDetailRepository
     * @param userCorrectGoodsRepository
     * @return
     */
    public static void returnCorrectTimes(CorrectOrder correctOrder, EssayGoodsOrderDetailRepository essayGoodsOrderDetailRepository,
                                          EssayUserCorrectGoodsRepository userCorrectGoodsRepository) {
        if (correctOrder.getGoodsOrderDetailId() <= 0) {
            return;
        }
        int userId = new Long(correctOrder.getUserId()).intValue();
        EssayGoodsOrderDetail goodsOrderDetail = essayGoodsOrderDetailRepository.findOne(correctOrder.getGoodsOrderDetailId());
        if (null == goodsOrderDetail) {
            return;
        }
        goodsOrderDetail.setNum(goodsOrderDetail.getNum() + 1);
        essayGoodsOrderDetailRepository.save(goodsOrderDetail);
        //生成新的用户次数表数据
        EssayUserCorrectGoods userCorrectGoods = createUserCorrectGoods(userId, goodsOrderDetail.getGoodsType(), essayGoodsOrderDetailRepository::findByUserIdAndGoodsType);
        List<EssayUserCorrectGoods> userCorrectTimes = userCorrectGoodsRepository.findByUserIdAndBizStatusAndStatusAndType(userId,
                UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus(),
                EssayStatusEnum.NORMAL.getCode(),
                goodsOrderDetail.getGoodsType());
        if (CollectionUtils.isNotEmpty(userCorrectTimes)) {
            userCorrectGoods.setId(userCorrectTimes.get(0).getId());
        }
        log.info("商品信息是：{}", JsonUtil.toJson(userCorrectGoods));
        userCorrectGoodsRepository.save(userCorrectGoods);
    }

    /**
     * 组装订单的过期时间
     *
     * @param detail
     * @param detailVO
     */
    public static void assemblingOrderExpireTime(EssayGoodsOrderDetail detail, GoodsOrderDetailVO detailVO) {
       assemblingUserCorrectGoods(detail.getExpireDate(),detail.getExpireFlag(),detail.getExpireTime(),detailVO);
    }

    public static void assemblingUserCorrectGoods(Date expireDate, int expireFlag, int expireTime ,GoodsOrderDetailVO detailVO) {
        detailVO.setExpireDateStr("");
        detailVO.setExpireFlag(expireFlag);
        if (expireFlag == 0) {
            return;
        }
        if (null == expireDate) {
            detailVO.setExpireFlag(0);
            return;
        }
        //订单存在有效期
        long time = expireDate.getTime();
        detailVO.setExpireTime(expireTime);
        long current = System.currentTimeMillis();
        long l = time - current;
        long days = l / TimeUnit.DAYS.toMillis(1);
        detailVO.setExpireDate(l == 0 ? 1 : (int) days);
        detailVO.setExpireDateStr(DateUtil.getFormatDateStyleString(expireDate.getTime()) + "到期");
    }
}
