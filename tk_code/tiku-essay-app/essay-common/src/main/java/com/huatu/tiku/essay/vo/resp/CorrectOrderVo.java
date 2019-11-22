package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by duanxiangchao on 2019/7/18
 */
@Data
@JsonInclude
public class CorrectOrderVo {

    private Long taskId;

    private String taskType;

    private String questionContent;

    private String orderStatus;

    private String usedTime;

    private String finishTime;

    private Integer teacherScore;

    private String comment;

    private Integer actualSalary;

    private String settlementStatus;
}
