package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static com.huatu.tiku.course.consts.NetSchoolSydwUrlConst.SYDW_SALE_DETAIL;
import static com.huatu.tiku.course.consts.NetSchoolUrlConst.SALE_DETAIL;

/**
 * @author hanchao
 * @date 2017/8/30 16:50
 */
@FeignClient(value = "course-service")
public interface SaleServiceV1 {
    @GetMapping(SALE_DETAIL)
    NetSchoolResponse queryDetail(@RequestParam  Map<String,Object> params);

    @GetMapping(SYDW_SALE_DETAIL)
    NetSchoolResponse querySydwDetail(@RequestParam  Map<String,Object> params);
}
