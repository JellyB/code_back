package com.huatu.tiku.course.netschool.api.v4;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "o-course-service")
public interface UserGoldServiceV4 {

    @GetMapping(value = "/lumenapi/v4/common/user/gold")
    NetSchoolResponse getUserGoldInfo(@RequestParam("userName") String userName);
}
