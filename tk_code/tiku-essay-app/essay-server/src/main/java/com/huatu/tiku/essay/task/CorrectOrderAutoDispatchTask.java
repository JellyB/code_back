package com.huatu.tiku.essay.task;

import com.google.gson.Gson;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.dispatch.CorrectOrderAutoDispatchService;
import com.huatu.tiku.essay.service.v2.correctOrder.CorrectOrderSnapshotService;
import com.huatu.tiku.essay.util.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * @author huangqingpeng
 * @title: CorrectOrderAutoDispatchTask
 * @description: 自动派单逻辑
 * @date 2019-07-2515:23
 */
@Component
@Slf4j
public class CorrectOrderAutoDispatchTask extends TaskService {

    @Autowired
    CorrectOrderService correctOrderService;

    @Autowired
    CorrectOrderSnapshotService correctOrderSnapshotService;

    @Autowired
    EssayTeacherService essayTeacherService;

    @Autowired
    CorrectOrderAutoDispatchService correctOrderAutoDispatchService;
    private static final Gson gson = new Gson();


    private static final long expireTime = 1;
    private static final String cacheKey = "correct_order_auto_dispatch_task_lock";
    private static final long startTime = 7;
    private static final long endTime = 23;
    private static final Integer AUTO_BACK_TIME_OUT = 1;

    // @Scheduled(fixedRate = 60000)       //一分钟执行一次派单
    public void autoDispatch() {

        task();
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();

        Predicate<Long> isWork = (time -> time > getTodayStartTime() && time < getTodayEndTime());
        /**
         * 是否工作，工作中正常派单撤单，非工作期间不做自动撤单
         * 派单操作
         */
        if (isWork.test(currentTimeMillis)) {
            log.info("开始自动撤单++++++++++++++++++++++++");
            long time = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(AUTO_BACK_TIME_OUT);        //之前分配的订单均收回
            if (!isWork.test(time)) {
                time = time - (TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(startTime) - TimeUnit.HOURS.toMillis(endTime));
            }
            correctOrderAutoDispatchService.autoBack(time);

            log.info("开始自动派单+++++++++++++++++++++++++");
            /**
             * 查询今天待派单的所有订单
             */
            List<CorrectOrder> correctOrders = correctOrderAutoDispatchService.findWaitDispatchOrderList();
            log.info("需要派单的订单有{}个", CollectionUtils.isEmpty(correctOrders) ? 0 : correctOrders.size());
            if (CollectionUtils.isNotEmpty(correctOrders)) {
                for (CorrectOrder correctOrder : correctOrders) {
                    log.info("准备进行派单的订单：{}", gson.toJson(correctOrder));
                    //校验派单条件，实现派单
                    correctOrderAutoDispatchService.dispatch(correctOrder);
                }
            }
        } else {
            essayTeacherService.initTodayAmount();
        }

    }

    @Override
    protected long getExpireTime() {
        return expireTime;
    }

    @Override
    public String getCacheKey() {
        return cacheKey;
    }

    public long getTodayStartTime() {
        return DateUtil.getTodayStartMillions() + TimeUnit.HOURS.toMillis(startTime);
    }

    public long getTodayEndTime() {
        return DateUtil.getTodayStartMillions() + TimeUnit.HOURS.toMillis(endTime);
    }
}
