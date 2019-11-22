package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/10
 */
@Data
@JsonInclude
public class TeacherDetailVo {

    private Long teacherId;

    private Long uCenterId;

    private String uCenterName;

    private String realName;

    private String nickName;

    private String phoneNum;

    private String email;

    private Integer teacherLevel;

    private String teacherLevelText;

    private Integer teacherStatus;

    private String teacherStatusText;

    private Integer department;

    private String departmentText;

    private String areaText;

    private List<Long> areaIds;

    private String entryDate;

    private List<String> correctTypeTexts;

    private List<Integer> correctType;

    private List<String> orderTypeTexts;

    private List<Integer> orderType;

    private BigDecimal teacherScore;

    private String baseSalary;

    private String maxLimit;

    private Integer questionLimit = 0;

    private Integer argumentLimit = 0;

    private Integer practicalLimit = 0;

    private Integer setQuestionLimit = 0;

    private String bankName;

    private String bankBranch;

    private String bankAddress;

    private String idCard;

    private String bankUserName;

    private String bankNum;

}
