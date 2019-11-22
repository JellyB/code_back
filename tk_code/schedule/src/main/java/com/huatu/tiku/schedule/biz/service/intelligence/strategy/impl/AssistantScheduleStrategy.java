package com.huatu.tiku.schedule.biz.service.intelligence.strategy.impl;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.service.intelligence.PipLine;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import com.huatu.tiku.schedule.biz.service.intelligence.strategy.IScheduleStrategy;
import com.huatu.tiku.schedule.biz.vo.TeacherVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 * 助教
 */
@Service
public class AssistantScheduleStrategy implements IScheduleStrategy {

    @Resource
    private PipLine pipLine;

    @Override
    public void filterTeacher(TeacherDataEvent event) {

        pipLine.getAssistantSchedulePipLine().execute(event);

    }
}
