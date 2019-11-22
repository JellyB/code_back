package com.huatu.tiku.schedule.biz.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class ClassHourInforVo implements Serializable {

    private static final long serialVersionUID = -7179156261312428915L;

    private Long id;

    private Long feedbackId;

    private Long teacherId;

    private String teacherName;

    private Double reallyExam;//真题题数

    private Double reallyHour;//真题课时

    private Double simulationExam;//模拟题数

    private Double simulationHour;//模拟题课时

    private Double articleHour;//文章课时

    private Double audioHour;//音频课时

    private String remark;
}
