package com.huatu.tiku.teacher.listener;

import com.huatu.tiku.teacher.service.SyncPaperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by huangqingpeng on 2018/9/5.
 */
@Slf4j
@Component
//@RabbitListener(queues = "sync_paper_question_queue")
public class SyncPaperQuestionListener {
    @Autowired
    SyncPaperService syncPaperService;
    @RabbitHandler
    public void onMessage(Map message) {
        try{
            log.info("message={}",message);
            Integer id = Integer.parseInt(String.valueOf(message.get("id")));
            syncPaperService.findPaperDetail(id);
        } catch (Exception e) {
            log.error("消息消费异常。。。");
            e.printStackTrace();
        }
    }
}
