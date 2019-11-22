package com.huatu.tiku.course.netschool.api.fall;

import java.util.Map;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.huatu.common.utils.web.RequestUtil;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.util.ResponseUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-23 下午1:32
 **/
@Slf4j
@Component
public class UserCourseServiceV6FallBack{


    private static final String CALENDAR_LEARN_PRE = "_mock_calendar_learn$";
    private static final String COURSE_MINE_PRE = "_course_mine_learn$";

    /**
     * 我的学习-日历接口
     *
     * @param params
     * @return
     */
    public NetSchoolResponse obtainLearnCalender(Map<String, Object> params) {
        log.warn("response from call back obtainLearnCalender");
        String key = CALENDAR_LEARN_PRE + RequestUtil.getParamSign(params);
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(null == response){
            log.warn("obtain learn calender not in fallbackHolder...");
            return NetSchoolResponse.newInstance(Lists.newArrayList());
        }
        return response;
    }

    /**
     * 我的-已过期课程
     *
     * @param params
     * @return
     */
    public NetSchoolResponse obtainExpiredCourses(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    /**
     * 我的课程等筛选列表
     *
     * @param params
     * @return
     */
    public NetSchoolResponse obtainCourseFilterList(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    /**
     * 我的课程
     *
     * @param params
     * @return
     */
    public NetSchoolResponse obtainMineCourses(Map<String, Object> params) {
        log.warn("response from call back obtainMineCourses");
        String key = COURSE_MINE_PRE + RequestUtil.getParamSign(params);
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(!ResponseUtil.isHardSuccess(response)){
            log.warn("obtain mine courses not in fallbackHolder...");
            Map<String,Object> fallBack = Maps.newHashMap();
            fallBack.put("data", Lists.newArrayList());
            fallBack.put("total", 0);
            fallBack.put("current_page", 1);
            fallBack.put("per_page", 10);
            fallBack.put("last_page", 0);
            fallBack.put("topNumber", 0);
            return NetSchoolResponse.newInstance(fallBack);
        }
        return response;
    }

    /**
     * 缓存我的学习日历接口
     * @param params
     * @param response
     */
    public void setCalendarLearnStaticData(Map<String,Object> params, NetSchoolResponse response){
        String key = CALENDAR_LEARN_PRE + RequestUtil.getParamSign(params);
        if(ResponseUtil.isSuccess(response)){
            FallbackCacheHolder.put(key, response);
        }
    }

    /**
     * 缓存我的课程数据接口
     * @param params
     * @param response
     */
    public void setCourseMineStaticData(Map<String,Object> params, NetSchoolResponse response){
        String key = COURSE_MINE_PRE + RequestUtil.getParamSign(params);
        if(ResponseUtil.isSuccess(response)){
            FallbackCacheHolder.put(key, response);
        }
    }
}
