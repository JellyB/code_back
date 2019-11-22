package com.huatu.naga.mq.listeners;

import com.huatu.naga.handler.MessageHandler;
import com.huatu.tiku.common.bean.report.ReportMessage;
import com.rabbitmq.client.Channel;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hanchao
 * @date 2018/1/18 15:51
 */
@Component
public class ReportMessageListener implements ChannelAwareMessageListener,InitializingBean {
    @Autowired
    private MessageConverter messageConverter;
    @Autowired(required = false)
    private List<MessageHandler> handlers;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            //可以通过直接json强转，因为已经写过类了，也可以根据type
            ReportMessage reportMessage = (ReportMessage) messageConverter.fromMessage(message);
            if(reportMessage == null){
                return;
            }
            for (MessageHandler handler : handlers) {
                if(handler.supports(reportMessage)){
                    handler.handleMessage(reportMessage);
                    break;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if(CollectionUtils.isNotEmpty(handlers)){
            AnnotationAwareOrderComparator.sort(handlers);
        }
    }
}
