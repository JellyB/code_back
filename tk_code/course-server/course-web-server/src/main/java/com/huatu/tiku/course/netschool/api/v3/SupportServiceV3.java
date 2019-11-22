package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/14 16:11
 */
@FeignClient(value = "course-service")
public interface SupportServiceV3 {
    /**
     * 课程 签到，签退
     * @param params
     * @return
     */
    @PostMapping("/v3/livecheck.php")
    NetSchoolResponse checkInOut(@RequestParam Map<String,Object> params);

}
