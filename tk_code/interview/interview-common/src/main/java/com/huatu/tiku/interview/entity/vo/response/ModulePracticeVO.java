package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.interview.entity.po.ModulePractice;
import com.huatu.tiku.interview.entity.po.PracticeExpression;
import com.huatu.tiku.interview.entity.po.PracticeRemark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/4/16.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModulePracticeVO extends ModulePractice {

    //练习内容名称
    private String practiceContentName;

    //语音语调
    private String pronunciation;
    //流畅程度
    private String fluencyDegree;
    //仪态动作
    private String deportment;
    //语音语调
    private List<PracticeExpression> pronunciationList;
    //流畅程度
    private  List<PracticeExpression> fluencyDegreeList;
    //仪态动作
    private  List<PracticeExpression> deportmentList;
    //优点
    private List<PracticeRemark> advantageList;
    //问题
    private List<PracticeRemark> disAdvantageList;

}
