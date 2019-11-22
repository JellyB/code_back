package com.huatu.tiku.schedule.biz.service.intelligence.strategy.impl;

import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.service.intelligence.PipLine;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import com.huatu.tiku.schedule.biz.service.intelligence.strategy.IScheduleStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 * 场控
 */
@Service
public class ControllerScheduleStrategy implements IScheduleStrategy {

    @Resource
    private PipLine pipLine;

    @Override
    public void filterTeacher(TeacherDataEvent event) {

        pipLine.getControllerSchedulePipLine().execute(event);

    }
}
