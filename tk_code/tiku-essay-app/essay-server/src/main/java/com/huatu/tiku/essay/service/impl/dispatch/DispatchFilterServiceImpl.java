package com.huatu.tiku.essay.service.impl.dispatch;

import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.snapshot.CorrectOrderSnapshotRepository;
import com.huatu.tiku.essay.service.dispatch.DispatchFilterService;
import com.huatu.tiku.essay.service.v2.correctOrder.CorrectOrderSnapshotService;
import com.huatu.tiku.essay.util.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.tools.jconsole.ProxyClient;

import java.util.Date;
import java.util.List;

/**
 * @author huangqingpeng
 * @title: DispatchFilterServiceImpl
 * @description: 自动派单的判断策略
 * @date 2019-07-2610:30
 */
@Service
@Slf4j
public class DispatchFilterServiceImpl implements DispatchFilterService {

    @Autowired
    CorrectOrderSnapshotRepository correctOrderSnapshotRepository;
    @Autowired
    CorrectOrderSnapshotService correctOrderSnapshotService;

    private static final Integer DISPATCH_COUNT_LIMIT = 3;      //三次自动派单机会
    @Override
    public boolean hasDispatchCount(CorrectOrder correctOrder) {
        List<CorrectOrderSnapshot> snapshots = correctOrderSnapshotRepository.findByOrderIdAndStatus(correctOrder.getId(), EssayStatusEnum.NORMAL.getCode());
        long count = snapshots.stream().filter(i -> i.getOperate() == CorrectOrderStatusEnum.OperateEnum.DISPATCH_AUTO.getCode()).count();
        if(!(count < DISPATCH_COUNT_LIMIT)){
            log.info("hasDispatchCount order :{} is {}",correctOrder.getId(),count < DISPATCH_COUNT_LIMIT);
        }
        return count < DISPATCH_COUNT_LIMIT;
    }

    @Override
    public boolean checkDispatchTime(CorrectOrder correctOrder) {
        int delayStatus = correctOrder.getDelayStatus();
        Date gmtCreate = correctOrder.getGmtCreate();
        boolean b = !(delayStatus == 1 && gmtCreate.getTime() < DateUtil.getTodayEndMillions());
        if(!b){
            log.info("checkDispatchTime order :{} is {}",correctOrder.getId(),b);
        }
        return b;
    }

    @Override
    public boolean checkDispatchSnapshot(CorrectOrder correctOrder) {
        boolean b = correctOrderSnapshotService.checkNoAdmin(correctOrder);
        if(!b){
            log.info("checkDispatchSnapshot order :{} is {}",correctOrder.getId(),b);
        }
        return b;
    }

    @Override
    public List<CorrectOrderSnapshot> findOperate(long id, CorrectOrderStatusEnum.OperateEnum dispatchAuto) {
        return correctOrderSnapshotService.findByOrderIdAndOperate(id,dispatchAuto);
    }
}
