package com.huatu.tiku.course.netschool.api.v5;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.SecKillCourseInfo;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.HashMap;


/**
 * 描述：课程service v5 降级
 *
 * @author biguodong
 * Create time 2019-04-25 7:20 PM
 **/

@Component
@Slf4j
public class CourseDegradeServiceV5 {

    private static final String ClassId = "classId";
    private static final String NetClassId = "netClassId";
    @Autowired
    private CourseServiceV5 courseService;

    @Value("${course.secKill.qqGroupInfo}")
    private String qqGroupInfo;

    @Value("${course.secKill.lastPlayInfo}")
    private String lastPlayInfo;


    /**
     * 获取 qq群、课程学习总进度
     * @param params
     * @return
     */
    @Degrade(key = "getQqGroupScheduleV5", name = "qq群课程学习总进度")
    public Object qqGroupSchedule(HashMap<String, Object> params){
        NetSchoolResponse netSchoolResponse = courseService.qqGroupSchedule(params);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 获取 qq群、课程学习总进度 - 降级处理方法
     * @param params
     * @return
     */
    public Object qqGroupScheduleDegrade(HashMap<String, Object> params){
        SecKillCourseInfo instance = SecKillCourseInfo.getInstance();
        String classId = MapUtils.getString(params, ClassId);
        //如果降级中命中秒杀课
        if(instance.getClassId().equals(classId)){
            try{
                log.info("降级秒杀课命中...且不为空:{}, apollo 配置:{}", classId, qqGroupInfo);
                JSONObject info = JSONObject.parseObject(qqGroupInfo);
                return info;
            }catch (Exception e){
                log.error("parse qq group for sec kill course error:{}", e.getMessage());
                return Maps.newLinkedHashMap();
            }
        }else{
            log.info("降级秒杀课未命中...请求远端:{}", classId);
            return ResponseUtil.build(courseService.qqGroupSchedule(params));
        }
    }

    /**
     * 继续学习
     * @param params
     * @return
     */
    @Degrade(key = "lastPlayLessonV5", name = "继续学习")
    public Object lastPlayLesson(HashMap<String, Object> params){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        NetSchoolResponse netSchoolResponse = courseService.lastPlayLesson(params);
        Object result = ResponseUtil.build(netSchoolResponse);
        stopWatch.stop();
        log.info("继续学习 V5,响应时间:{}", stopWatch.prettyPrint());
        return result;
    }

    /**
     * 继续学习 -- 降级处理方法
     * @param params
     * @return
     */
    public Object lastPlayLessonDegrade(HashMap<String, Object> params){
        SecKillCourseInfo instance = SecKillCourseInfo.getInstance();
        String classId = MapUtils.getString(params, NetClassId);
        //如果降级中命中秒杀课
        if(instance.getClassId().equals(classId)){
            try{
                log.info("降级秒杀课命中...且不为空:{}, apollo 配置:{}", classId, lastPlayInfo);
                JSONObject info = JSONObject.parseObject(lastPlayInfo);
                return info;
            }catch (Exception e){
                log.error("parse last playLesson for sec kill course error:{}", e.getMessage());
                return Maps.newLinkedHashMap();
            }
        }else{
            log.info("降级秒杀课未命中...请求远端:{}", classId);
            return ResponseUtil.build(courseService.lastPlayLesson(params));
        }
    }
}
