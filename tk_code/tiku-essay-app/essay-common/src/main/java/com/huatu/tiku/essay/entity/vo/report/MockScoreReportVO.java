package com.huatu.tiku.essay.entity.vo.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.huatu.tiku.essay.vo.resp.MockQuestionAnswerVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by x6 on 2017/12/30.
 * 成绩报告
 */
@Builder
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockScoreReportVO {

    //模考名称	string	@mock=2018省模考大赛-申论
    private String name;

    private int areaEnrollCount;//地区报名人数
    private int areaRank;//地区排名
    private int totalEnroll;//全站报名人数
    private int totalRank;//全站排名
    private double maxScore;//全站最高分（double）
    //学员得分（double）
    private double examScore;
    private int totalCount;//总题目数
    private int unfinishedCount;//未完成题目数
    private double score;//满分
    private int spendTime;//学员用时
    private long answerCardId;//答题卡ID
    private long submitTime;


    //问题列表
    private List<MockQuestionAnswerVO> questionList;

    //折线数据
    private Line line;

    //地区id
    private long areaId;


    public MockScoreReportVO() {
        this.setQuestionList(Lists.newArrayList(new MockQuestionAnswerVO()));
        this.setLine(new Line());
    }


}
