package com.huatu.ztk.paper.bo;

import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小模考报告返回值
 * Created by huangqingpeng on 2019/2/19.
 */
@Data
@NoArgsConstructor
public class SmallEstimateReportBo extends SmallEstimateSimpleReportBo{

    /**
     * 练习类型(练习类型+模块日期)
     */
    private String typeInfo;
    /**
     * 交卷时间
     */
    private long submitTime;
    /**
     * 答对题数
     */
    private int rNum;
    /**
     * 最高答对题数
     */
    private int rNumMax;
    /**
     * 平均答对题数
     */
    private int rNumAverage;
    /**
     * 未做完题量
     */
    private int uNum;
    /**
     * 做错题量
     */
    private int wNum;
    /**
     * 交卷时间排序
     */
    private int submitSort;
    /**
     * 分数
     */
    private double score;
    /**
     * 分数排名
     */
    private int rank;
    /**
     * 参加人数（包含还未交卷的人数）
     */
    private int joinCount;
    /**
     * 报告更新时间
     */
    private long reportTime;
    /**
     * 总耗时
     */
    private int expendTime;
    /**
     * 剩余时间（秒）
     */
    private int remainTime;
    /**
     * 模块信息
     */
    private List<Module> modules;
    /**
     * 试题ID
     */
    private List<Integer> questions;
    /**
     * 试题答案正确与否（0未做1正确2错误）
     */
    private int[] corrects;
    /**
     * 答案（默认‘0’）
     */
    private String[] answers;
    /**
     * 是否有疑问（0无1有）
     */
    private int[] doubts;
    /**
     * 单个试题耗时
     */
    private int[] times;
    /**
     * 知识点情况（只涉及三级知识点)
     */
    private List<QuestionPointTree> questionPointTrees;

    @Builder
    public SmallEstimateReportBo(long practiceId, String idStr, String name, int qCount, int submitCount, int beatRate, String typeInfo, long submitTime, int rNum, int rNumMax, int rNumAverage, int uNum, int wNum, int submitSort, double score, int rank, int joinCount, long reportTime, int expendTime, int remainTime, List<Module> modules, List<Integer> questions, int[] corrects, String[] answers, int[] doubts, int[] times, List<QuestionPointTree> questionPointTrees) {
        super(practiceId, idStr, name, qCount, submitCount, beatRate);
        this.typeInfo = typeInfo;
        this.submitTime = submitTime;
        this.rNum = rNum;
        this.rNumMax = rNumMax;
        this.rNumAverage = rNumAverage;
        this.uNum = uNum;
        this.wNum = wNum;
        this.submitSort = submitSort;
        this.score = score;
        this.rank = rank;
        this.joinCount = joinCount;
        this.reportTime = reportTime;
        this.expendTime = expendTime;
        this.remainTime = remainTime;
        this.modules = modules;
        this.questions = questions;
        this.corrects = corrects;
        this.answers = answers;
        this.doubts = doubts;
        this.times = times;
        this.questionPointTrees = questionPointTrees;
    }
}
