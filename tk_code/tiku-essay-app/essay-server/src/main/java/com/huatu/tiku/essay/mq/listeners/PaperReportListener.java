package com.huatu.tiku.essay.mq.listeners;

import com.huatu.tiku.essay.service.EssayPaperReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.huatu.tiku.essay.constant.status.SystemConstant.ESSAY_PAPER_REPORT_QUEUE;

/**
 * 生成试卷批改报告
 */
@Component
@Slf4j
public class PaperReportListener {

    @Autowired
    EssayPaperReportService essayPaperReportService;
    @Autowired
    private MessageConverter messageConverter;


   @RabbitListener(queues = ESSAY_PAPER_REPORT_QUEUE)
    public void onMessage(Message message) {
        try {
            Long  answerCardId = Long.parseLong(messageConverter.fromMessage(message).toString());
            log.debug("=====收到 生成试卷批改报告 MQ队列消息,answerId:{}======",answerCardId);

            essayPaperReportService.getReport(answerCardId);
        } catch(MessageConversionException e){
            log.error("convert error，data={}",message,e);
            throw new AmqpRejectAndDontRequeueException("convert error...");
        } catch(Exception e){
            log.error("deal message error，data={}",message,e);
        }
    }


}
