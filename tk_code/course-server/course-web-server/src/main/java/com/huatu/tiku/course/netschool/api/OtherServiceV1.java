package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.ExpressResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static com.huatu.tiku.course.consts.NetSchoolUrlConst.IOS_PAY_VERIFY;
import static com.huatu.tiku.course.consts.NetSchoolUrlConst.LOGISTICS_QUERY;

/**
 * 针对api.huatu.com的
 * @author hanchao
 * @date 2017/8/31 15:44
 */
@FeignClient(value = "o-course-service")
public interface OtherServiceV1 {
    @RequestMapping(value = LOGISTICS_QUERY,method = RequestMethod.GET)
    ExpressResult detail(@RequestParam Map<String,Object> params);

    @PostMapping(value=IOS_PAY_VERIFY)
    String payOrder(@RequestParam Map<String, Object> parameterMap);
}
