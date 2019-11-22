package com.huatu.tiku.course.netschool.api.fall;

import com.google.common.collect.Lists;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v3.UserCoursesServiceV3;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/10/2 16:10
 */
@Component
public class UserCourseServiceV3Fallback implements UserCoursesServiceV3 {
    @Override
    public NetSchoolResponse findProducts(Map<String, Object> params) {
        return NetSchoolResponse.newInstance(Lists.newArrayList());
    }

    @Override
    public NetSchoolResponse getProductIsBuy(Map<String, Object> params) {
        return NetSchoolResponse.newInstance(Lists.newArrayList());
    }

    @Override
    public NetSchoolResponse findUserCourses(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse findUserHideCourses(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse hideCourse(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse showCourse(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse getMyPackCourseDetail(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse getLiveCalendar(String username) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse getCalendarDetail(String ids) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse save1V1Table(Map<String,Object> p) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse get1V1Table(String p) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse getHighEndInfo(String p) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse setHighEndInfo(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }

    @Override
    public NetSchoolResponse highendRecieve(String p) {
        return NetSchoolResponse.DEFAULT_ERROR;
    }
}
