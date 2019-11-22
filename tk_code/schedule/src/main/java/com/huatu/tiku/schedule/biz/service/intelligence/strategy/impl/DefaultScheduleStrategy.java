package com.huatu.tiku.schedule.biz.service.intelligence.strategy.impl;

import com.huatu.tiku.schedule.biz.service.intelligence.PipLine;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import com.huatu.tiku.schedule.biz.service.intelligence.strategy.IScheduleStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by duanxiangchao on 2018/5/10
 */
@Service
public class DefaultScheduleStrategy implements IScheduleStrategy {


    @Resource
    private PipLine pipLine;

    @Override
    public void filterTeacher(TeacherDataEvent event) {

        if(event.getTeacherDates().size() > 0){
            pipLine.getDefaultSchedulePipLine().execute(event);
        }

    }

}
