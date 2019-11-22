package com.huatu.tiku.schedule.biz.service.intelligence.strategy;

import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.service.intelligence.strategy.impl.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by duanxiangchao on 2018/5/10
 */
@Service
public class ScheduleStrategyFactory {

    @Resource
    private DefaultScheduleStrategy defaultScheduleStrategy;
    @Resource
    private LearnerScheduleStrategy learnerScheduleStrategy;
    @Resource
    private AssistantScheduleStrategy assistantScheduleStrategy;
    @Resource
    private CompereScheduleStrategy compereScheduleStrategy;
    @Resource
    private ControllerScheduleStrategy controllerScheduleStrategy;
    @Resource
    private LecturerScheduleStrategy lecturerScheduleStrategy;


    public IScheduleStrategy getScheduleStrategy(TeacherType teacherType){
        IScheduleStrategy scheduleStrategy = null;
        if(teacherType == null){
            scheduleStrategy = defaultScheduleStrategy;
        } else {
            switch (teacherType) {
                case JS:
                    scheduleStrategy = learnerScheduleStrategy;
                    break;
                case ZJ:
                    scheduleStrategy = assistantScheduleStrategy;
                    break;
                case XXS:
                    scheduleStrategy = lecturerScheduleStrategy;
                    break;
                case CK:
                    scheduleStrategy = controllerScheduleStrategy;
                    break;
                case ZCR:
                    scheduleStrategy = compereScheduleStrategy;
                    break;
            }
        }
        return scheduleStrategy;
    }



}
