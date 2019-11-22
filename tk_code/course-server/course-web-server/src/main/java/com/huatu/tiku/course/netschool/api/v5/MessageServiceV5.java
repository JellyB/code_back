package com.huatu.tiku.course.netschool.api.v5;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by lijun on 2018/9/29
 */
@FeignClient(value = "o-course-service", path = "/lumenapi/v4/common/message/")
public interface MessageServiceV5 {

    @GetMapping("sendVerify")
    NetSchoolResponse sendVerify(@RequestParam Map<String, Object> params);
}
