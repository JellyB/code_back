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

    @NotNull(message = "直播教师数据id不能为空")
    private Long liveTeacherId;

    @NotNull(message = "教师不能为空")
    private Long teacherId;

    private TeacherCourseLevel level;
}
