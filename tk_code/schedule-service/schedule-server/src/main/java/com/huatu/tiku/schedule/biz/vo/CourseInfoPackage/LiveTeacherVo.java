package com.huatu.tiku.schedule.biz.vo.CourseInfoPackage;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import lombok.Data;

import java.io.Serializable;

/**返回直播教师封装
 * @author wangjian
 **/
@Data
public class LiveTeacherVo implements Serializable {

    private static final long serialVersionUID = -3606584758340653596L;
    private Long liveTeacherId;//id
    private Long teacherId;//教师id
    private String teacherName;//教师姓名
    private Long subjectId;//科目
    private String subjectName;
    private String teacherCourseLevelKey;
    private String teacherCourseLevelValue;//级别
    private CourseConfirmStatus confirm;//是否确认
    private String teacherTypeKey;
    private String teacherTypeValue;
}
