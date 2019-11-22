package com.huatu.tiku.essay.service.impl.correct;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant.GoodsTypeEnum;
import com.huatu.tiku.essay.dto.ApiPHPCourseGoodsOrderDto;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.*;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.service.ZtkUserService;
import com.huatu.tiku.essay.service.correct.UserCorrectGoodsServiceV4;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.vo.resp.OrderCreateVO;
import com.huatu.tiku.essay.vo.resp.OrderResponseVO;
import com.huatu.tiku.essay.vo.resp.correct.CorrectTimesSimpleVO;
import com.huatu.tiku.essay.vo.resp.correct.ResponseExtendVO;
import com.huatu.tiku.essay.vo.resp.correct.UserCorrectTimesVO;
import com.huatu.tiku.essay.vo.resp.goods.GoodsOrderDetailVO;
import com.huatu.tiku.essay.vo.user.ZtkUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: UserCorrectGoodsServiceImplV4
 * @description: 用户批改次数维护
 * @date 2019-07-0823:33
 */
@Service
@Slf4j
public class UserCorrectGoodsServiceImplV4 implements UserCorrectGoodsServiceV4 {
    /**
     * 课程赠送默认订单编号（针对7.1.164版本之前的订单）
     */
    private static final String DEFAULT_COURSE_ORDER_NUM_STR = "ht20190807000000000";
    @Autowired
    private EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository;

    @Autowired
    private EssayGoodsOrderDetailRepository goodsOrderDetailRepository;

    @Autowired
    private EssayGoodsOrderRepository essayGoodsOrderRepository;

    @Autowired
    private UserCorrectGoodsService userCorrectGoodsService;

    @Autowired
    private EssayCorrectGoodsRepository essayCorrectGoodsRepository;

    @Autowired
    private EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    private EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    private EssayPaperService essayPaperService;

    @Autowired
    private EssayQuestionService essayQuestionService;

    @Autowired
    private ZtkUserService ztkUserService;

    @Autowired
    EntityManager entityManager;
    /**
     * 查询用户所有订单详情
     */
    BiFunction<Integer, Integer, List<EssayGoodsOrderDetail>> getAllDetails = ((userId, goodsType) -> {
        List<EssayGoodsOrderDetail> byUserIdAndGoodsType = goodsOrderDetailRepository.findByUserIdAndGoodsType(userId, goodsType);
        return byUserIdAndGoodsType;
    });

    @Override
    public UserCorrectTimesVO findByUserIdAndBizStatusAndStatus(int userId) {

        //用户批改次数统计查询
        List<EssayUserCorrectGoods> userCorrectGoodsList = getEssayUserCorrectGoodsInfo(userId);
        log.info("userId:{},getEssayUserCorrectGoodsInfo:{}", userId, new Gson().toJson(userCorrectGoodsList));
        //TODO 查询用户所有有时限的批改订单详情（考虑缓存机制）
        List<EssayGoodsOrderDetail> orderDetailsWithExpire = goodsOrderDetailRepository.findByUserIdAndIsLimitNumAndStatusAndNumGreaterThan(
                userId,
                1,
                EssayStatusEnum.NORMAL.getCode(),
                0
        );
        return UserCorrectTimesUtil.assemblingUserCorrectTimes.apply(userCorrectGoodsList, orderDetailsWithExpire);
    }

    /**
     * 用户批改次数统计查询
     * TODO 统计数据查询需要补充逻辑和缓存机制
     *
     * @param userId
     * @return
     */
    private List<EssayUserCorrectGoods> getEssayUserCorrectGoodsInfo(int userId) {
        /**
         * 当前存在的用户统计数据
         */
        List<EssayUserCorrectGoods> userCorrectGoodsList = essayUserCorrectGoodsRepository.findByUserIdAndBizStatusAndStatus(
                userId,
                UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus(),
                UserCorrectGoodsConstant.UserCorrectGoodsStatusEnum.NORMAL.getStatus());
        log.info("findByUserIdAndBizStatusAndStatus:{}", new Gson().toJson(userCorrectGoodsList));
        long currentTimeMillis = System.currentTimeMillis();
        List<EssayUserCorrectGoods> tempRemoveList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(userCorrectGoodsList)) {
            for (EssayUserCorrectGoods userCorrectGoods : userCorrectGoodsList) {
                if (null != userCorrectGoods.getExpireTime() &&
                        userCorrectGoods.getExpireTime().getTime() < currentTimeMillis) {
                    tempRemoveList.add(userCorrectGoods);
                }
            }
        }
        if (CollectionUtils.isEmpty(tempRemoveList)) {
            return userCorrectGoodsList;
        }
        for (EssayUserCorrectGoods userCorrectGoods : tempRemoveList) {
            userCorrectGoodsList.remove(userCorrectGoods);
            EssayUserCorrectGoods newUserTimes = resetUserCorrectTime(userId, GoodsTypeEnum.create(userCorrectGoods.getType()));
            userCorrectGoodsList.add(newUserTimes);
        }

        return userCorrectGoodsList;
    }


    @Override
    public List<GoodsOrderDetailVO> detail(Pageable pageRequest, int userId, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum) {
        List<EssayGoodsOrderDetail> details = goodsOrderDetailRepository.findByUserIdAndGoodsTypeAndStatus(
                pageRequest,
                userId,
                goodsTypeEnum.getType(),
                EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(details)) {
            return Lists.newArrayList();
        }
        List<EssayGoodsOrder> essayGoodsOrderLists = essayGoodsOrderRepository.findByIdIn(details.stream().map(EssayGoodsOrderDetail::getRecordId).collect(Collectors.toList()));
        List<EssayCorrectGoods> all = essayCorrectGoodsRepository.findAll();
        for (EssayGoodsOrderDetail detail : details) {
            Optional<EssayCorrectGoods> first = all.stream().filter(i -> i.getId() == detail.getGoodsId()).findFirst();
            if (first.isPresent()) {
                EssayCorrectGoods essayCorrectGoods = first.get();
                detail.setGoodsName(essayCorrectGoods.getName());
            }
        }
        return assemblingOrderDetailVO.apply(details, essayGoodsOrderLists);
    }

    @Override
    public long countDetail(int userId, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum) {
        return goodsOrderDetailRepository.countByUserIdAndGoodsTypeAndStatus(
                userId,
                goodsTypeEnum.getType(),
                EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus());
    }

    @Override
    public CorrectTimesSimpleVO findCorrectTimes(int userId, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum, int id) {
        //整体批改次数查询
        List<EssayUserCorrectGoods> essayUserCorrectGoodsInfo = getEssayUserCorrectGoodsInfo(userId);
        //初始化返回结果
        CorrectTimesSimpleVO correctTimesSimpleVO = UserCorrectTimesUtil.initCorrectTimesVO(goodsTypeEnum);
        Optional<EssayUserCorrectGoods> any = essayUserCorrectGoodsInfo.stream().filter(i -> i.getType() == goodsTypeEnum.getType())
                .findAny();
        if (any.isPresent()) {      //该类型是否有批改次数记录
            EssayUserCorrectGoods userCorrectGoods = any.get();
            correctTimesSimpleVO.setNum(userCorrectGoods.getUsefulNum() - userCorrectGoods.getSpecialNum());
            if (userCorrectGoods.getSpecialNum() > 0) {     //如果专用批改次数大于0，则直接查询确认是否有改id的专用次数
                List<EssayGoodsOrderDetail> orderDetails =
                        goodsOrderDetailRepository.findByUserIdAndGoodsTypeAndStatusAndSpecialId(userId,
                                goodsTypeEnum.getType(),
                                EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus(),
                                id);
                if (CollectionUtils.isNotEmpty(orderDetails)) {
                    long currentTimeMillis = System.currentTimeMillis();
                    int sum = orderDetails.stream().filter(i -> i.getExpireDate().getTime() > currentTimeMillis)
                            .filter(i -> i.getCount() > 0)
                            .mapToInt(EssayGoodsOrderDetail::getCount).sum();
                    correctTimesSimpleVO.setSpecialNum(sum);//只查询可用的专用次数，非改题目的专用次数不展示
                }
            }
        }
        return correctTimesSimpleVO;
    }

    /**
     * 创建订单
     *
     * @param userId
     * @param orderCreateVO
     * @param terminal
     * @param userName
     * @return
     */
    @Override
    public OrderResponseVO createOrder(int userId, OrderCreateVO orderCreateVO, int terminal, String userName) {
        return userCorrectGoodsService.createOrder(userId, orderCreateVO, terminal, userName);
    }


    private BiFunction<List<EssayGoodsOrderDetail>, List<EssayGoodsOrder>, List<GoodsOrderDetailVO>> assemblingOrderDetailVO = (
            (details, orders) -> {
                List<GoodsOrderDetailVO> result = Lists.newArrayList();
                for (EssayGoodsOrderDetail detail : details) {
                    GoodsOrderDetailVO goodsOrderDetailVO = new GoodsOrderDetailVO();
                    BeanUtils.copyProperties(detail, goodsOrderDetailVO);
                    goodsOrderDetailVO.setName(detail.getGoodsName());
                    goodsOrderDetailVO.setBizStatusName(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.getName(detail.getBizStatus()));
                    UserOrderUtil.assemblingOrderExpireTime(detail, goodsOrderDetailVO);
                    if (CollectionUtils.isEmpty(orders)) {
                        result.add(goodsOrderDetailVO);
                        continue;
                    }
                    Optional<EssayGoodsOrder> any = orders.stream().filter(i -> i.getId() == detail.getRecordId()).findAny();
                    if (any.isPresent()) {
                        EssayGoodsOrder essayGoodsOrder = any.get();
                        goodsOrderDetailVO.setOrderNumStr(essayGoodsOrder.getOrderNumStr());
                        goodsOrderDetailVO.setPayTime(null == essayGoodsOrder.getPayTime() ? detail.getGmtModify() : essayGoodsOrder.getPayTime());
                        EssayGoodsOrderSourceEnum source = essayGoodsOrder.getSource();
                        if (null == source) {
                            source = EssayGoodsOrderSourceEnum.APP;
                        }
                        if (source != EssayGoodsOrderSourceEnum.APP) {
                            goodsOrderDetailVO.setMemo(source.getValue());
                        } else {
                            goodsOrderDetailVO.setMemo(StringUtils.isBlank(essayGoodsOrder.getComment()) ? "" : essayGoodsOrder.getComment());
                        }
                        goodsOrderDetailVO.setSource(source.getValue());
                    }
                    result.add(goodsOrderDetailVO);
                }
                return result;
            }
    );


    /**
     * 批改次数校验
     *
     * @param userId
     * @param type
     * @param id
     * @return
     */
    @Override
    public ResponseExtendVO check(int userId, int type, int id) {
        ResponseExtendVO responseExtendVO = ResponseExtendVO.builder().build();
        if (id < 0) {
            return responseExtendVO;
        }
        EssayCorrectGoodsConstant.GoodsTypeEnum intelligenceGoodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(type, CorrectModeEnum.INTELLIGENCE.getMode());

        EssayCorrectGoodsConstant.GoodsTypeEnum manualGoodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(type, CorrectModeEnum.MANUAL.getMode());

        CorrectTimesSimpleVO manual = findCorrectTimes(userId, manualGoodsTypeEnum, id);
        CorrectTimesSimpleVO intelligence = findCorrectTimes(userId, intelligenceGoodsTypeEnum, id);

        responseExtendVO.setManual(manual);
        responseExtendVO.setIntelligence(intelligence);
        dealResponseExtendInfo(responseExtendVO, userId, type, id,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);     //只有普通批改才会消耗批改次数
        return responseExtendVO;
    }

    @Override
    public List<EssayGoodsOrderDetail> updateCorrectOrderDetailTimes(int userId, GoodsTypeEnum goodsTypeEnum, long specialId, Map<Long, Long> reduceMap) {
        int goodsType = goodsTypeEnum.getType();
        List<EssayGoodsOrderDetail> orderDetails = goodsOrderDetailRepository
                .findByUserIdAndGoodsType(userId, goodsType);
        if (CollectionUtils.isEmpty(orderDetails)) {
            throw new BizException(EssayErrors.LOW_CORRECT_TIMES);
        }

        //优先使用有期限的订单，再使用无期限的订单
        Date nowDate = new Date();

        List<EssayGoodsOrderDetail> detailsWithExpireFlag = orderDetails.stream()
                .filter(i -> i.getStatus() == EssayStatusEnum.NORMAL.getCode())
                .filter(i -> EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.available(i.getBizStatus()))
                .filter(i -> i.getExpireFlag() == 1)
                .filter(i -> i.getExpireDate().after(nowDate)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(detailsWithExpireFlag)) {
            Optional<EssayGoodsOrderDetail> any = detailsWithExpireFlag.stream().filter(i -> i.getIsLimitNum() == 0).findAny();
            if (any.isPresent()) {        //如果存在未过期的订单则直接不扣除次数，不做修改
                return orderDetails;
            }
            List<EssayGoodsOrderDetail> tempOrderDetails = detailsWithExpireFlag.stream().filter(i -> i.getNum() > 0).sorted(Comparator.comparing(EssayGoodsOrderDetail::getExpireDate)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tempOrderDetails)) {       //存在有有限期的且有批改次数的订单，则优先扣除，直接返回
                EssayGoodsOrderDetail essayGoodsOrderDetail = tempOrderDetails.get(0);
                essayGoodsOrderDetail.setNum(essayGoodsOrderDetail.getNum() - 1);
                goodsOrderDetailRepository.save(essayGoodsOrderDetail);
                reduceMap.put(specialId, essayGoodsOrderDetail.getId());
                return orderDetails;
            }
        }
        //无期限的订单处理逻辑（默认没有不限次数的订单）
        List<EssayGoodsOrderDetail> detailsWithoutExpireFlag = orderDetails.stream().filter(i -> i.getStatus() == EssayStatusEnum.NORMAL.getCode())
                .filter(i -> i.getExpireFlag() == 0)
                .filter(i -> i.getNum() > 0)
                .sorted(Comparator.comparing(BaseEntity::getGmtCreate)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(detailsWithoutExpireFlag)) {
            EssayGoodsOrderDetail essayGoodsOrderDetail = detailsWithoutExpireFlag.get(0);
            essayGoodsOrderDetail.setNum(essayGoodsOrderDetail.getNum() - 1);
            goodsOrderDetailRepository.save(essayGoodsOrderDetail);
            reduceMap.put(specialId, essayGoodsOrderDetail.getId());
            return orderDetails;
        }
        throw new BizException(EssayErrors.LOW_CORRECT_TIMES);
    }

    /**
     * 处理返回扩展信息
     *  @param responseExtendVO
     * @param userId
     * @param type
     * @param id
     * @param modeTypeEnum
     */
    private void dealResponseExtendInfo(ResponseExtendVO responseExtendVO, int userId, int type, int id, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        responseExtendVO.setAnswerCardId(0L);
        responseExtendVO.setOtherAnswerCardId(0L);
        responseExtendVO.setCorrectMode(CorrectModeEnum.INTELLIGENCE.getMode());
        responseExtendVO.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        responseExtendVO.setLastType(CorrectModeEnum.INTELLIGENCE.getMode());
        responseExtendVO.setManualRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());

        // 如果 type 为套题
        if (type == 1) {
            List<EssayPaperAnswer> essayPaperAnswers = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndStatusAndAnswerCardType(
                    userId,
                    Long.valueOf(id),
                    EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                    modeTypeEnum.getType());
            essayPaperService.dealPaperResponseExtendInfo(essayPaperAnswers, responseExtendVO);
            // 如果 type 为单题（议论文）
        } else {
            List<EssayQuestionAnswer> essayQuestionAnswers = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndStatusAndQuestionBaseIdAndAnswerCardType(userId,
                    0L,
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    Long.valueOf(id),
                    modeTypeEnum.getType());
            essayQuestionService.dealQuestionResponseExtendInfo(essayQuestionAnswers, responseExtendVO);
        }
    }

    @Transactional
    @Override
    public void buyCourse(Integer userId, ApiPHPCourseGoodsOrderDto goodsOrderDto) {
        int totalMoney = 0;
        int realMoney = 0;

        List<EssayGoodsOrderDetail> goodsOrderDetails = Lists.newArrayListWithExpectedSize(goodsOrderDto.getCorrectGoodsList().size());

        for (ApiPHPCourseGoodsOrderDto.CorrectGoods correctGoods : goodsOrderDto.getCorrectGoodsList()) {
            EssayCorrectGoods essayCorrectGoods = essayCorrectGoodsRepository.findOne(correctGoods.getId());

            Assert.isTrue(EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.SELLING.getBizStatus() == essayCorrectGoods.getBizStatus(), "商品【" + essayCorrectGoods.getId() + "】状态错误");
            Assert.isTrue(essayCorrectGoods.getInventory() > 0, "商品【" + essayCorrectGoods.getId() + "】库存不足");
            Assert.isTrue(EssayCorrectGoodsSaleTypeEnum.COURSE_GIFT.equals(essayCorrectGoods.getSaleType()), "商品【" + essayCorrectGoods.getId() + "】类型错误");

            totalMoney += essayCorrectGoods.getActivityPrice() * correctGoods.getCount();
            realMoney += essayCorrectGoods.getPrice() * correctGoods.getCount();

            if (essayCorrectGoods.getInventory() < correctGoods.getCount()) {
                throw new BizException(EssayErrors.LOW_INVENTORY);
            }

            int expireDate = essayCorrectGoods.getExpireDate();
            long todayEndMillions = DateUtil.getTodayEndMillions();

            EssayGoodsOrderDetail goodsOrderDetail = EssayGoodsOrderDetail.builder()
                    .goodsId(essayCorrectGoods.getId())
                    .goodsName(essayCorrectGoods.getName())
                    .count(correctGoods.getCount())
                    .userId(userId)
                    .price(essayCorrectGoods.getActivityPrice())
                    .unit(essayCorrectGoods.getNum())
                    .correctMode(essayCorrectGoods.getCorrectMode())
                    .goodsType(essayCorrectGoods.getType())
                    .isLimitNum(essayCorrectGoods.getIsLimitNum())
                    .expireFlag(essayCorrectGoods.getExpireFlag())
                    .expireDate(new Date(todayEndMillions + TimeUnit.DAYS.toMillis(expireDate)))
                    .expireTime(expireDate)
                    .num(correctGoods.getCount() * essayCorrectGoods.getNum())
                    .goodsType(essayCorrectGoods.getType())
                    .build();

            goodsOrderDetail.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus());
            goodsOrderDetail.setStatus(EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus());

            goodsOrderDetails.add(goodsOrderDetail);
        }

        Date now = new Date();

        EssayGoodsOrder goodsOrder = new EssayGoodsOrder();
        goodsOrder.setUserId(userId);
        goodsOrder.setTotalMoney(totalMoney);
        goodsOrder.setRealMoney(realMoney);
        goodsOrder.setPayTime(now);
        goodsOrder.setGmtCreate(now);
        goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus());
        goodsOrder.setSource(EssayGoodsOrderSourceEnum.ONLINE_COURSE);
        goodsOrder.setCourseOrderId(goodsOrderDto.getOrderId());
        goodsOrder.setPayType(PayConstant.FREE_GIFT_OF_COURSE);
        goodsOrder.setOrderNumStr(goodsOrderDto.getOrderNum());
        goodsOrder.setStatus(EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus());

        // 填充手机号
        ZtkUserVO ztkUserVO = ztkUserService.getById(userId);

        goodsOrder.setName(ztkUserVO.getName());
        goodsOrder.setMobile(ztkUserVO.getMobile());

        essayGoodsOrderRepository.save(goodsOrder);

        // 保存订单明细
        goodsOrderDetails.forEach(goodsOrderDetail -> goodsOrderDetail.setRecordId(goodsOrder.getId()));

        goodsOrderDetailRepository.save(goodsOrderDetails);

        // 更新次数
        UserOrderUtil.updateUserCorrectTime(goodsOrder,
                goodsOrderDetails,
                essayUserCorrectGoodsRepository,
                getAllDetails
        );
    }

    @Override
    public EssayUserCorrectGoods resetUserCorrectTime(int userId, GoodsTypeEnum goodsTypeEnum) {
        EssayUserCorrectGoods userCorrectGoods = UserOrderUtil.createUserCorrectGoods(userId, goodsTypeEnum.getType(), getAllDetails);
        List<EssayUserCorrectGoods> userCorrectTimes = essayUserCorrectGoodsRepository.findByUserIdAndBizStatusAndStatusAndType(userId,
                UserCorrectGoodsConstant.UserCorrectGoodsBizStatusEnum.NORMAL.getBizStatus(),
                EssayStatusEnum.NORMAL.getCode(),
                goodsTypeEnum.getType());
        if (CollectionUtils.isNotEmpty(userCorrectTimes) && null != userCorrectGoods) {
            userCorrectGoods.setId(userCorrectTimes.get(0).getId());
        }
        essayUserCorrectGoodsRepository.save(userCorrectGoods);
        return userCorrectGoods;
    }

    @Override
    public List<GoodsOrderDetailVO> detail(int page, int pageSize, int userId, GoodsTypeEnum goodsTypeEnum) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" v_essay_goods_order_detail.id,v_essay_goods_order_detail.biz_status,v_essay_goods_order_detail.count,v_essay_goods_order_detail.unit,v_essay_goods_order_detail.goods_name,v_essay_goods_order_detail.expire_flag,v_essay_goods_order_detail.expire_date,v_essay_goods_order_detail.expire_time, ");
        sql.append(" v_essay_goods_order_detail.is_limit_num,v_essay_goods_order_detail.num,v_essay_goods_order.order_num_str,v_essay_goods_order.pay_time,v_essay_goods_order.source,v_essay_goods_order.`comment`, ");
        sql.append(" IF (v_essay_goods_order_detail.num<=0 OR (v_essay_goods_order_detail.expire_flag=1 AND v_essay_goods_order_detail.expire_date< NOW()) OR v_essay_goods_order_detail.biz_status = 6,0,1) status_sort ");
        sql.append(" FROM v_essay_goods_order_detail LEFT JOIN v_essay_goods_order ON v_essay_goods_order.id=v_essay_goods_order_detail.record_id WHERE");
        sql.append(" v_essay_goods_order_detail.`status` = 1  ");
        sql.append(" and v_essay_goods_order_detail.user_id = ").append(userId);
        sql.append(" and v_essay_goods_order_detail.goods_type = ").append(goodsTypeEnum.getType());
        sql.append(" ORDER BY status_sort DESC,v_essay_goods_order_detail.expire_flag DESC,v_essay_goods_order_detail.expire_date ASC,v_essay_goods_order_detail.gmt_create DESC ");
        sql.append(" limit ").append((page - 1) * pageSize).append(",").append(pageSize);
        log.info("sql={}", sql);
        Query dataQuery = entityManager.createNativeQuery(sql.toString());
        try {
            List<Object[]> details = dataQuery.getResultList();
            if (CollectionUtils.isEmpty(details)) {
                return Lists.newArrayList();
            }
            List<GoodsOrderDetailVO> result = Lists.newArrayList();
            for (Object[] detail : details) {
                GoodsOrderDetailVO.GoodsOrderDetailVOBuilder builder = GoodsOrderDetailVO.builder();
                Timestamp payTime = (Timestamp) detail[11];
                GoodsOrderDetailVO.GoodsOrderDetailVOBuilder goodsOrderDetailVOBuilder = builder.id((Long) detail[0])
                        .bizStatus((Short) detail[1])
                        .count((Integer) detail[2])
                        .unit((Integer) detail[3])
                        .name((String) detail[4])
                        .expireFlag((Integer) detail[5])
                        .expireTime(((Integer) detail[7]))
                        .isLimitNum((Integer) detail[8])
                        .num((Integer) detail[9])
                        .orderNumStr((String) detail[10])
                        .payTime(null == payTime ? null : new Date(payTime.getTime()));
                EssayGoodsOrderSourceEnum value = EssayGoodsOrderSourceEnum.ONLINE_COURSE;
                Integer source = (Integer) detail[12];
                // 只有课程赠送的订单app端才显示来源source
                if (null != source && value.ordinal() == source) {
                    goodsOrderDetailVOBuilder.source(value.getValue()).memo(value.getValue());
                }
                Timestamp expireDate = (Timestamp) detail[6];
                GoodsOrderDetailVO build = goodsOrderDetailVOBuilder.build();
                /**
                 * 如果无订单编号，默认为课程赠送，且生成固定订单编号
                 */
                if (StringUtils.isBlank(build.getOrderNumStr())) {
                    build.setSource(value.getValue());
                    build.setMemo(value.getValue());
                    build.setOrderNumStr(DEFAULT_COURSE_ORDER_NUM_STR);
                }
                UserOrderUtil.assemblingUserCorrectGoods(expireDate != null ? new Date(expireDate.getTime()) : null,
                        build.getExpireFlag(), build.getExpireTime(), build);
                result.add(build);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }
}
