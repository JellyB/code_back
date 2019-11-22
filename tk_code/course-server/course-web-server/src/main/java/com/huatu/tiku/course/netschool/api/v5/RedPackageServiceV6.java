package com.huatu.tiku.course.netschool.api.v5;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.RedServiceFallback;

/**
 * Created by lijun on 2018/9/28
 */
@FeignClient(value = "o-course-service", path = "/lumenapi/v4/common/red/" , fallback = RedServiceFallback.class)
public interface RedPackageServiceV6 {



    /**
     * 判断红包是否显示
     */
    @GetMapping(value = "show_redEnv")
    NetSchoolResponse showRedEvn();
}
