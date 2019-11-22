package com.huatu.tiku.course.bean.vo;

import lombok.*;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-05 10:41 AM
 **/


@NoArgsConstructor
@Setter
@Getter
public class CourseWorkCourseVo {

    private String courseTitle;
    private Long courseId;
    private int undoCount;
    private List<CourseWorkWareVo> wareInfoList;

    @Builder
    public CourseWorkCourseVo(String courseTitle, Long courseId, int undoCount, List<CourseWorkWareVo> wareInfoList) {
        this.courseTitle = courseTitle;
        this.courseId = courseId;
        this.undoCount = undoCount;
        this.wareInfoList = wareInfoList;
    }
}
