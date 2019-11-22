package com.huatu.tiku.essay.service.v2.impl.goods;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.constant.status.PayConstant;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.EssayCorrectGoodsSaleTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderSourceEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.ZtkUserService;
import com.huatu.tiku.essay.service.impl.correct.UserOrderUtil;
import com.huatu.tiku.essay.service.v2.goods.UserCorrectGoodsServiceV2;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.vo.admin.correct.UserCorrectGoodsRewardV2VO;
import com.huatu.tiku.essay.vo.user.ZtkUserVO;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * Created by x6 on 2017/11/21.
 */
@Service
@Slf4j
public class UserCorrectGoodsServiceV2Impl implements UserCorrectGoodsServiceV2 {

    @Autowired
    private EssayGoodsOrderRepository goodsOrderRepository;

    @Autowired
    private EssayGoodsOrderDetailRepository goodsOrderDetailRepository;

    @Autowired
    private EssayCorrectGoodsRepository essayCorrectGoodsRepository;

    @Autowired
    private ZtkUserService ztkUserService;

    @Autowired
    private EssayRewardRecordRepository essayRewardRecordRepository;

    @Autowired
    private EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository;

    @Autowired
    private EssayRewardRecordDetailRepository essayRewardRecordDetailRepository;

    /**
     * 查询用户所有订单详情
     */
    BiFunction<Integer, Integer, List<EssayGoodsOrderDetail>> getAllDetails = ((userId, goodsType) -> {
        List<EssayGoodsOrderDetail> byUserIdAndGoodsType = goodsOrderDetailRepository.findByUserIdAndGoodsType(userId, goodsType);
        return byUserIdAndGoodsType;
    });

    /**
     * 根据用户账户信息赠送批改
     */
    @Transactional
    @Override
    public List<String> reward(UserCorrectGoodsRewardV2VO vo) {
        log.info("批量赠送金币:{}", JsonUtil.toJson(vo));
        List<String> errorUserList = Lists.newArrayList();

        List<String> notExistUserList = Lists.newArrayList();
        // 代报记录明细
        List<EssayRewardRecordDetail> rewardRecordDetails = Lists.newArrayListWithExpectedSize(vo.getCorrectGoodsList().size());

        vo.getAccountList().forEach(account -> {
            try {
                ZtkUserVO ztkUserVO = ztkUserService.getByUsernameOrderMobile(account);
                if (null != ztkUserVO) {
                    int totalMoney = 0;

                    List<EssayGoodsOrderDetail> goodsOrderDetails = Lists.newArrayListWithExpectedSize(vo.getCorrectGoodsList().size());

                    for (UserCorrectGoodsRewardV2VO.CorrectGoods correctGoods : vo.getCorrectGoodsList()) {
                        EssayCorrectGoods essayCorrectGoods = essayCorrectGoodsRepository.findOne(correctGoods.getId());

                        Assert.isTrue(EssayCorrectGoodsConstant.CorrectGoodsBizStatusEnum.SELLING.getBizStatus() == essayCorrectGoods.getBizStatus(), "商品【" + essayCorrectGoods.getId() + "】状态错误");
                        Assert.isTrue(essayCorrectGoods.getInventory() > 0, "商品【" + essayCorrectGoods.getId() + "】库存不足");
                        Assert.isTrue(EssayCorrectGoodsSaleTypeEnum.COURSE_GIFT.equals(essayCorrectGoods.getSaleType()), "商品【" + essayCorrectGoods.getId() + "】类型错误");

                        totalMoney += essayCorrectGoods.getActivityPrice() * correctGoods.getCount();

                        if (essayCorrectGoods.getInventory() < correctGoods.getCount()) {
                            throw new BizException(EssayErrors.LOW_INVENTORY);
                        }

                        int expireDate = essayCorrectGoods.getExpireDate();
                        long todayEndMillions = DateUtil.getTodayEndMillions();

                        EssayGoodsOrderDetail goodsOrderDetail = EssayGoodsOrderDetail.builder()
                                .goodsId(essayCorrectGoods.getId())
                                .goodsName(essayCorrectGoods.getName())
                                .count(correctGoods.getCount())
                                .userId(ztkUserVO.getId())
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
                        goodsOrderDetail.setCreator(vo.getCreator());
                        goodsOrderDetail.setStatus(EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus());
                        goodsOrderDetails.add(goodsOrderDetail);

                        // 代报记录
                        EssayRewardRecordDetail rewardRecordDetail = new EssayRewardRecordDetail();
                        rewardRecordDetail.setType(essayCorrectGoods.getType());
                        rewardRecordDetail.setGoodsId(essayCorrectGoods.getId());
                        rewardRecordDetail.setCount(correctGoods.getCount());
                        rewardRecordDetail.setCreator(vo.getCreator());

                        rewardRecordDetails.add(rewardRecordDetail);
                    }

                    Date now = new Date();

                    EssayGoodsOrder goodsOrder = new EssayGoodsOrder();
                    goodsOrder.setUserId(ztkUserVO.getId().intValue());
                    goodsOrder.setTotalMoney(totalMoney);
                    goodsOrder.setRealMoney((int) (vo.getRealMoney() * 100));
                    goodsOrder.setPayTime(now);
                    goodsOrder.setGmtCreate(now);
                    goodsOrder.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus());
                    goodsOrder.setSource(EssayGoodsOrderSourceEnum.OFFLINE_COURSE);
                    goodsOrder.setPayType(PayConstant.FREE_GIFT_OF_COURSE);
                    goodsOrder.setMobile(ztkUserVO.getMobile());
                    goodsOrder.setComment(vo.getRemark());
                    goodsOrder.setName(ztkUserVO.getName());

                    //生成订单编号(年月日时分秒+四位随机数)
                    SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
                    Random r = new Random();
                    StringBuilder orderNumStr = new StringBuilder();
                    orderNumStr.append("ht").append(date.format(new Date())).append(r.nextInt(10000));

                    goodsOrder.setOrderNumStr(orderNumStr.toString());
                    goodsOrder.setCreator(vo.getCreator());
                    goodsOrder.setStatus(EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.NORMAL.getStatus());

                    goodsOrderRepository.save(goodsOrder);

                    // 保存订单明细
                    goodsOrderDetails.forEach(goodsOrderDetail -> goodsOrderDetail.setRecordId(goodsOrder.getId()));

                    goodsOrderDetailRepository.save(goodsOrderDetails);

                    // 更新次数
                    UserOrderUtil.updateUserCorrectTime(goodsOrder,
                            goodsOrderDetails,
                            essayUserCorrectGoodsRepository,
                            getAllDetails
                    );
                } else {
                    notExistUserList.add(account);
                }
            } catch (Exception e) {
                errorUserList.add(account);
                log.error("UserCorrectGoodsServiceV2.reward exception", e);
            }
        });
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(notExistUserList)) {
            log.info("批量充值用户不存在，用户名是:{}", JsonUtil.toJson(notExistUserList));
        }

        // 充值记录
        EssayRewardRecord rewardRecord = new EssayRewardRecord();
        if (!errorUserList.isEmpty()) {
            rewardRecord.setErrorList(Joiner.on(",").join(errorUserList));
        }
        if (StringUtils.isEmpty(vo.getUrl())) {
            rewardRecord.setSource(Joiner.on(",").join(vo.getAccountList()));
        } else {
            rewardRecord.setSource(vo.getUrl());
        }
        rewardRecord.setRemark(vo.getRemark());
        rewardRecord.setSource(vo.getUrl());
        rewardRecord.setCreator(vo.getCreator());

        essayRewardRecordRepository.save(rewardRecord);

        rewardRecordDetails.forEach(rewardRecordDetail -> rewardRecordDetail.setRewardRecordId(rewardRecord.getId()));

        essayRewardRecordDetailRepository.save(rewardRecordDetails);

        return errorUserList;
    }
}
