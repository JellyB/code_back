package com.huatu.tiku.schedule.biz.domain;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**录播课时反馈
 * @author wangjian
 **/
@Entity
@Getter
@Setter
public class VideoFeedback extends BaseDomain {

    private static final long serialVersionUID = 3159178862690680999L;

    private Long courseId;//课程id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId", insertable = false, updatable = false)
    private Course course;//课程

    private Integer year;

    private Integer month;

    @Temporal(TemporalType.DATE)
    private Date date;

    private FeedbackStatus feedbackStatus;//审核状态

    @OneToMany(mappedBy = "videoFeedback",cascade = CascadeType.REMOVE)
    private List<VideoFeedbackInfo> infos;
}
