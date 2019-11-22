package com.huatu.tiku.schedule.biz.vo;

import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.domain.TeacherSubject;
import com.huatu.tiku.schedule.biz.enums.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**教师数据封装
 * @author wangjian
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TeacherVo implements Serializable{
    private static final long serialVersionUID = 6134071336353050660L;

    private Long TeacherId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 考试类型
     */
    private ExamType examType;

    private Long subjectId;

    private String subjectName;

    /**
     * 类型 0 组员 1 组长
     */
    private Boolean leaderFlag;

    /**
     * 教师类型
     */
    private TeacherType teacherType;

    /**
     * 手机
     */
    private String phone;

    /**
     * 微信
     */
    private String wechat;

    /**
     * 状态
     */
    private TeacherStatus status;

    /**
     * 教师职级
     */
    private TeacherLevel teacherLevel;

    /**
     * 授课集合
     */
    private List<TeacherSubjectVo> teacherSubjects;

    private Boolean isPartTime;

    private Boolean isInvalid;

    private Long pid;

    public TeacherVo(Teacher teacher){
        this.examType=teacher.getExamType();
        this.TeacherId=teacher.getId();
        this.leaderFlag=teacher.getLeaderFlag();
        this.name=teacher.getName();
        this.phone=teacher.getPhone();
        this.status=teacher.getStatus();
        this.teacherLevel=teacher.getTeacherLevel();
        Subject subject = teacher.getSubject();
        this.subjectId=teacher.getSubjectId();
        if(subject!=null){
            this.subjectName=subject.getName();
        }
        this.teacherType=teacher.getTeacherType();
        this.wechat=teacher.getWechat();
        this.pid = teacher.getPid();

        List<TeacherSubject> teacherSubjectList = teacher.getTeacherSubjects();
        if(teacherSubjectList!=null&&!teacherSubjectList.isEmpty()){
            teacherSubjects=new ArrayList<>();
            for(TeacherSubject teacherSubject:teacherSubjectList){
                TeacherSubjectVo teacherSubjectVo=new TeacherSubjectVo(teacherSubject);
                teacherSubjects.add(teacherSubjectVo);
            }
        }
        isPartTime=teacher.getIsPartTime();
        isInvalid=teacher.getIsInvalid();
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    private  class TeacherSubjectVo implements Serializable{

        private static final long serialVersionUID = -6750355599874243298L;

        /**
         * 教师授课id
         */
        private Long teacherSubjectId;
        /**
         * 考试类型
         */
        private ExamType examType;

        /**
         * 科目名称
         */
        private String subjectName;

        /**
         * 科目id
         */
        private Long subjectId;

        /**
         * 教师等级
         */
        private TeacherCourseLevel teacherCourseLevel;

        private TeacherSubjectVo(TeacherSubject teacherSubject){
            this.teacherSubjectId=teacherSubject.getId();
            this.examType=teacherSubject.getExamType();
            this.subjectId=teacherSubject.getSubjectId();
            if(subjectId!=null){
                this.subjectName=teacherSubject.getSubject().getName();
            }
            this.teacherCourseLevel=teacherSubject.getTeacherCourseLevel();
        }
    }

}
