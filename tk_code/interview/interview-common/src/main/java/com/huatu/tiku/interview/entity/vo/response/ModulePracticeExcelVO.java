package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.interview.entity.po.PracticeExpression;
import com.huatu.tiku.interview.entity.po.PracticeRemark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 学员练习数据导出(模块练习)
 * @date 2018/7/26下午9:32
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModulePracticeExcelVO {
    private String answerDate;
    //学员姓名
    private String userName;
    //地区
    private String areaName;
    //班级
    private String className;
    //语音语调
    private String pronunciationStr;
    //流畅程度
    private  String fluencyDegreeStr;
    //仪态动作
    private  String deportmentStr;
    //优点
    private String advantageStr;
    //问题
    private String disAdvantageStr;
    // 其他
    private String elseRemark;
    //综合评价
    private String totalRemark;


//    //语音语调
//    private List<PracticeExpression> pronunciationList;
//    //流畅程度
//    private  List<PracticeExpression> fluencyDegreeList;
//    //仪态动作
//    private  List<PracticeExpression> deportmentList;
//    //优点
//    private List<PracticeRemark> advantageList;
//    //问题
//    private List<PracticeRemark> disAdvantageList;


}
