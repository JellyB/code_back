package com.huatu.tiku.schedule.biz.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FeedbackUpdateLogVo {

    private Long id;

    private Long feedbackId;

    private String examType;

    private String subject;

    private Integer year;

    private Integer month;

    private String teacherName;

    private String field;

    private String oriValue;

    private String value;

    private String operator;

    private Date operationTime;
}
