package com.huatu.tiku.schedule.biz.service.imple;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.ClassHourInfo;
import com.huatu.tiku.schedule.biz.domain.FeedbackUpdateLog;
import com.huatu.tiku.schedule.biz.dto.FeedbackUpdateDto;
import com.huatu.tiku.schedule.biz.repository.ClassHourInfoRepository;
import com.huatu.tiku.schedule.biz.service.ClassHourInfoService;
import com.huatu.tiku.schedule.biz.service.FeedbackUpdateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;

@Service
public class ClassHourInfoServiceImpl extends BaseServiceImpl<ClassHourInfo, Long> implements ClassHourInfoService {

    @Autowired
    private ClassHourInfoRepository classHourInfoRepository;

    @Autowired
    private FeedbackUpdateLogService feedbackUpdateLogService;

    @Transactional
    @Override
    public void updateClassHour(FeedbackUpdateDto feedbackUpdateDto) throws Exception {
        ClassHourInfo classHourInfo = classHourInfoRepository.getOne(feedbackUpdateDto.getId());

        String methodSuffix = feedbackUpdateDto.getField().substring(0, 1).toUpperCase() + feedbackUpdateDto.getField().substring(1);

        String getter = "get" + methodSuffix;
        String setter = "set" + methodSuffix;

        Method getterMethod = ReflectionUtils.findMethod(ClassHourInfo.class, getter);

        Class fieldType = ReflectionUtils.findField(ClassHourInfo.class, feedbackUpdateDto.getField()).getType();

        Object oriValue = getterMethod.invoke(classHourInfo);

        Method setterMethod = ReflectionUtils.findMethod(ClassHourInfo.class, setter, fieldType);

        setterMethod.invoke(classHourInfo, fieldType == Double.class ? Double.parseDouble(feedbackUpdateDto.getValue()) : feedbackUpdateDto.getValue());

        classHourInfoRepository.save(classHourInfo);

        FeedbackUpdateLog feedbackUpdateLog = new FeedbackUpdateLog();
        feedbackUpdateLog.setFeedbackId(classHourInfo.getFeedbackId());
        feedbackUpdateLog.setClassHourId(feedbackUpdateDto.getId());
        feedbackUpdateLog.setField(feedbackUpdateDto.getField());
        feedbackUpdateLog.setOriValue(oriValue == null ? "" : oriValue.toString());
        feedbackUpdateLog.setValue(feedbackUpdateDto.getValue());
        feedbackUpdateLog.setType(0);

        feedbackUpdateLogService.save(feedbackUpdateLog);
    }
}
