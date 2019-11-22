package com.huatu.tiku.essay.mq.listeners;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.essay.constant.course.CallBack;
import com.huatu.tiku.essay.constant.status.SystemConstant;
import com.huatu.tiku.essay.service.courseExercises.CallBackEssayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.huatu.tiku.essay.constant.status.SystemConstant.ESSAY_PAPER_REPORT_QUEUE;

/**
 * 直播转回放申论处理队列
 */
@Component
@Slf4j
public class CallBackEssayListener {

    @Autowired
    private CallBackEssayService callBackEssayService;

   @RabbitListener(queues = SystemConstant.CALL_BACK_FAN_OUT_ESSAY)
    public void onMessage(String message) {
       log.info("直播转回放 essay 处理:{}", message);
       CallBack callBack = JSONObject.parseObject(message, CallBack.class);
       callBackEssayService.updateBindingAndMetaInfo(callBack);
    }
}
