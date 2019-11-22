package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.biz.enums.CoursePhase;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**接收更改阶段参数
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateCoursePhaseDto implements Serializable {
    private static final long serialVersionUID = 581532802706239103L;

    @NotNull(message = "ID不能为空")
    private Long courseLiveTeacherId;

    /**
     * 阶段
     */
    private CoursePhase coursePhase;
}
