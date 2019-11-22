package com.huatu.tiku.essay.service.task;

import com.huatu.tiku.essay.service.EssayMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author zhouwei
 * @Description: 异步任务demo
 * @create 2017-12-27 下午1:22
 **/
@Slf4j
@Component
public class AsyncMatchServiceImpl {


    @Autowired
    EssayMatchService essayMatchService;

    /**
     * 保存报名信息到mysql
     * @param positionId
     * @param paperId
     * @param userId
     */
    @Async
    public void saveEnrollToMysql(int positionId, long paperId, int userId) {
        essayMatchService.saveEnrollToMysql( positionId,  paperId,  userId);

    }
}
