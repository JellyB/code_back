package com.huatu.tiku.course.mq.listeners;

import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 描述：课后作业数据纠正队列
 *
 * @author biguodong
 * Create time 2019-03-07 5:24 PM
 **/
@Slf4j
@Component
public class CourseWorkDataCorrectListener {


    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;



    /**
     * 课后作业数据纠正队列
     * @param message
     */
    @RabbitListener(queues = RabbitMqConstants.COURSE_EXERCISES_PROCESS_LOG_CORRECT_QUEUE)
    public void onMessage(String message){
        courseExercisesProcessLogManager.correct(message);
    }
}
