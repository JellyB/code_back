package com.huatu.tiku.schedule.biz.vo.CourseLivePackage;

import lombok.Data;

import java.io.Serializable;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;

/**返回直播教师封装
 * @author wangjian
 **/
@Data
public class LiveTeacherVo implements Serializable {

    private static final long serialVersionUID = -3606584758340653596L;
    private Long liveTeacherId;//id
    private Long teacherId;//教师id
    private String teacherName;//教师姓名
    private String coursePhaseKey;//阶段
    private String coursePhaseValue;
    private Long subjectId;//科目
    private String subjectName;
    private String teacherCourseLevelKey;
    private String teacherCourseLevelValue;//级别
    private Long moduleId;//模块
    private String moduleName;
    private CourseConfirmStatus confirm;//是否确认
}
