package com.huatu.tiku.schedule.biz.domain;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**课时统计规则
 * @author wangjian
 **/
@Getter
@Setter
@Entity
@ToString
public class Rule extends BaseDomain{


    private static final long serialVersionUID = -3774112518465708145L;
    /**
     * 课程分类
     */
    private CourseCategory courseCategory;

    /**
     * 考试类型
     */
    private ExamType examType;

    /**
     * 直播类型(授课,练习)
     */
    private CourseLiveCategory liveCategory;

    /**
     * 计算系数
     */
    private Float coefficient;

    @Temporal(TemporalType.DATE)
    private Date dateBegin;

    private Integer dateBeginInt;

    @Temporal(TemporalType.DATE)
    private Date dateEnd;

    private Integer dateEndInt;
}
