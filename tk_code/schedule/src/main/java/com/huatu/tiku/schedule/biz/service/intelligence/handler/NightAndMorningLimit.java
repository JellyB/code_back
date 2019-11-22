package com.huatu.tiku.schedule.biz.service.intelligence.handler;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.repository.TeacherRepository;
import com.huatu.tiku.schedule.biz.service.intelligence.ScheduleHandler;
import com.huatu.tiku.schedule.biz.service.intelligence.ScheduleHandlerContext;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import com.huatu.tiku.schedule.biz.util.TimeConstant;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 * 当天晚上和第二天早上的课不能放一起
 */
@Service
public class NightAndMorningLimit implements ScheduleHandler {

    private final TeacherRepository teacherRepository;

    @Autowired
    public NightAndMorningLimit(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public void work(ScheduleHandlerContext ctx, TeacherDataEvent dataEvent) {

        Date date = dataEvent.getDate();

        List<TeacherDataEvent.TeacherData> oldData = Lists.newArrayList();
        oldData.addAll(dataEvent.getTeacherDates());

        List<TeacherDataEvent.TeacherData> teacherDatas = dataEvent.getTeacherDates();
        List<Long> ids = Lists.newArrayList();
        teacherDatas.forEach(teacherData ->  {
            ids.add(teacherData.getTeacherId());
        });

        List<Long> removeIds = Lists.newArrayList();
        if(dataEvent.getTimeEnd() > TimeConstant.NIGHT_BEGIN_TIME){
            /**
             * 晚上的课，排除第二天早上有课的教师
             */
            date = DateUtils.addDays(date, 1);
            List<Object[]> objects = teacherRepository.getTeacherLiveCountMorning(ids, date, TimeConstant.MORNING_END_TIME);
            objects.forEach(role -> {
                removeIds.add(Long.parseLong(role[0].toString()));
            });
        }
        if(dataEvent.getTimeBegin() < TimeConstant.MORNING_END_TIME){
            /**
             * 早上的课，排除前一天晚上有课的教师
             */
            date = DateUtils.addDays(date, -1);
            List<Object[]> objects = teacherRepository.getTeacherLiveCountNight(ids, date, TimeConstant.NIGHT_BEGIN_TIME);
            objects.forEach(role -> {
                removeIds.add(Long.parseLong(role[0].toString()));
            });
        }

        Iterator<TeacherDataEvent.TeacherData> iterator = teacherDatas.iterator();
        while (iterator.hasNext()){
            TeacherDataEvent.TeacherData teacherData = iterator.next();
            if(removeIds.contains(teacherData.getTeacherId())){
                iterator.remove();
            }
        }

        if(teacherDatas.size() > 0){
            ctx.executeNext(dataEvent);
        } else {
            dataEvent.setTeacherDates(teacherDatas);
        }

    }
}
