package com.huatu.tiku.essay.mq.listeners;

import com.huatu.tiku.essay.constant.status.SystemConstant;
import com.huatu.tiku.essay.vo.resp.CreateAnswerCardVO;
import com.huatu.tiku.essay.service.EssayMockExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by x6 on 2017/12/26.
 * 创建答题卡
 */
@Component
@Slf4j
public class MockCreateAnswerCardListener {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    EssayMockExamService essayMockExamService;

    @RabbitListener(queues = SystemConstant.CREATE_ESSAY_MOCK_ANSWER_CARD_QUEUE,containerFactory = "rabbitFactory")
    public void onMessage(Message message){
        try {
            log.debug("=====收到 创建答题卡 MQ队列消息======");
            CreateAnswerCardVO createAnswerCardVO = (CreateAnswerCardVO)messageConverter.fromMessage(message);
            essayMockExamService.createMockAnswerCard(createAnswerCardVO);

        } catch(MessageConversionException e){
            log.error("convert error，data={}",message,e);
            throw new AmqpRejectAndDontRequeueException("convert error...");
        } catch(Exception e){
            log.error("deal message error，data={}",message,e);
        }
    }


}
