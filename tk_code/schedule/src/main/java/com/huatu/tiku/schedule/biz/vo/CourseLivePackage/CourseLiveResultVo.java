package com.huatu.tiku.schedule.biz.vo.CourseLivePackage;

import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**返回课程属性封装
 * @author wangjian
 **/
@Data
public class CourseLiveResultVo implements Serializable {

    private static final long serialVersionUID = 1085431653471690422L;
    private List<String> head;//日期
    private List<List> body;//直播数据

    private Long courseId;//课程id
    private String courseName;//成名
    private String examTypeKey;//考试类型
    private String examTypeValue;
    private Long subjectId;//科目id
    private String subjectName;
    private Boolean learningTeacherFlag;//是否需要学习师
    private Boolean assistantFlag;//是否助教
    private Boolean controllerFlag;//是否场控
    private Boolean compereFlag;//主持人
    private String  statusKey;//状态
    private String  statusValue;
    private Boolean isInterview;
    private String place;//地点
    private CourseCategory category;//课程类型

    public CourseLiveResultVo(Course course){
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
        learningTeacherFlag=course.getLearningTeacherFlag();
        assistantFlag=course.getAssistantFlag();
        controllerFlag=course.getControllerFlag();
        compereFlag=course.getCompereFlag();
        CourseStatus status = course.getStatus();
        if(status!=null){
            statusKey=status.getText();
            statusValue=status.getValue();
        }
        if(course.getExamType().equals(ExamType.MS)){//面试类型
            isInterview=true; //是面试类型
        }else{
            isInterview=false;//不是面试类型
        }
        this.place=course.getPlace();
        this.category=course.getCourseCategory();
    }

}
