package com.huatu.tiku.schedule.biz.domain;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**录播课时反馈详情
 * @author wangjian
 **/
@Entity
@Getter
@Setter
public class VideoFeedbackInfo extends BaseDomain {

    private static final long serialVersionUID = 3159178862690680999L;

    private Long courseId;//课程id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videoFeedbackId", insertable = false, updatable = false)
    private VideoFeedback videoFeedback;//反馈

    private Long videoFeedbackId;//反馈id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacherId", insertable = false, updatable = false)
    private Teacher teacher;

    private Long teacherId;//教师id

    private Double result;//剪辑时长

    private String remark;//备注

}
