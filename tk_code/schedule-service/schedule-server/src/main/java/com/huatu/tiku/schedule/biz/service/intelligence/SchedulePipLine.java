package com.huatu.tiku.schedule.biz.service.intelligence;

import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;

import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 */
public interface SchedulePipLine {

    /**
     * execute pipeline begin,if teacherDataEvents is Empty,
     * then do nothing
     */
    void execute(TeacherDataEvent event);

    /**
     * add scheduleHandler to pipline,The program will be executed in sequence
     */
    void addLast(ScheduleHandler handler);

}
