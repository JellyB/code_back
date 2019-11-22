package com.huatu.tiku.course.mq.listeners;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticeForCoursePaper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 描述：课后作业保存答题卡队列监听
 *
 * @author biguodong
 * Create time 2019-03-07 5:24 PM
 **/
@Slf4j
@Component
public class CourseWorkAnswerCardSubmitListener {


    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;



    /**
     * 课后作业提交大调控监听处理
     * @param message
     */
    @RabbitListener(queues = RabbitMqConstants.COURSE_WORK_SUBMIT_CARD_INFO)
    public void onMessage(String message){

        JSONObject data = JSONObject.parseObject(message);
        JSONObject paper = data.getJSONObject("paper");
        PracticeForCoursePaper practiceForCoursePaper = JSONObject.parseObject(paper.toJSONString(), PracticeForCoursePaper.class);
        PracticeCard practiceCard = JSONObject.parseObject(message, PracticeCard.class);
        practiceCard.setPaper(practiceForCoursePaper);
        log.info("course_work_submit_card_info:{}", practiceCard.getId());
        courseExercisesProcessLogManager.submitCourseWorkAnswerCard(practiceCard);
    }
}
