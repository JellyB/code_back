package com.huatu.one.biz.service;

import com.google.common.collect.Lists;
import com.huatu.one.biz.feign.PHPScheduleClient;
import com.huatu.one.biz.model.User;
import com.huatu.one.biz.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 课表
 *
 * @author geek-s
 * @date 2019-08-26
 */
@Service
public class ScheduleService {

    private final String TOEKN = "b5315e77-3cbe-4d73-ba01-cfa97ab6b4cc";

    @Autowired
    private PHPScheduleClient phpScheduleClient;

    @Autowired
    private UserService userService;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    /**
     * 根据老师ID，日期查询课表
     *
     * @param openid 微信ID
     * @param date   日期
     * @return 课表
     */
    public List<ScheduleVo> list(String openid, String date) {
        User user = userService.selectByOpenid(openid);

        PHPResponseWrapper<PHPScheduleResponse> schedules = phpScheduleClient.getSchedule(user.getTeacherId(), user.getTeacherType(), date, TOEKN);

        List<ScheduleVo> scheduleVos = Lists.newArrayList();

        schedules.getData().forEach(schedule -> {
            String time = sdf.format(schedule.getBeginTime()) + " - " + sdf.format(schedule.getEndTime());

            scheduleVos.add(ScheduleVo.builder()
                    .name(schedule.getClassTitle())
                    .content(schedule.getTitle())
                    .time(time)
                    .code(schedule.getJoinCode()).build());
        });

        return scheduleVos;
    }

    /**
     * 获取老师每月课程日期安排
     * @param openid 微信id
     * @param date 月份
     * @return
     */
    public Object monthScheduleList(String openid, String date) {
        User user = userService.selectByOpenid(openid);
        date=date.substring(0,7);
        PHPResponseWrapper<String> schedules = phpScheduleClient.getSchedulesPerMonth(user.getTeacherId(), user.getTeacherType(), date, TOEKN);
        List<MonthScheduleVo> scheduleVos = Lists.newArrayList();
        schedules.getData().forEach(schedule->{
            scheduleVos.add(MonthScheduleVo.builder().value(schedule).build());
        });
        return scheduleVos;
    }

    /**
     *  获取审核人员课程列表
     * @return
     */
    public List<ScheduleVo> scheduleList(String Date){
        List<ScheduleVo> scheduleVos = Lists.newArrayList();

        if(Date.equals("2019-08-19")){
            scheduleVos.add(ScheduleVo.builder()
                    .name("小学语文")
                    .content("第一节课")
                    .time("08:00:00-08:45:00")
                    .code("trob").build());
        }
        if(Date.equals("2019-08-30")){
            scheduleVos.add(ScheduleVo.builder()
                    .name("小学数学")
                    .content("第四节课")
                    .time("14:00:00-14:45:00")
                    .code("dgfg").build());
        }
        if(Date.equals("2019-09-04")){
            scheduleVos.add(ScheduleVo.builder()
                    .name("小学地理")
                    .content("第二节课")
                    .time("09:00:00-09:45:00")
                    .code("gdfg").build());
        }
        if(Date.equals("2019-09-12")){
            scheduleVos.add(ScheduleVo.builder()
                    .name("小学自然")
                    .content("第四课")
                    .time("13:00:00-13:45:00")
                    .code("pouv").build());
        }
        return scheduleVos;
    }

    /**
     * 获取审核月课程日期安排
     * @param date 月份
     * @return
     */
    public Object auditScheduleList(String date) {
        date=date.substring(0,7);
        List<MonthScheduleVo> scheduleVos = Lists.newArrayList();

        if(date.equals("2019-08")){
            scheduleVos.add(MonthScheduleVo.builder().value("2019-08-19").build());
            scheduleVos.add(MonthScheduleVo.builder().value("2019-08-30").build());
        }
        if(date.equals("2019-09")){
            scheduleVos.add(MonthScheduleVo.builder().value("2019-09-04").build());
            scheduleVos.add(MonthScheduleVo.builder().value("2019-09-12").build());
        }
        return scheduleVos;
    }
}
