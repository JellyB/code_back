package com.huatu.tiku.essay.service.dispatch;

import com.huatu.tiku.essay.entity.correct.CorrectOrder;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: CorrectOrderAutoDispatchService
 * @description: 派单行为
 * @date 2019-07-2515:47
 */
public interface CorrectOrderAutoDispatchService {

    /**
     * 查询接单超时的订单
     * @param time      时间戳，小于改时间戳的订单均需要退回重新分配
     */
    void autoBack(long time);

    /**
     * 查询待分配的订单
     * @return
     */
    List<CorrectOrder> findWaitDispatchOrderList();

    void dispatch(CorrectOrder correctOrder);



}
