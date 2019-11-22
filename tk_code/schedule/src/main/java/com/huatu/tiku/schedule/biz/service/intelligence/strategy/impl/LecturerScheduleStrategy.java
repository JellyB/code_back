package com.huatu.tiku.schedule.biz.service.intelligence.strategy.impl;

import com.huatu.tiku.schedule.biz.service.intelligence.PipLine;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import com.huatu.tiku.schedule.biz.service.intelligence.strategy.IScheduleStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.RegEx;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 * 讲师
 */
@Service
public class LecturerScheduleStrategy implements IScheduleStrategy {

    @Resource
    private PipLine pipLine;

    @Override
    public void filterTeacher(TeacherDataEvent event) {

        pipLine.getLecturerSchedulePipLine().execute(event);

    }
}
