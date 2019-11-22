package com.huatu.tiku.course.service.cache;


import com.huatu.common.utils.web.RequestUtil;

import java.util.HashMap;

/**
 * 课程相关 key
 * Created by lijun on 2018/6/25
 */
public final class CourseCacheKey {

    /**
     * 课程合集
     */
    public static String collectionClassesKeyV4(HashMap map) {
        String paramSign = RequestUtil.getParamSign(map);
        return "course:v4:collection:" + paramSign;
    }


    /**
     * 课程详情 - 网页
     */
    public static String classExtKeyV5(int classId, int terminal) {
        return new StringBuilder("course:v5:classExt:").append(classId).append(":").append(terminal).toString();
    }

    /**
     * 课程大纲-售前
     */
    public static String timeTableKeyV5(int classId, int parentId, int page, int pageSize) {
        return new StringBuilder("course:v5:timeTable")
                .append(":").append(classId)
                .append(":").append(parentId)
                .append(":").append(page)
                .append(":").append(pageSize)
                .toString();
    }

    /**
     * 课程活动详情
     */
    public static String appClassActivityDetailsKeyV5(int classId){
        return new StringBuilder("course:v5:appClassActivityDetail:").append(classId).toString();
    }
}
