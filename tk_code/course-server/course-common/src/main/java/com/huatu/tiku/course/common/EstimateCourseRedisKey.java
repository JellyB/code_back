package com.huatu.tiku.course.common;

/**
 * 模考关联课程信息缓存
 * Created by huangqingpeng on 2019/2/26.
 */
public class EstimateCourseRedisKey {

    private final static String SMALL_ESTIMATE_COURSE_ID_KEY = "small_estimate_course_id_key";


    public static String getSmallEstimateCourseIdKey(int subjectId){
        StringBuilder sb = new StringBuilder(SMALL_ESTIMATE_COURSE_ID_KEY);
        sb.append("_").append(subjectId);
        return sb.toString();
    }
}
