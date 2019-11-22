package com.huatu.tiku.course.netschool.api.v3;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/8/30 16:50
 */
@FeignClient(value = "course-service")
public interface SaleServiceV3 {
    @GetMapping("/v3/isSaleOut.php")
    NetSchoolResponse getDetail(@RequestParam Map<String, Object> params);

}
