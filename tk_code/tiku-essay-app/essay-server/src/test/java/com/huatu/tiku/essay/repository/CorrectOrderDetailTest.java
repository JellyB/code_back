package com.huatu.tiku.essay.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.entity.EssayGoodsOrder;
import com.huatu.tiku.essay.entity.EssayGoodsOrderDetail;
import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;
import com.huatu.tiku.essay.essayEnum.CorrectOrderSnapshotChannelEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.service.impl.correct.UserOrderUtil;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: CorrectOrderDetailTest
 * @description: TODO
 * @date 2019-08-0621:13
 */
public class CorrectOrderDetailTest extends TikuBaseTest {

    @Autowired
    private EssayGoodsOrderRepository essayGoodsOrderRepository;
    @Autowired
    private EssayCorrectGoodsRepository essayCorrectGoodsRepository;
    @Autowired
    private EssayGoodsOrderDetailRepository essayGoodsOrderDetailRepository;
    @Autowired
    private EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository;

    Cache<Integer, EssayCorrectGoods> GoodsTypeMap = CacheBuilder.newBuilder().build();

    Cache<Integer, EssayCorrectGoods> GoodsIdMap = CacheBuilder.newBuilder().build();


    /**
     * 通过user总表数据矫正detail数据
     */
    @Test
    public void test() {
        int page = 0;
        int size = 100;
        while (true) {
            Pageable pageable = new PageRequest(page, size, Sort.Direction.ASC, "id");
            Page<EssayUserCorrectGoods> all = essayUserCorrectGoodsRepository.findAll(pageable);
            if (CollectionUtils.isEmpty(all.getContent())) {
                break;
            }
            for (EssayUserCorrectGoods userCorrectGoods : all.getContent()) {
                if (userCorrectGoods.getStatus() == -1) {
                    continue;
                }
                handlerUserCorrectTimes(userCorrectGoods);
            }
            page++;
            System.out.println("end = " + (page + 1) * size);
        }

    }

    /**
     * 针对单个用户某一类型的答题卡进行分析处理
     *
     * @param userCorrectGoods
     * @See 只适用于订单都是无期限的情况，其他情况不适用
     */
    private void handlerUserCorrectTimes(EssayUserCorrectGoods userCorrectGoods) {
        int totalNum = userCorrectGoods.getTotalNum();
        int usefulNum = userCorrectGoods.getUsefulNum();
        int userId = userCorrectGoods.getUserId();
        int goodsType = userCorrectGoods.getType();

        fillGoodsOrder(userId);

        List<EssayGoodsOrderDetail> orderDetails = essayGoodsOrderDetailRepository.findByUserIdAndGoodsType(userId, goodsType);
        if (CollectionUtils.isEmpty(orderDetails)) {
            createOrderDetail(userId, goodsType, totalNum, usefulNum);
            return;
        }
        orderDetails.sort(Comparator.comparing(i -> i.getGmtCreate()));
        Collections.reverse(orderDetails);
        for (EssayGoodsOrderDetail orderDetail : orderDetails) {
            if (orderDetail.getBizStatus() != EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus()) {
                orderDetail.setNum(orderDetail.getCount() * orderDetail.getUnit());
                continue;
            }
            int i = orderDetail.getUnit() * orderDetail.getCount();
            totalNum -= i;
            if (usefulNum > i) {
                orderDetail.setNum(i);
                usefulNum -= i;
            } else {
                orderDetail.setNum(usefulNum);
                usefulNum = 0;
            }
        }
        essayGoodsOrderDetailRepository.save(orderDetails);
        if (totalNum > 0) {
            createOrderDetail(userId, goodsType, totalNum, usefulNum);
        }

    }

    private void fillGoodsOrder(int userId) {
        List<EssayGoodsOrder> orders = essayGoodsOrderRepository.findByUserIdAndStatus(userId, EssayStatusEnum.NORMAL.getCode());
        orders.stream().filter(i -> null == i.getModifierId() || i.getModifierId().intValue() <= 0)
                .forEach(i -> {
                    i.setModifierId(new Long(userId));
                    essayGoodsOrderRepository.save(i);
                    List<EssayGoodsOrderDetail> details = essayGoodsOrderDetailRepository.findByRecordId(i.getId());
                    if (!CollectionUtils.isEmpty(details)) {
                        for (EssayGoodsOrderDetail detail : details) {
                            detail.setUserId(userId);
                            long goodsId = detail.getGoodsId();
                            EssayCorrectGoods goods = findGoodsById(goodsId);
                            if (goods == null) {
                                detail.setStatus(EssayStatusEnum.DELETED.getCode());
                            } else {
                                detail.setPrice(goods.getPrice() * detail.getCount());
                                detail.setUnit(goods.getNum());
                                detail.setNum(0);
                                detail.setExpireFlag(0);
                                detail.setIsLimitNum(1);
                                detail.setGoodsName(goods.getName());
                                detail.setGoodsType(goods.getType());
                                detail.setBizStatus(i.getBizStatus());
                            }
                            essayGoodsOrderDetailRepository.save(detail);
                        }
                    }
                });

    }

    private void createOrderDetail(int userId, int goodsType, int totalNum, int usefulNum) {
        EssayCorrectGoods goods = findGoodsByType(goodsType);
        if (null == goods) {
            return;
        }
        EssayGoodsOrderDetail build = EssayGoodsOrderDetail.builder()
                .expireFlag(0)
                .unit(goods.getNum())
                .isLimitNum(1)
                .goodsType(goodsType)
                .goodsId(goods.getId())
                .correctMode(1)
                .count(totalNum / goods.getNum())
                .goodsName(goods.getName())
                .num(1)
                .recordId(-1)
                .price(goods.getActivityPrice() * totalNum / goods.getNum())
                .userId(userId)
                .num(usefulNum).build();
        build.setStatus(EssayStatusEnum.NORMAL.getCode());
        build.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus());
        essayGoodsOrderDetailRepository.save(build);
        System.out.println("createOrderDetail = " + build.getId());
    }

    private EssayCorrectGoods findGoodsByType(int goodsType) {
        EssayCorrectGoods goods = GoodsTypeMap.getIfPresent(goodsType);
        if (null == goods) {
            List<EssayCorrectGoods> all = essayCorrectGoodsRepository.findAll();
            Map<Integer, EssayCorrectGoods> collect = all.stream().filter(i -> i.getStatus() == 1).filter(i -> i.getBizStatus() == 1).collect(Collectors.toMap(i -> i.getType(), i -> i));
            GoodsTypeMap.putAll(collect);
            goods = collect.get(goodsType);
        }
        return goods;
    }

    private EssayCorrectGoods findGoodsById(long id) {
        EssayCorrectGoods goods = GoodsIdMap.getIfPresent(id);
        if (null == goods) {
            EssayCorrectGoods one = essayCorrectGoodsRepository.findOne(id);
            if (null != one) {
                GoodsIdMap.put(new Long(id).intValue(), one);
            }
        }
        return null;
    }

    @Test
    public void test2() {
        int userId = 9433855;
        int goodsType = 0;
        EssayUserCorrectGoods userCorrectGoods = UserOrderUtil.createUserCorrectGoods(userId, goodsType, (a, b) -> essayGoodsOrderDetailRepository.findByUserIdAndGoodsType(a, b));
        System.out.println("new Gson().toJson(userCorrectGoods) = " + new Gson().toJson(userCorrectGoods));
        EssayUserCorrectGoods byUserIdAndStatusAndType = essayUserCorrectGoodsRepository.findByUserIdAndStatusAndType(userId, EssayStatusEnum.NORMAL.getCode(), goodsType);
        System.out.println("new Gson().toJson(byUserIdAndStatusAndType) = " + new Gson().toJson(byUserIdAndStatusAndType));
    }
}
