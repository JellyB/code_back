package com.huatu.one.biz.controller.api.v1;

import com.huatu.one.biz.model.User;
import com.huatu.one.biz.service.ScheduleService;
import com.huatu.one.biz.service.UsageRecordService;
import com.huatu.one.biz.service.UserService;
import com.huatu.one.biz.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课表
 *
 * @author geek-s
 * @date 2019-08-26
 */
@RestController
@RequestMapping("/v1/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UsageRecordService usageRecordService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private UserService userService;

    /**
     * 根据日期查询课表
     *
     * @param date 日期
     * @return 课表
     */
    @GetMapping
    public Object list(String date, @RequestHeader String openid, @RequestHeader String version) {
        // 小程序审核中匿名/未审核逻辑
        String auditVersion = versionService.getVersion();

        User user = userService.selectByOpenid(openid);

        if (auditVersion.equals(version) && (user == null || user.getStatus().equals(1))) {
            return scheduleService.scheduleList(date);
        }

        usageRecordService.saveRecord(openid, 2);

        return scheduleService.list(openid, date);
    }

    /**
     * 根据月份查询课表安排
     *
     * @param date 日期
     * @return 课表
     */
    @GetMapping(value = "/monthSchedule")
    public Object monthScheduleList(String date, @RequestHeader String openid, @RequestHeader String version) {

        // 小程序审核中匿名/未审核逻辑
        String auditVersion = versionService.getVersion();

        User user = userService.selectByOpenid(openid);

        if (auditVersion.equals(version) && (user == null || user.getStatus().equals(1))) {
            return scheduleService.auditScheduleList(date);
        }

        return scheduleService.monthScheduleList(openid, date);
    }
}
