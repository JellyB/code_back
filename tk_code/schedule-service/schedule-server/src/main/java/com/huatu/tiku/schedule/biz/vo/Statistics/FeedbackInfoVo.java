package com.huatu.tiku.schedule.biz.vo.Statistics;

import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.VideoFeedbackInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**录播反馈
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class FeedbackInfoVo extends CourseLive implements Serializable {

    private static final long serialVersionUID = -4871733646588268525L;

    private Date date;

    private Long teacherId;

    private Long id;

    private Double result;//剪辑时长

    private String courseName;

    public FeedbackInfoVo(VideoFeedbackInfo info){
        this.courseName=info.getVideoFeedback().getCourse().getName();
        this.date=info.getVideoFeedback().getDate();
        this.teacherId=info.getTeacherId();
        this.id=info.getId();
        this.result=info.getResult();
    }
}
