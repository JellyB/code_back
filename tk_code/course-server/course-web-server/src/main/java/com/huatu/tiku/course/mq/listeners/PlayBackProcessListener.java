package com.huatu.tiku.course.mq.listeners;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.course.bean.vo.RecordProcess;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-15 10:21 AM
 **/

@Component
@Slf4j
public class PlayBackProcessListener {

    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;


    /**
     * 录播观看到 85% 创建答题卡
     * @param message
     */
    @RabbitListener(queues = RabbitMqConstants.PLAY_BACK_DEAL_INFO)
    public void onMessage(String message){
        try{
            log.info("录播观看到 85% 创建课后作业答题卡v7:{}", message);
            RecordProcess recordProcess = JSONObject.parseObject(message, RecordProcess.class);
            if(null != recordProcess){
                courseExercisesProcessLogManager.dealRecordProcess(recordProcess);
            }else{
                return;
            }
        }catch (Exception e){
            log.error("大数据上报学习进度创建课后作业答题卡失败:{}", message);
        }
    }
}
