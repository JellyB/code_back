package com.huatu.tiku.essay.task;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.entity.EssayCorrectGoods;
import com.huatu.tiku.essay.entity.EssayGoodsOrder;
import com.huatu.tiku.essay.entity.EssayGoodsOrderDetail;
import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayCorrectGoodsRepository;
import com.huatu.tiku.essay.repository.EssayGoodsOrderDetailRepository;
import com.huatu.tiku.essay.repository.EssayGoodsOrderRepository;
import com.huatu.tiku.essay.repository.EssayUserCorrectGoodsRepository;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: CorrectGoodsOrderTask
 * @description: TODO
 * @date 2019-08-0623:54
 */
@Slf4j
//@Component
public class CorrectGoodsOrderTask extends TaskService {

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

    private static ReentrantLock reentrantLock = new ReentrantLock();

    private static int page = 0;
    private static final int size = 5000;

   @Scheduled(fixedRate = 60000)
    public void syncOrderDetail() throws BizException {
        task();
    }

    @Override
    public void run() {
//        try{
//            log.info("开始处理用户批改次数记录及订单详情：{}->{}",page*size,(page+1)*size);
//            if(reentrantLock.tryLock()){
//                Pageable pageable = new PageRequest(page, size, Sort.Direction.DESC, "id");
//                Page<EssayUserCorrectGoods> all = essayUserCorrectGoodsRepository.findAll(pageable);
//                if (CollectionUtils.isEmpty(all.getContent())) {
//                    log.info("已完成用户所有批改次数记录");
//                    return;
//                }
//                for (EssayUserCorrectGoods userCorrectGoods : all.getContent()) {
//                    if (userCorrectGoods.getStatus() == -1) {
//                        continue;
//                    }
//                    try {
//                        handlerUserCorrectTimes(userCorrectGoods);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            page ++;
//            reentrantLock.unlock();
//            log.info("完成用户批改次数记录：{}",page * size);
//        }
        log.info("开始批改次数矫正");
        List<EssayUserCorrectGoods> all = essayUserCorrectGoodsRepository.findByStatusAndIsLimitNum(EssayStatusEnum.NORMAL.getCode(), 0);
        try {
            if (CollectionUtils.isEmpty(all)) {
                log.info("已完成用户所有批改次数记录");
                return;
            }
            for (EssayUserCorrectGoods userCorrectGoods : all) {
                try {
                    handlerUserCorrectTimes(userCorrectGoods);
                    userCorrectGoods.setIsLimitNum(1);
                    essayUserCorrectGoodsRepository.save(userCorrectGoods);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("完成批改次数矫正");
//        try {
//            log.info("开始矫正用户订单详情中status=0的数据");
//            List<EssayGoodsOrderDetail> essayGoodsOrderDetails = essayGoodsOrderDetailRepository.findByStatus(0);
//            log.info("查询到有{}个订单需要矫正", CollectionUtils.isEmpty(essayGoodsOrderDetails) ? 0 : essayGoodsOrderDetails.size());
//            for (EssayGoodsOrderDetail detail : essayGoodsOrderDetails) {
//                try {
//                    long recordId = detail.getRecordId();
//                    long goodsId = detail.getGoodsId();
//                    EssayCorrectGoods goods = findGoodsById(goodsId);
//                    EssayGoodsOrder order = essayGoodsOrderRepository.findOne(recordId);
//                    if(null == order || null == goods){
//                        detail.setStatus(EssayStatusEnum.DELETED.getCode());
//                        essayGoodsOrderDetailRepository.save(detail);
//                        continue;
//                    }
//                    int userId = order.getUserId();
//                    EssayUserCorrectGoods userCorrectGoods = essayUserCorrectGoodsRepository.findByUserIdAndStatusAndType(userId, EssayStatusEnum.NORMAL.getCode(), goods.getType());
//                    if(null == userCorrectGoods){
//                        detail.setStatus(EssayStatusEnum.DELETED.getCode());
//                        essayGoodsOrderDetailRepository.save(detail);
//                        continue;
//                    }
//                    handlerUserCorrectTimes(userCorrectGoods);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            log.info("完成矫正用户批改次数详情工作！！");
//        }


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
        Optional<EssayGoodsOrderDetail> first = orderDetails.stream().filter(i -> i.getRecordId() == -1L).findFirst();
        if (first.isPresent()) {
            EssayGoodsOrderDetail goodsOrderDetail = first.get();
            goodsOrderDetail.setStatus(EssayStatusEnum.DELETED.getCode());
            essayGoodsOrderDetailRepository.save(goodsOrderDetail);
        }
        orderDetails.sort(Comparator.comparing(i -> i.getGmtCreate()));
        Collections.reverse(orderDetails);
        for (EssayGoodsOrderDetail orderDetail : orderDetails) {
            if (orderDetail.getBizStatus() != EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.PAYED.getBizStatus() ||
                    orderDetail.getStatus() == EssayStatusEnum.DELETED.getCode()) {
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
                                detail.setStatus(1);
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
        EssayCorrectGoods goods = GoodsIdMap.getIfPresent(new Long(id).intValue());
        if (null == goods) {
            EssayCorrectGoods one = essayCorrectGoodsRepository.findOne(id);
            if (null != one) {
                GoodsIdMap.put(new Long(id).intValue(), one);
                return one;
            }
        }
        return goods;
    }

    @Override
    protected long getExpireTime() {
        return 1;
    }

    @Override
    public String getCacheKey() {
        return "sync_order_detail";
    }
}
