package com.huatu.tiku.course.ic.api.v1;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 面库 用户-课程 订单信息
 * Created by lijun on 2018/7/18
 */
@FeignClient(value = "ic-service", path = "/co/cloud/v1/course")
public interface IcUserCourseServiceV1 {

    /**
     * 获取课程的销量
     */
    @GetMapping(value = "checkCourses")
    Object courseCountList(@RequestParam("courseIds") String courseIds);

    /**
     * 判断用户是否购买
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     */
    @GetMapping(value = "checkBuyCourse")
    Object checkUserBuy(@RequestParam("userId") String userId, @RequestParam("courseId") int courseId);

    /**
     * 获取用户已经购买的课程
     *
     * @param userId 用户ID
     * @return
     */
    @GetMapping(value = "/userId/{userId}")
    Object userHasBuyList(@PathVariable("userId") String userId);
}
