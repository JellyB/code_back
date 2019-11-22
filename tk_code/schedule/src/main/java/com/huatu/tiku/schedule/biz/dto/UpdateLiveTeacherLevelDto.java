package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import com.huatu.tiku.schedule.biz.enums.TeacherCourseLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**接收更改授课级别参数
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateLiveTeacherLevelDto implements Serializable{
    private static final long serialVersionUID = 49150611203816612L;

    @NotNull(message = "courseLiveTeacherId不能为空")
    private Long courseLiveTeacherId;

    /**
     * 授课级别
     */
    private TeacherCourseLevel teacherCourseLevel;
}
