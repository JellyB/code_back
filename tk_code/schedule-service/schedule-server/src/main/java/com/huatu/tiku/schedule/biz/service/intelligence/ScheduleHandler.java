package com.huatu.tiku.schedule.biz.service.intelligence;

import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;

import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 */
public interface ScheduleHandler {

    void work(ScheduleHandlerContext ctx, TeacherDataEvent dataEvent);

}
