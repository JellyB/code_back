package com.huatu.tiku.schedule.biz.vo;

import com.huatu.tiku.schedule.biz.enums.*;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class RuleVo implements Serializable{
    private static final long serialVersionUID = 9106240148184947656L;

    private Long id;
    /**
     * 课程分类
     */
    private CourseCategory courseCategory;

    /**
     * 考试类型
     */
    private ExamType examType;

    /**
     * 直播类型(授课,练习)
     */
    private CourseLiveCategory liveCategory;

    /**
     * 计算系数
     */
    private Float coefficient;

    private String dateBegin;

    private String dateEnd;

    private String status;

    private SchoolType schoolType;

    private TeacherType teacherType;
}
