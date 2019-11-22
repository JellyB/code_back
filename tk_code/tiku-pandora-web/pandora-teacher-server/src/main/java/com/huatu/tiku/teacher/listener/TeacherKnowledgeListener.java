package com.huatu.tiku.teacher.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-04-24 上午11:46
 **/
@Slf4j
@Component
@RabbitListener(queues = "teacherKnowledge")
public class TeacherKnowledgeListener {

    @RabbitHandler
    public void process(Map<String, Object> message) {
        try {
            Set<Map.Entry<String, Object>> set = message.entrySet();
            for (Iterator i = set.iterator(); i.hasNext(); ) {
                Object object = i.next();
                System.out.println("::::::::::::::" + object);
            }

        } catch (Exception e) {
            log.error("消息消费异常。。。");
            e.printStackTrace();
        }

    }

}
