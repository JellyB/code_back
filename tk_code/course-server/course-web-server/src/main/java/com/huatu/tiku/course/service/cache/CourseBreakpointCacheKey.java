package com.huatu.tiku.course.service.cache;

import java.util.Map;

/**
 * 知识点缓存
 * Created by lijun on 2018/6/19
 */
public class CourseBreakpointCacheKey {


    /**
     * 获取课程-断点缓存key
     * 此处考虑到使用降级、缓存管理工具中只能传入map
     *
     * @param map
     * @return
     */
    public static String breakpointKey(Map<String, Object> map) {
        Long courseId = Long.valueOf(String.valueOf(map.getOrDefault("courseId", "0")));
        Integer courseType = Integer.valueOf(String.valueOf(map.getOrDefault("courseType", "0")));
        return breakpointKey(courseId, courseType);
    }

    /**
     * 获取课程-断点缓存key
     *
     * @param courseId   课程ID
     * @param courseType 课程类型
     * @return key
     */
    public static String breakpointKey(Long courseId, Integer courseType) {
        return "course:breakpoint:" + courseType + "$" + courseId;
    }

    /**
     * 获取课程-断点-试题 缓存key 信息
     *
     * @param breakpointId 断点ID
     * @return
     */
    public static String breakpointQuestionKey(Long breakpointId) {
        return "course:breakpoint:question:" + breakpointId;
    }

}
