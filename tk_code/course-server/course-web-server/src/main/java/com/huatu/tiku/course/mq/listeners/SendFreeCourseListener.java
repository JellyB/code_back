package com.huatu.tiku.course.mq.listeners;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.huatu.tiku.common.consts.CatgoryType;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.course.bean.FreeCourseBean;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.CourseServiceV1;
import com.huatu.tiku.course.netschool.api.SydwCourseServiceV1;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.springboot.basic.subject.SubjectEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;


/**
 * 赠送课程任务
 */
@Component
@Slf4j
public class SendFreeCourseListener {
    @Autowired
    private CourseServiceV1 courseService;
    @Autowired
    private SydwCourseServiceV1 sydwCourseService;

    @Autowired
    private MessageConverter messageConverter;

    /**
     * 送课
     * @param message
     */
    @RabbitListener(queues = RabbitConsts.QUEUE_SEND_FREE_COURSE)
    public void onMessage(Message message) {
        try {
            FreeCourseBean bean = (FreeCourseBean) messageConverter.fromMessage(message);
            HashMap<String, Object> parameterMap = Maps.newHashMap();
            parameterMap.put("username", bean.getUsername());
            parameterMap.put("source", bean.getSource());
            parameterMap.put("tag", bean.getTag());

            log.info(">>> send free course,username is {},source is {},tag is {}",bean.getUsername(),bean.getSource(),bean.getTag());

            NetSchoolResponse response = null;
            if(bean.getCatgory() == CatgoryType.GONG_WU_YUAN || bean.getCatgory() == SubjectEnum.GONGWUYUAN.code()){
                response = courseService.sendFree(RequestUtil.encryptJsonParams(parameterMap));
            }else{
                response = sydwCourseService.sendFree(RequestUtil.encryptJsonParams(parameterMap));
            }
            if(log.isInfoEnabled()){
                log.info(">>> send free result,response is {}", JSON.toJSONString(response));
            }

        } catch(MessageConversionException e){
            log.error("convert error，data={}",message,e);
            throw new AmqpRejectAndDontRequeueException("convert error...");
        } catch(Exception e){
            log.error("deal message error，data={}",message,e);
        }
    }


}
