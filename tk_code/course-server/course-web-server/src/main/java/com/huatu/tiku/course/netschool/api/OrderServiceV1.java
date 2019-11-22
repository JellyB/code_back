package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static com.huatu.tiku.course.consts.NetSchoolSydwUrlConst.*;
import static com.huatu.tiku.course.consts.NetSchoolUrlConst.*;

/**
 * @author hanchao
 * @date 2017/8/30 15:40
 */
@FeignClient(value = "course-service")
public interface OrderServiceV1 {
    @GetMapping(CREATE_ORDER_IOS)
    NetSchoolResponse createOrderIos(@RequestParam Map<String,Object> params);

    @GetMapping(SYDW_CREATE_ORDER_IOS)
    NetSchoolResponse createSydwOrderIos(@RequestParam Map<String,Object> params);

    @GetMapping(CREATE_ORDER_ANDROID)
    NetSchoolResponse createOrderAnroid(@RequestParam Map<String,Object> params);

    @GetMapping(SYDW_CREATE_ORDER_ANDROID)
    NetSchoolResponse createSydwOrderAnroid(@RequestParam Map<String,Object> params);

    @GetMapping(FREE_COURSE)
    NetSchoolResponse getFree(@RequestParam Map<String,Object> params);

    @GetMapping(SYDW_FREE_COURSE)
    NetSchoolResponse getFreeSydw(@RequestParam Map<String,Object> params);

}
