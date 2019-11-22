package com.huatu.tiku.course.netschool.api;

import com.huatu.tiku.course.netschool.api.fall.HtmlServiceV1FallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static com.huatu.tiku.course.consts.NetSchoolUrlConst.COURSE_H5;

/**
 * @author hanchao
 * @date 2017/8/30 11:59
 */
@FeignClient(value = "course-service",fallbackFactory = HtmlServiceV1FallbackFactory.class)
public interface HtmlServiceV1 {
    @RequestMapping(value = COURSE_H5,method = RequestMethod.GET )
    String courseDetail(@RequestParam("rid")int rid);
}
