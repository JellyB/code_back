package com.huatu.one.biz.feign;

import com.huatu.one.biz.vo.PHPResponseWrapper;
import com.huatu.one.biz.vo.PHPScheduleResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PHP课表
 *
 * @author geek-s
 * @date 2019-08-28
 */
@FeignClient(name = "php-schedule", url = "${php-schedule.ribbon.listOfServers}")
public interface PHPScheduleClient {
    /**
     * 获取老师课表
     *
     * @param teacherId   老师ID
     * @param teacherType 老师类型
     * @param date        日期
     * @param token       令牌
     * @return 课表
     */
    @GetMapping("/v5/c/lesson/teacher_live_detail")
    PHPResponseWrapper<PHPScheduleResponse> getSchedule(@RequestParam("teacherId") Long teacherId, @RequestParam("role") String teacherType,

                                                        @RequestParam("beginTime") String date, @RequestHeader("token") String token);

    /**
     * 获取老师每月课程日期安排
     * @param teacherId
     * @param teacherType
     * @param date
     * @return
     */
    @GetMapping("/v5/c/lesson/teacher_live_month")
    PHPResponseWrapper<String> getSchedulesPerMonth(@RequestParam("teacherId") Long teacherId, @RequestParam("role") String teacherType,

                                                                      @RequestParam("month") String date, @RequestHeader("token") String token);


}
