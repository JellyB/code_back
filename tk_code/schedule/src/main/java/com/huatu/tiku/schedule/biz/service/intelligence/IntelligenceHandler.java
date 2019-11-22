package com.huatu.tiku.schedule.biz.service.intelligence;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import com.huatu.tiku.schedule.biz.service.intelligence.strategy.IScheduleStrategy;
import com.huatu.tiku.schedule.biz.service.intelligence.strategy.ScheduleStrategyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 */
@Service
public class IntelligenceHandler {

    @Resource
    private ScheduleStrategyFactory strategyFactory;

    public void schedule(TeacherType teacherType, List<TeacherScoreBean> scoreBeans, Date date, Integer timeBegin, Integer timeEnd){

        if(scoreBeans.size() > 0){
            TeacherDataEvent event = new TeacherDataEvent();
            event.setDate(date);
            event.setTimeBegin(timeBegin);
            event.setTimeEnd(timeEnd);

            List<TeacherDataEvent.TeacherData> teacherDatas = Lists.newArrayList();
            event.setTeacherDates(teacherDatas);
            scoreBeans.forEach(scoreBean ->{
                TeacherDataEvent.TeacherData teacherDataEvent = new TeacherDataEvent.TeacherData();
                teacherDataEvent.setTeacherId(scoreBean.getId());
                teacherDataEvent.setTeacherName(scoreBean.getName());
                teacherDataEvent.setWeight(scoreBean.getScore());
                teacherDatas.add(teacherDataEvent);
            });
            IScheduleStrategy scheduleStrategy = strategyFactory.getScheduleStrategy(teacherType);
            scheduleStrategy.filterTeacher(event);

            scoreBeans.clear();
            teacherDatas.forEach(teacherData -> {
                TeacherScoreBean teacherScoreBean = new TeacherScoreBean();
                teacherScoreBean.setId(teacherData.getTeacherId());
                teacherScoreBean.setName(teacherData.getTeacherName());
                teacherScoreBean.setScore(teacherData.getWeight());
                scoreBeans.add(teacherScoreBean);
            });
        }


    }

}
