package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: 套题演练
 * @date 2018/7/27下午3:31
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaperPracticeExcelVO {
    private String answerDate;
    //学员姓名
    private String userName;
    //地区
    private String areaName;
    //班级
    private String className;


    //------------举止仪态-------------
    private String behavior;
    //------------语言表达-------------
    private String languageExpression;
    //------------是否精准扣题-------------
    private String focusTopic;
    //------------是否条理清晰-------------
    private String isOrganized;
    //------------是否言之有物-------------
    private String haveSubstance;



    //------------其他评价----------
    private String elseRemark;


}
