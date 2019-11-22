package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static com.huatu.tiku.course.consts.NetSchoolUrlConst.LOGISTICS;

/**
 * @author hanchao
 * @date 2017/8/30 14:55
 */
@FeignClient(value = "course-service")
public interface LogisticsServiceV1 {
    @RequestMapping(value = LOGISTICS,method = RequestMethod.GET)
    NetSchoolResponse queryList(@RequestParam Map<String,Object> params);

}
