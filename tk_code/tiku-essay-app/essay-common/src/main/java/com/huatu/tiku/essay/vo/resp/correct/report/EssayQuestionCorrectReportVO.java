package com.huatu.tiku.essay.vo.resp.correct.report;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/11
 * @描述 单题人工批改报告
 */
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class EssayQuestionCorrectReportVO implements Serializable {

    //单题试卷名称1
    private String paperName;
    //批改时间1
    private String correctDate;
    //总分1
    private double score;
    //学员得分1
    private double examScore;

    //全站排名1
    private long totalRank;
    //全站考试总人数1
    private long totalCount;
    //全站平均分1
    private double avgScore;
    //总用时1
    private int spendTime;
    /**
     * 老师评价（本题阅卷评价～套卷的综合阅卷）
     */
    private List<RemarkVo> remarkList;

    private long questionBaseId;

    private long questionDetailId;

    private Double maxScore;
    /**
     * 名师之声音频Id
     */
    private int audioId;

    /**
     * 百家云token
     */
    private String token;

    /**
     * 学员是否评价此次批注 0 未评价 1 评价
     */
    private int feedBackStatus;

    /**
     * 百家云名师之声音频token
     */
    private String audioToken;

    /**
     * 批改方式 1智能批改 2人工批改
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

}
