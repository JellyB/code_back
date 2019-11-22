package com.huatu.ztk.knowledge.common.analysis.annotation;

import com.huatu.ztk.knowledge.common.analysis.event.AnalysisEvent;
import com.huatu.ztk.knowledge.common.analysis.model.EventEntity;
import com.huatu.ztk.knowledge.common.analysis.publisher.SpringContextPublisher;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhengyi
 * @date 2018-12-24 11:26
 **/
@Aspect
@Component
public class AnalysisReportHandler {

    private final SpringContextPublisher springContextPublisher;

    @Autowired
    public AnalysisReportHandler(SpringContextPublisher springContextPublisher) {
        this.springContextPublisher = springContextPublisher;
    }

    @Pointcut("@annotation(com.huatu.ztk.knowledge.common.analysis.annotation.AnalysisReport)")
    public void analysisReportCut() {
    }

    @Before("analysisReportCut()&&@annotation(analysisReport)")
    public void doBefore(AnalysisReport analysisReport) {
        System.out.println("开始记录->事件类型:" + analysisReport.value()+System.currentTimeMillis());
        EventEntity.newInstance(analysisReport.value());
    }

    @After("analysisReportCut()")
    public void after(JoinPoint joinPoint) {
        System.out.println("开始记录->事件类型:" + joinPoint.getStaticPart()+System.currentTimeMillis());
        springContextPublisher.pushEvent(new AnalysisEvent(EventEntity.getInstance()));
    }

}