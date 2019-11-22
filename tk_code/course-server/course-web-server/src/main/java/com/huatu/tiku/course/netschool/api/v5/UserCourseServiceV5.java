package com.huatu.tiku.course.netschool.api.v5;

import com.google.common.collect.Maps;
import com.huatu.common.Result;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.util.ResponseUtil;
import com.netflix.hystrix.HystrixCommand;
import feign.hystrix.Fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.ImmutableBean;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 用户个人课程信息相关
 * Created by lijun on 2018/6/25
 */
@FeignClient(value = "o-course-service", path = "/lumenapi", fallbackFactory = UserCourseServiceV5.UserCourseServiceV5FallbackFactory.class)
public interface UserCourseServiceV5 {

    /**
     * 查询我的课程-已删除
     *
     * @param params
     */
    @GetMapping(value = "/v4/common/user/my_course?isDelete=1")
    NetSchoolResponse getMyDeletedClasses(@RequestParam Map<String, Object> params);

    /**
     * 查询我的课程-未删除
     */
    @GetMapping(value = "/v4/common/user/my_course?isDelete=0")
    NetSchoolResponse getMyNotDeletedClasses(@RequestParam Map<String, Object> params);

    /**
     * 查询我的直播日历
     */
    @GetMapping(value = "/v4/common/class/live_calendar")
    NetSchoolResponse liveCalendar(@RequestParam("userName") String userName);

    /**
     * 直播日历详情
     */
    @GetMapping(value = "/v4/common/class/live_detail")
    NetSchoolResponse liveCalendarDetail(@RequestParam("id") String idList);

    @Slf4j
    @Component
    class UserCourseServiceV5FallbackFactory implements Fallback<UserCourseServiceV5>{
        @Override
        public UserCourseServiceV5 create(Throwable throwable, HystrixCommand command) {
            return new UserCourseServiceV5(){
                /**
                 * 查询我的课程-已删除
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getMyDeletedClasses(Map<String, Object> params) {
                    log.error("user course service v5 getMyDeletedClasses fallback,params: {}, fall back reason: {}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 查询我的课程-未删除
                 *
                 * @param params
                 */
                @Override
                public NetSchoolResponse getMyNotDeletedClasses(Map<String, Object> params) {
                    log.error("user course service v5 getMyNotDeletedClasses fallback,params: {}, fall back reason: {}", params, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 查询我的直播日历
                 *
                 * @param userName
                 */
                @Override
                public NetSchoolResponse liveCalendar(String userName) {
                    log.error("user course service v5 liveCalendar fallback,params: {}, fall back reason: {}", userName, throwable);
                    return NetSchoolResponse.DEFAULT;
                }

                /**
                 * 直播日历详情
                 *
                 * @param idList
                 */
                @Override
                public NetSchoolResponse liveCalendarDetail(String idList) {
                    log.error("user course service v5 liveCalendarDetail fallback,params: {}, fall back reason: {}", idList, throwable);
                    Map<String,Object> data = Maps.newHashMap();
                    data.putAll(ResponseUtil.DEFAULT_PHP_PAGE);
                    data.put("date", "1990-01-01");
                    data.put("type", 3);
                    return new NetSchoolResponse(Result.SUCCESS_CODE, "", data);
                }
            };
        }
    }
}
