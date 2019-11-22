package com.huatu.tiku.essay.mq.listeners;

import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.EssayMockExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.huatu.tiku.essay.constant.status.SystemConstant.ANSWER_CORRECT_FINISH_QUEUE;

/**
 * Created by x6 on 2017/12/29.
 * 答案批改完成  （分数的四个Zset的处理）
 */
@Component
@Slf4j
public class MockAnswerCorrectFinishListener {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    EssayMockExamService essayMockExamService;


    @RabbitListener(queues = ANSWER_CORRECT_FINISH_QUEUE,containerFactory = "rabbitFactory")
    public void onMessage(Message message){
        try {
            String  finish =  messageConverter.fromMessage(message).toString();
            log.info("消息内容为:"+finish);
            essayMockExamService.correctFinish(finish, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
        } catch(MessageConversionException e){
            log.error("convert error，data={}",message,e);
            throw new AmqpRejectAndDontRequeueException("convert error...");
        } catch(Exception e){
            log.error("deal message error，data={}",message,e);
        }
    }

}
