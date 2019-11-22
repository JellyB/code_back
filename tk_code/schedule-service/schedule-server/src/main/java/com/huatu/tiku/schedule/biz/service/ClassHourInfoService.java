package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.ClassHourInfo;
import com.huatu.tiku.schedule.biz.dto.FeedbackUpdateDto;

public interface ClassHourInfoService extends BaseService<ClassHourInfo, Long> {

    void updateClassHour(FeedbackUpdateDto feedbackUpdateDto) throws Exception;
}
