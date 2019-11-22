package com.huatu.tiku.schedule.biz.domain.delete;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.CourseStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

/**删除课程明细
 * @author wangjian
 **/
@Entity
@Getter
@Setter
public class DeleteCourseDetail extends BaseDomain {

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 删除原因
     */
    private String reason;

    /**
     * 删除时状态
     */
    private CourseStatus status;
}
