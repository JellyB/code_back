package com.huatu.tiku.course.service.v6;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.huatu.common.Result;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.CourseServiceV6FallBack;
import com.huatu.tiku.course.netschool.api.fall.UserCourseServiceV6FallBack;
import com.huatu.tiku.course.netschool.api.v6.CourseServiceV6;
import com.huatu.tiku.course.netschool.api.v6.UserCourseServiceV6;
import com.huatu.tiku.course.service.v1.AccessLimitService;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-16 下午1:46
 **/
@Service
@Slf4j
public class CourseBizV6Service {

    @Autowired
    private CourseServiceV6 courseService;

    @Autowired
    private UserCourseServiceV6 userCourseService;

    @Autowired
    private CourseServiceV6FallBack courseServiceV6FallBack;

    @Autowired
    private UserCourseServiceV6FallBack userCourseServiceV6FallBack;

    @Autowired
    private AccessLimitService accessLimitService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 课程日历详情接口
     * @param params
     * @return
     */
    @Degrade(key = "calendarDetail", name = "课程日历详情")
    public Object calendarDetail(Map<String,Object> params){
        try{
            NetSchoolResponse netSchoolResponse = courseService.calendarDetail(params);
            if (null != netSchoolResponse && null != netSchoolResponse.getData()) {
                courseServiceV6FallBack.setCalendarDetailStaticData(params, netSchoolResponse);
            }
            return ResponseUtil.build(netSchoolResponse);
        }catch (Exception e){
            log.error("calendarDetail caught an exception and return from fallBack:{}", e);
            NetSchoolResponse netSchoolResponse = courseServiceV6FallBack.calendarDetail(params);
            return ResponseUtil.build(netSchoolResponse);
        }
    }

    /**
     * 我的学习日历接口
     * @param params
     * @return
     */
    @Degrade(key = "obtainLearnCalender", name = "我的学习日历")
    public Object obtainLearnCalender(Map<String,Object> params){
        try{
            NetSchoolResponse netSchoolResponse = userCourseService.obtainLearnCalender(params);
            if(null != netSchoolResponse && null != netSchoolResponse.getData()){
                userCourseServiceV6FallBack.setCalendarLearnStaticData(params, netSchoolResponse);
            }
            log.info("result={}",new Gson().toJson(netSchoolResponse));
            return ResponseUtil.build(netSchoolResponse);
        }catch (Exception e){
            log.error("param = {}", new Gson().toJson(params));
            log.error("obtainLearnCalender caught an exception and return from fallBack:{}", e);
            NetSchoolResponse netSchoolResponse = userCourseServiceV6FallBack.obtainLearnCalender(params);
            return ResponseUtil.build(netSchoolResponse);
        }
    }


    /**
     * 我的课程接口
     * @param params
     * @return
     */
    @Degrade(key = "obtainMineCourses", name = "我的课程")
    public Object obtainMineCourses(Map<String,Object> params){
        try{
            NetSchoolResponse netSchoolResponse = userCourseService.obtainMineCourses(params);
            if(null != netSchoolResponse && null !=  netSchoolResponse.getData() && ResponseUtil.isHardSuccess(netSchoolResponse)){
                userCourseServiceV6FallBack.setCourseMineStaticData(params, netSchoolResponse);
            }
            return ResponseUtil.build(netSchoolResponse);
        }catch (Exception e){
            log.error("obtainMineCourses caught an exception and return from fallBack:{}", e);
            NetSchoolResponse netSchoolResponse = userCourseServiceV6FallBack.obtainMineCourses(params);
            return ResponseUtil.build(netSchoolResponse);
        }
    }

    /**
     * 我的课程降级方法
     * @param params
     * @return
     */
    public Object obtainMineCoursesDegradeBack(Map<String,Object> params){
        if(accessLimitService.tryAccess()){
        NetSchoolResponse netSchoolResponse = userCourseServiceV6FallBack.obtainMineCourses(params);
        log.warn("obtainMineCoursesDegrade.data:{}", JSONObject.toJSONString(netSchoolResponse));
        return ResponseUtil.build(netSchoolResponse);
        }else{
            return new NetSchoolResponse<>(Result.SUCCESS_CODE, "当前请求的人数过多，请在5分钟后重试", Lists.newArrayList());
        }
    }

    /**
     * 我的课程降级方法
     * @param params
     * @return
     */
    public Object obtainMineCoursesDegrade(Map<String,Object> params){
        if(accessLimitService.tryAccess()){
            NetSchoolResponse netSchoolResponse = userCourseService.obtainMineCourses(params);
            log.info("obtainMineCoursesDegrade.data:{}", JSONObject.toJSONString(netSchoolResponse));
            return ResponseUtil.build(netSchoolResponse);
        }else{
            if(MapUtils.getInteger(params, "terminal") == 1){
                log.info("我的课程降级 -- 安卓:{}", params);
                return new NetSchoolResponse<>(Result.SUCCESS_CODE, "当前请求的人数过多，请在5分钟后重试", Lists.newArrayList());
            }else{
                log.info("我的课程降级 -- IOS:{}", params);
                return null;
            }
        }
    }


    /**
     * 我的学习日历降级接口
     * @param params
     * @return
     */
    public Object obtainLearnCalenderDegrade(Map<String,Object> params){
        /*NetSchoolResponse netSchoolResponse = userCourseServiceV6FallBack.obtainLearnCalender(params);
        log.warn("obtainLearnCalenderDegrade.data:{}", JSONObject.toJSONString(netSchoolResponse));
        return ResponseUtil.build(netSchoolResponse);*/
        return Lists.newArrayList();
    }

    /**
     * 课程日历详情接口降级接口
     * @param params
     * @return
     */
    public Object calendarDetailDegrade(Map<String,Object> params){
        /*NetSchoolResponse netSchoolResponse = courseServiceV6FallBack.calendarDetail(params);
        log.warn("calendarDetailDegrade.data:{}", JSONObject.toJSONString(netSchoolResponse));
        return ResponseUtil.build(netSchoolResponse);*/

        Map<String,Object> result = Maps.newHashMap();
        result.put("msg", "未来七天内，没有直播课哦~");
        result.put("type", 1);
        result.put("current_page", 1);
        result.put("data", Lists.newArrayList());
        result.put("date", "2019-03-18");
        result.put("month", "03");
        result.put("day", "18");
        result.put("perPage", params.get("pageSize"));
        result.put("last_page", 0);
        result.put("total", 0);
        result.put("from", 0);
        result.put("to", 0);
        return result;
    }

    public List<Map> getBaseClassInfo(List<Integer> classIds) {
        List<Map> result = Lists.newArrayList();
        for (Integer classId : classIds) {
            String baseCourseInfoKey = CourseCacheKey.getBaseCourseInfoKey(classId);
            Map hashMap = (Map)redisTemplate.opsForValue().get(baseCourseInfoKey);
            try {
                if(null == hashMap){
                    Map params = new HashMap(){
                        {
                            put("classId",classId);
                        }
                    };
                    NetSchoolResponse baseClassInfo = courseService.getBaseClassInfo(params);
                    Map data = (Map)baseClassInfo.getData();
                    data.put("id",classId);
                    redisTemplate.opsForValue().set(baseCourseInfoKey,data,1, TimeUnit.DAYS);
                    result.add(data);
                }else{
                    result.add(hashMap);
                }
            }catch (Exception e){
                e.printStackTrace();
                Map map = new HashMap(){
                    {
                        put("id",classId);
                    }
                };
                result.add(map);
            }

        }
        return result;
    }
}
