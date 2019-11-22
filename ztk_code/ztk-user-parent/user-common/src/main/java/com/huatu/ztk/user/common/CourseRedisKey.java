package com.huatu.ztk.user.common;

/**
 * Created by linkang on 9/19/16.
 */
public class CourseRedisKey {
    /**
     * 课程名称
     */
    public static final String COMMENT_COURSE_NAME = "comment_course_name";

    /**
     * 总开关
     */
    public static final String COMMENT_COURSE_SWITCH = "comment_course_switch";


    public static String getCommentCourseSendMarkKey(long userId, String cv) {
        return String.format("comment_course_send_%s_%s", cv, userId);
    }
}

