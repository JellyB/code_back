package com.huatu.tiku.teacher.controller.admin.util;


import com.huatu.tiku.teacher.task.InitQuestionPointTreeTask;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("util/pool")
public class PoolController {

    @Autowired
    InitQuestionPointTreeTask initQuestionPointTreeTask;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 重置抽题池数据
     *
     * @return
     */
    @PostMapping("reset")
    public Object resetQuestionPointTree() {
        final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        Long expire = redisTemplate.getExpire(pointSummaryKey, TimeUnit.MINUTES);
        redisTemplate.expire(pointSummaryKey, 59, TimeUnit.MINUTES);
        initQuestionPointTreeTask.run();
        Long expire1 = redisTemplate.getExpire(pointSummaryKey, TimeUnit.MINUTES);
        return SuccessMessage.create(expire + "->" + expire1);
    }
}
