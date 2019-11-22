package com.huatu.tiku.match.task;

import com.huatu.tiku.match.service.impl.v1.sync.MatchSyncStatusServiceImpl;
import com.huatu.tiku.match.service.v1.sync.MatchMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by huangqingpeng on 2019/2/25.
 */
@Component
@Slf4j
public class MatchSyncTask {

    @Autowired
    MatchSyncStatusServiceImpl matchSyncStatusService;

    @Autowired
    MatchMetaService matchMetaService;

    @Scheduled(fixedRate = 60000)
    public void syncMatchUserMeta() {
        //同步情况打印
        matchSyncStatusService.countSyncInfo();
        matchSyncStatusService.checkRunningId();
        boolean flag = matchSyncStatusService.checkRunningTime();
        if(!flag){
            log.info("同步任务启动失败，有模考大赛正在进行！！！");
            return;
        }
        //获取待同步的试卷Id（如果有正在处理的试卷，会返回-1，没有卷在需要同步的时候，也会返回-1）
        int waitingId = matchSyncStatusService.popWaitingId();
        if (waitingId < 0) {
            log.info("同步任务未成功获取到待同步试卷ID,直接结束！");
            return;
        }
        //获取同步锁，并将需要同步的试卷置为RUNNING
        boolean startFlag = matchSyncStatusService.startSync(waitingId);
        if(!startFlag){
            log.info("同步任务未抢到同步锁，直接结束！");
            return;
        }
        matchMetaService.syncMatchMetaInfo2DB(waitingId, -1);
    }


}
