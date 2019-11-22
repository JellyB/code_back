package com.huatu.springboot.report.support;

import com.huatu.springboot.report.core.RabbitMqReportQueueEnum;
import com.huatu.tiku.common.bean.report.ReportMessage;
import com.huatu.tiku.common.bean.report.WebReportMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hanchao
 * @date 2018/1/11 15:27
 */
public class RabbitReporter implements MessageReporter {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;

    @Override
    public void report(ReportMessage message) {
        //直接使用json发送，避免不同序列化导致的问题
        rabbitTemplate.send(RabbitConsts.QUEUE_REPORT, jackson2JsonMessageConverter.toMessage(message, new MessageProperties()));
    }

    public void report(String queueName,Object message){
        rabbitTemplate.send(queueName, jackson2JsonMessageConverter.toMessage(message, new MessageProperties()));
    }

    public void report(RabbitMqReportQueueEnum[] rabbitMqReportQueue, ReportMessage message) {
        for (RabbitMqReportQueueEnum queueEnum : rabbitMqReportQueue) {
            //当前上报的数据是否只上报 自定义数据,减少IO 流
            boolean simpleData = queueEnum.isSimpleData() && message instanceof WebReportMessage;
            rabbitTemplate.send(
                    queueEnum.getQueueName(),
                    jackson2JsonMessageConverter.toMessage(simpleData ? ((WebReportMessage) message).getExtraData() : message, new MessageProperties())
            );
        }
    }

}
