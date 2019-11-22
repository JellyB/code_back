package com.huatu.tiku.schedule.biz.vo;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.domain.VideoFeedback;
import com.huatu.tiku.schedule.biz.domain.VideoFeedbackInfo;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class VideoFeedbackVo implements Serializable{

    private Long id;

    private Long courseId;

    private String courseName;

    private String date;

    private Integer year;

    private Integer month;

    private FeedbackStatus status;

    public VideoFeedbackVo(VideoFeedback videoFeedback){
        this.id=videoFeedback.getId();
        this.courseId=videoFeedback.getCourseId();
        this.status=videoFeedback.getFeedbackStatus();
        this.date= DateformatUtil.format0(videoFeedback.getCreatedDate());
        this.year=videoFeedback.getYear();
        this.month=videoFeedback.getMonth();
        this.courseName=videoFeedback.getCourse().getName();
    }

}
