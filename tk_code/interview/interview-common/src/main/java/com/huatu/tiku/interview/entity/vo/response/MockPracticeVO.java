package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.interview.entity.po.MockPractice;
import com.huatu.tiku.interview.entity.po.PracticeExpression;
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
public class MockPracticeVO  extends MockPractice {

    //练习内容名称
    private String practiceContentName = "全真模拟";
    private PaperDetailVO paper;
    //语音语调
    private List<PracticeExpression> pronunciationList;
    //流畅程度
    private  List<PracticeExpression> fluencyDegreeList;
    //仪态动作
    private  List<PracticeExpression> deportmentList;
}
