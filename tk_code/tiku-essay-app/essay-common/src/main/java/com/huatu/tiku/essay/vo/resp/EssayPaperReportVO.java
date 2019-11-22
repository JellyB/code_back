package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 套题批改报告响应VO
 * @date 2018/12/6 7:44 PM
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayPaperReportVO {
    //试卷名称1
    private String paperName;
    //批改时间1
    private String correctDate;
    //总分1
    private double score;
    //学员得分1
    private double examScore;
    //学员分数提升/下降分值
    private double examScoreChange;
    //全站排名1
    private long totalRank;
    //全站考试总人数1
    private long totalCount;
    //全站最高分1
    private double maxScore;
    //全站最高分提升/下降分值
    private double maxScoreChange;
    //全站平均分1
    private double avgScore;
    //全站排名1
    private int totalRankChange;

    /**
     * 批改情况
     */
    //题目数
    private int questionCount;
    //未作答题目数1
    private int unfinishedCount;
    //总用时1
    private int spendTime;
    //题目列表
    private List<EssayPaperReportQuestionVO> questionVOList;

    /**
     * 老师评价（本题阅卷评价～套卷的综合阅卷）
     */
    private List<RemarkVo> remarkList;

    /**
     * 名师之声
     */
    private int audioId;

    /**
     * 学员是否评价批注 0 未评价 1 评价
     */
    private int feedBackStatus;

    /**
     * 百家云token
     */
    private String token;

    /**
     * 名师之声百家云音频url
     */

    /**
     * 名师之声token
     */
    private String audioToken;

    private long paperId;

    /**
     * 批改方式(智能批改)
     */
    private int correctMode;
    /**
     * 学员报告评价的星级
     */
    private int feedBackStar;
    /**
     * 学员报告评价内容
     */
    private String feedBackContent;

    /**
     * 题型（1真题还是0模拟题）
     */
    private int type;

    /**
     * 智能转人工次数统计
     */
    private Integer convertCount;
}
