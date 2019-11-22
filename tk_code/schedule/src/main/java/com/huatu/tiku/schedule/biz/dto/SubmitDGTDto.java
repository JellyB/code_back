package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class SubmitDGTDto implements Serializable{
    private static final long serialVersionUID = 933169741676250865L;

    @NotNull(message = "直播ID不能为空")
    private Long liveId;

    private Long liveTeacherId;

    @NotNull(message = "教师类型不能为空")
    private TeacherType teacherType;

    @NotNull(message = "教师不能为空")
    private Long teacherId;

    private TeacherCourseLevel level;
}
