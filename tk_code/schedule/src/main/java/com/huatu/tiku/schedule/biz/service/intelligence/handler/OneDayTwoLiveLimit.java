package com.huatu.tiku.schedule.biz.service.intelligence.handler;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.repository.TeacherRepository;
import com.huatu.tiku.schedule.biz.service.intelligence.ScheduleHandler;
import com.huatu.tiku.schedule.biz.service.intelligence.ScheduleHandlerContext;
import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 * 一天最多拍两节课
 */
@Service
public class OneDayTwoLiveLimit implements ScheduleHandler {


    private final TeacherRepository teacherRepository;

    @Autowired
    public OneDayTwoLiveLimit(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public void work(ScheduleHandlerContext ctx, TeacherDataEvent dataEvent) {

        Date date = dataEvent.getDate();

        List<TeacherDataEvent.TeacherData> teacherDatas = dataEvent.getTeacherDates();
        List<Long> ids = Lists.newArrayList();
        teacherDatas.forEach(teacherData ->  {
            ids.add(teacherData.getTeacherId());
        });

        List<Long> removeIds = Lists.newArrayList();
        teacherRepository.getTeacherLiveCount(ids, date).forEach(role -> {
            Integer count = Integer.parseInt(role[1].toString());
            if(count >= 2){
                removeIds.add(Long.parseLong(role[0].toString()));
            }
        });

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
