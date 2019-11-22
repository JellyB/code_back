package com.huatu.one.biz.feign;

import com.huatu.one.OneApplicationTests;
import com.huatu.one.biz.vo.PHPResponseWrapper;
import com.huatu.one.biz.vo.PHPScheduleResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class PHPScheduleClientT extends OneApplicationTests {

    @Autowired
    private PHPScheduleClient phpScheduleClient;

    @Test
    public void getSchedule() {
        Long teacherId = 237L;
        String teacherType = "teacher";
        String date = "2019-08-28";
        String token = "b5315e77-3cbe-4d73-ba01-cfa97ab6b4cc";

        PHPResponseWrapper<PHPScheduleResponse> schedule = phpScheduleClient.getSchedule(teacherId, teacherType, date, token);

        log.info("schedule is {}", schedule);
    }

    @Test
    public void getMonthSchedule(){
        Long teacherId = 237L;
        String teacherType = "teacher";
        String date = "2019-08";
        String token = "b5315e77-3cbe-4d73-ba01-cfa97ab6b4cc";

        PHPResponseWrapper<String> schedule = phpScheduleClient.getSchedulesPerMonth(teacherId, teacherType, date, token);

        log.info("month schedule is {}", schedule);
    }
}
