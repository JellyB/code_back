package com.huatu.tiku.schedule.biz.domain;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author wangjian
 **/
@Getter
@Setter
@Entity
public  class ClassHourInfo extends BaseDomain {

    private static final long serialVersionUID = 7093957592966896503L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedbackId", insertable = false, updatable = false)
    private ClassHourFeedback feedback;

    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacherId", insertable = false, updatable = false)
    private Teacher teacher;

    private Long teacherId;

    private Double reallyExam;//真题题数

    private Double reallyHour;//真题课时

    private Double simulationExam;//模拟题数

    private Double simulationHour;//模拟题课时

    private Double articleHour;//文章课时

    private Double audioHour;//音频课时

    private String remark;
}
