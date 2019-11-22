package com.huatu.tiku.schedule.biz.service.intelligence.strategy;

import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;

import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 */
public interface IScheduleStrategy {

    public void filterTeacher(TeacherDataEvent event);

}
