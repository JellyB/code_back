package com.huatu.ztk.course.task;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.course.service.OrderService;
import com.huatu.ztk.user.bean.FreeCourseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * 赠送课程任务
 */
public class SendFreeCourseTask implements MessageListener{
    private static final Logger logger = LoggerFactory.getLogger(SendFreeCourseTask.class);

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}",text);
        try {
            final FreeCourseBean bean = JsonUtil.toObject(text, FreeCourseBean.class);
            final HashMap<String, Object> parameterMap = Maps.newHashMap();
            parameterMap.put("username", bean.getUsername());
            parameterMap.put("source", bean.getSource());
            parameterMap.put("tag", bean.getTag());

            orderService.sendFreeCourse(parameterMap, bean.getCatgory());
        }catch (Exception e){
            logger.error("ex，data={}",text,e);
        }
    }
}
