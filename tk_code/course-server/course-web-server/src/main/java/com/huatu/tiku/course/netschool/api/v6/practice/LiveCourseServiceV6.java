package com.huatu.tiku.course.netschool.api.v6.practice;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by lijun on 2019/2/21
 */
@FeignClient(value = "o-course-service", path = "/lumenapi/v5")
public interface LiveCourseServiceV6 {

    @GetMapping(value = "/blue/lesson/live_ids")
    NetSchoolResponse getLiveCourseIdListByRoomId(@RequestParam("bjyRoomId") Long bjyRoomId);

}
