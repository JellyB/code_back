package com.huatu.tiku.essay.vo.resp.courseExercises;

import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述 课后作业报告
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseExercisesCommonReportVo {

    //答题卡id
    private String answerId;

    //批改时间
    private String correctDate;

    //交卷时间
    private String submitTime;

    //总分
    private double score;

    //学员得分
    private double examScore;

    //我的排名
    private long totalRank;

    //班级最高分
    private double maxScore;

    //班级平均分
    private double avgScore;

    //平均用时
    private double avgSpendTime;

    //批改方式(智能批改)
    private int correctMode;

    // 老师评价（套卷的综合阅卷）
    private List<RemarkVo> remarkList;

    // 名师之声
    private int audioId;

    //名师之声token
    private String audioToken;

    //学员是否评价批注 0 未评价 1 评价
    private int feedBackStatus;

    //学员报告评价的星级
    private int feedBackStar;

    // 学员报告评价内容
    private String feedBackContent;

    //优秀成绩排名信息
    private List<UserScoreRankVo> userScoreRankList;

    // 当前作答批改次数(第一次 or 第二次)
    private Integer correctNum;

    /**
     * 另外一张答题卡的id
     */
    private Long otherAnswerCardId;

    /**
     * 耗时
     */
    private int spendTime;

    /**
     * 报告名称
     */
    private String reportName;

    /**
     * 地区名称(客户端需要)
     */
    private String areaName;

    /**
     * 试卷名称
     */
    private String paperName;

    /**
     * 另一张答题卡的状态
     */
    private Integer otherAnswerBizStatus;


}
