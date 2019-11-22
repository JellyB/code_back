package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.CourseConfirmStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class DGTBindTeacherDto implements Serializable{

    private static final long serialVersionUID = 3674789237374780598L;

    /**
     * 直播id
     */
    @NotNull(message = "直播ID不能为空")
    private Long liveId;

    /**
     * 课程直播教师ID
     */
    private Long courseLiveTeacherId;

    /**
     * 教师ID
     */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotNull(message = "教师确认状态不能为空")
    private CourseConfirmStatus confirm;
    /**
     * 助教类型
     */
    @NotNull(message = "教师类型不能为空")
    private TeacherType teacherType;

}
