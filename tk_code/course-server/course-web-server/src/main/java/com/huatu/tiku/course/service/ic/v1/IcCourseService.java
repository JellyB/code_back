package com.huatu.tiku.course.service.ic.v1;

import com.huatu.tiku.course.bean.CourseDetailV3DTO;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by lijun on 2018/7/18
 */
public interface IcCourseService {

    /**
     * 面库首页列表查询
     */
    List<HashMap<String, Object>> icClassList(HashMap<String, Object> map);

    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @param userId   用户ID
     */
    CourseDetailV3DTO getCourseDetail(int courseId, String userId) throws InterruptedException, ExecutionException;

    /**
     * 通过用户ID 获取当前用户已购买课程
     *
     * @param userId 用户ID
     */
    Object userBuyCourse(String userId);
}
