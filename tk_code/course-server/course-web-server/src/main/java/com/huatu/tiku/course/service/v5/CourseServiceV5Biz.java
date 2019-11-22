package com.huatu.tiku.course.service.v5;

import java.util.HashMap;

/**
 * 封装 courseService 接口
 * Created by lijun on 2018/6/25
 */
public interface CourseServiceV5Biz {

    /**
     * 获取课程说明
     */
    String getClassExt(int classId, int terminal);

    /**
     * 课程大纲-售前
     */
    Object findTimetable(int classId, int parentId, int page, int pageSize);

    /**
     * 课程大纲-售后
     */
    Object findPurchasesTimetable(long userId, HashMap<String, Object> map);

    /**
     * 课程详情活动促销
     */
    Object appClassActivityDetails(int classId, int terminal);

}
