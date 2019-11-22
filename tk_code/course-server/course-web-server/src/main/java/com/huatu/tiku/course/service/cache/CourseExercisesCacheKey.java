package com.huatu.tiku.course.service.cache;

/**
 * Created by lijun on 2018/6/21
 */
public class CourseExercisesCacheKey {

    public static String CourseExercisesKey(Integer courseType, Long courseId) {
        return "course:exercises:question" + courseType + "$" + courseId;
    }
}
