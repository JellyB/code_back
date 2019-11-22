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
public class TeacherVo {

    private Long teacherId;

    private String realName;

    private String nickName;

    private String phoneNum;

    private String teacherLevel;

    private String teacherStatus;

    private String department;

    private String area;

    private List<String> correctTypes;

    private BigDecimal teacherScore;

    private String currentCorrectAmount;

    private String totalCorrectAmount;

}
