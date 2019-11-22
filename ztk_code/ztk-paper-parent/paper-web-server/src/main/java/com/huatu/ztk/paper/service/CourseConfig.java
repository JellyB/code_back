package com.huatu.ztk.paper.service;

import com.baidu.disconf.client.common.annotations.DisconfItem;
import org.springframework.stereotype.Component;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/10/29
 * @描述 解析课程合集课程url
 */
@Component
public class CourseConfig {
    private String courseUrl;

    @DisconfItem(key = "courseUrl")
    public String getCourseUrl() {
        return courseUrl;
    }

    public void setCourseUrl(String courseUrl) {
        this.courseUrl = courseUrl;
    }


}
