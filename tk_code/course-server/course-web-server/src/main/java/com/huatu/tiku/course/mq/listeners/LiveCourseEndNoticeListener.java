package com.huatu.tiku.course.mq.listeners;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.manager.EssayExercisesAnswerMetaManager;
import com.huatu.tiku.course.util.RedisLockHelper;
import com.huatu.tiku.essay.essayEnum.CourseWareTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-11-08 11:15 AM
 **/

@Slf4j
@Component
public class LiveCourseEndNoticeListener {

    @Autowired
    private EssayExercisesAnswerMetaManager essayExercisesAnswerMetaManager;


    @Autowired
    private RedisLockHelper redisLockHelper;

    @RabbitListener(queues = RabbitMqConstants.LIVE_COURSE_END_NOTICE)
    public void onMessage(String message){
        log.info("定时任务处理申论老师定时发送下课学员信息:{}", message);

        JSONObject data = JSONObject.parseObject(message);
        int classId = data.getIntValue("classId");
        long syllabusId = data.getLongValue("syllabusId");
        int userId = data.getIntValue("userId");
        int courseWareId = data.getInteger("courseWareId");

        String key = "essay.live.end" + userId + "" + syllabusId;
        long time = System.currentTimeMillis();

        if(redisLockHelper.lock(key, String.valueOf(time), 5 * 1000, TimeUnit.MILLISECONDS)){
            essayExercisesAnswerMetaManager.createEssayInitUserMeta(userId, syllabusId, CourseWareTypeEnum.TableCourseTypeEnum.LIVE.getType(), courseWareId, classId);
        }
        redisLockHelper.unlock(key, String.valueOf(time));
    }
}
