package com.huatu.tiku.schedule.biz.vo;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 重新排课获取直播信息
 * @author wangjian
 **/
@Data
public class LiveInfoVo implements Serializable{
    private static final long serialVersionUID = -2316591755494960266L;

    private ExamType examType;
    private String date;//时间
    private Long courseId;//课程id
    private String courseName;//课程名
    private Long liveId;//直播id
    private String liveName;//直播名

    private Long id;
    private Long subjectId;
    private String subjectName;
    private TeacherCourseLevel level;//授课级别
//    private Long teacherId;
//    private String teacherName;
    private Map teacher;
    private CourseConfirmStatus confirm;
    private TeacherType teacherType;

}
