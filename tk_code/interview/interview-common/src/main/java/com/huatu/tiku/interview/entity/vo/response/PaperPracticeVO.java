package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.interview.entity.po.PaperPractice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/4/16.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaperPracticeVO extends PaperPractice {
    //练习内容名称
    private String practiceContentName = "套题演练";
    //------------举止仪态-------------
    private String behaviorAdvice;
    //------------语言表达-------------
    private String languageExpressionAdvice;
    //------------是否精准扣题-------------
    private String focusTopicAdvice;
    //------------是否条理清晰-------------
    private String isOrganizedAdvice;
    //------------是否言之有物-------------
    private String haveSubstanceAdvice;
}
