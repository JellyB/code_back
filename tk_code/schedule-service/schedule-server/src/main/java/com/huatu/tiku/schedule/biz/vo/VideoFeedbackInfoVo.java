package com.huatu.tiku.schedule.biz.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class VideoFeedbackInfoVo implements Serializable{

    private Long id;

    private Long courseId;//课程id

    private Long teacherId;

    private String teacherName;

    private Double result;

    private String remark;
}
