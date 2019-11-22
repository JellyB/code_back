package com.huatu.ztk.search.util;

import com.huatu.ztk.search.dao.CourseKeyWordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhengyi
 * @date 2019-03-07 18:14
 **/
@Component
public class CourseUtil {

    @Autowired
    private CourseKeyWordDao courseKeyWordDao;

    public CourseKeyWordDao get() {
        return courseKeyWordDao;
    }
}