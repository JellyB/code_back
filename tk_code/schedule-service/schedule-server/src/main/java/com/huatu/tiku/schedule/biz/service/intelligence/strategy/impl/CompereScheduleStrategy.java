package com.huatu.tiku.schedule.biz.service.intelligence.strategy.impl;

import com.huatu.tiku.schedule.biz.service.intelligence.PipLine;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import com.huatu.tiku.schedule.biz.service.intelligence.strategy.IScheduleStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 * 主持人
 */
@Service
public class CompereScheduleStrategy implements IScheduleStrategy {

    @Resource
    private PipLine pipLine;

    @Override
    public void filterTeacher(TeacherDataEvent events) {

        pipLine.getCompereSchedulePipLine().execute(events);

    }
}
