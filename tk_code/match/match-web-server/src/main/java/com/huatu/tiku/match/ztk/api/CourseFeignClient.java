package com.huatu.tiku.match.ztk.api;

import com.huatu.tiku.match.common.FeignResponse;
import com.huatu.tiku.match.ztk.api.fallback.CourseFallBack;
import com.huatu.ztk.paper.common.ResponseMsg;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-03 下午8:15
 **/

@FeignClient(value = "t-course-server", path = "/c", fallback = CourseFallBack.class)
@Primary
public interface CourseFeignClient {

    String CLASS_ID = "classId";
    String COURSE_TITLE = "courseTitle";
    String LIVE_DATE = "liveDate";
    String PRICE = "price";
    /**
     * 获取解析课课程信息
     * @param params
     * @return
     */
    @GetMapping(value = "/v6/courses/courseAnalysis")
    FeignResponse analysis(@RequestParam Map<String,Object> params);

    /**
     * 是否领取课程
     * @param params userName、classId
     * @return
     */
    @GetMapping(value = "/v5/order/hasGetBigGiftOrder")
    ResponseMsg<Object> isHasGet(Map<String,Object> params);
}
