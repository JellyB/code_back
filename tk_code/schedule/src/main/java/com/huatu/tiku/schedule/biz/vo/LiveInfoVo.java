package com.huatu.tiku.schedule.biz.vo;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 重新排课获取直播信息
 * @author wangjian
 **/
@Data
public class LiveInfoVo implements Serializable{
    private static final long serialVersionUID = -2316591755494960266L;
    private boolean assflag;//需要助教
    private boolean comflag;//需要主持人
    private boolean ctrlflag;//需要场控
    private boolean ltflag;//需要学习师

    private ExamType examType;
    private String date;//时间
    private Long courseId;//课程id
    private String courseName;//课程名
    private Long liveId;//直播id
    private String liveName;//直播名
    private String assName;//助教名
    private Long assId;//助教id
    private CourseConfirmStatus assConfirm;//助教确认状态
    private String comName;
    private Long comId;
    private CourseConfirmStatus comConfirm;
    private String ctrlName;
    private Long ctrlId;
    private CourseConfirmStatus ctrlConfirm;
    private String ltName;
    private Long ltId;
    private CourseConfirmStatus ltConfirm;
    private List<LiveTeacher> liveTeachers;

    @Data
    public static class LiveTeacher{
        private Long id;
        private String coursePhase;
        private Long subjectId;
        private String subjectName;
        private Long moduleId;
        private String moduleName;
        private TeacherCourseLevel lever;//授课级别
        private Long teacherId;
        private String teacherName;
        private CourseConfirmStatus confirm;
        private Boolean readOnly;
    }

}
