package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
public class MockPracticeExcelVO {
    private String answerDate;
    //学员姓名
    private String userName;
    //地区
    private String areaName;
    //班级
    private String className;


    //答案列表
    private List<String> answerList;

    //------------其他评价----------
    private String elseRemark;

    private Double overAllScore;
}
