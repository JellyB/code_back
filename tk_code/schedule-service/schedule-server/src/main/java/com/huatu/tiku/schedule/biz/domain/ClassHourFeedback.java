package com.huatu.tiku.schedule.biz.domain;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**教研课时反馈
 * @author wangjian
 **/
@Getter
@Setter
@Entity
public class ClassHourFeedback extends BaseDomain{

    private static final long serialVersionUID = -6472512190609176326L;

    private ExamType examType;//考试类型

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subjectId", insertable = false, updatable = false)
    private Subject subject;

    private Long subjectId;

    private Integer year;

    private Integer month;

    private FeedbackStatus status;

    @Temporal(TemporalType.DATE)
    private Date date;

    /**
     * 课程直播
     */
    @OneToMany(mappedBy = "feedback",cascade = CascadeType.REMOVE)
    private List<ClassHourInfo> infos;



}
