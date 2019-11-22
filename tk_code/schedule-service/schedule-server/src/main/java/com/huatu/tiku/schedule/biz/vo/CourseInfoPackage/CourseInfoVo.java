package com.huatu.tiku.schedule.biz.vo.CourseInfoPackage;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Data
public class CourseInfoVo implements Serializable{

    private static final long serialVersionUID = -4256380714662666038L;

    private Long courseId;//课程id
    private String courseName;//课程名
    private String examTypeKey;//考试类型
    private String examTypeValue;
    private Long subjectId;//科目id
    private String subjectName;
    private Boolean assistantFlag;//是否助教
    private Boolean controllerFlag;//是否场控
    private Boolean compereFlag;//主持人
    private String  statusKey;//状态
    private String  statusValue;
    private Boolean isInterview;
    private String place;//地点
    private CourseCategory category;//课程类型
    private List<CourseLiveInfoVo> lives;

    public CourseInfoVo(Course course){
        courseId=course.getId();
        courseName=course.getName();
        ExamType examType = course.getExamType();
        if(examType!=null){
            examTypeKey=examType.getText();
            examTypeValue=examType.getValue();
        }
        Subject subject = course.getSubject();
        if(subject!=null){
            subjectId=subject.getId();
            subjectName=subject.getName();
        }
        assistantFlag=course.getAssistantFlag();
        controllerFlag=course.getControllerFlag();
        compereFlag=course.getCompereFlag();
        CourseStatus status = course.getStatus();
        if(status!=null){
            statusKey=status.getText();
            statusValue=status.getValue();
        }
        isInterview = ExamType.MS.equals(course.getExamType());
        this.place=course.getPlace();
        this.category=course.getCourseCategory();
        List<CourseLive> courseLives = course.getCourseLives();
        if(courseLives!=null&&!courseLives.isEmpty()){
            lives= Lists.newArrayList();
            for (CourseLive courseLive : courseLives) {
                CourseLiveInfoVo live=new CourseLiveInfoVo(courseLive);
                lives.add(live);
            }
        }
    }
}
