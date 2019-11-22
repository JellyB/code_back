package com.huatu.tiku.essay.entity.vo.report;

import com.huatu.tiku.essay.vo.resp.MockQuestionAnswerVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 适配行测报告
 * Created by huangqp on 2018\1\7 0007.
 */
@Builder
@AllArgsConstructor
@Data
public class EssayAnswerCardVO {
    private long id;//答题卡id
    private long userId;//用户id
    private int subject;//知识点类目
    private int catgory;//考试科目
    private double score;//预测分数
    private String scoreStr;// 字符串类型
    private double difficulty;//难度系数
    private String name;//答题卡名称
    private int rcount;//答题正确数量
    private int wcount;//答题错误数量
    private int ucount;//未做答题数量
    private int status;//答题卡状态 已完成，未做完
    private int type;//答题卡 类型
    private int terminal;//答题终端: pc,移动
    private int expendTime;//耗时
    private int speed;//平均答题速度
    private long createTime;//当作交卷时间使用
    private int remainingTime;//剩余时间


    //    private int unfinishedCount;//本次答题做到第几题
    private int lastIndex;//本次答题做到第几题
    private int[] corrects;//是否正确
    private String[] answers;//答题记录
    private int[] times;//每道题的耗时 单位是秒
    private List<Object> points;
    private int[] doubts; //疑问
    private Object paper;


    private CardUserMeta cardUserMeta;//用户做题统计
    private MatchCardUserMeta matchMeta;
    //申论特有字段
    /**
     * 问题列表
     */
    private List<MockQuestionAnswerVO> questionList;
    /**
     * 试卷名称
     */
    private String paperName;
    private int questionCount;
    private double totalScore;

    public EssayAnswerCardVO(MockScoreReportVO source) {
        this.setId(source.getAnswerCardId());//答题卡id
        this.setPaperName(source.getName());//试卷名称
        this.setName(source.getName());
        this.setCreateTime(source.getSubmitTime());
        this.setQuestionCount(source.getQuestionList().size());//总题目数
        this.setTotalScore(source.getScore());//满分
        CardUserMeta cardUserMeta = new CardUserMeta();
        cardUserMeta.setTotal(source.getTotalCount());//总参加人数
        cardUserMeta.setRank(source.getTotalRank());//总排名
        cardUserMeta.setMax(source.getMaxScore());//最高分
        //地区报名人数
        this.setCardUserMeta(cardUserMeta);
        MatchCardUserMeta matchCardUserMeta = new MatchCardUserMeta();
        matchCardUserMeta.setPositionCount(source.getAreaEnrollCount());//地区总人数
        matchCardUserMeta.setPositionRank(source.getAreaRank());//地区排名
        matchCardUserMeta.setScoreLine(source.getLine());//折线数据
        matchCardUserMeta.setPositionId((int) source.getAreaId());//地区id
        this.setMatchMeta(matchCardUserMeta);
        this.setScore(source.getExamScore());//学员得分
        this.setExpendTime(source.getSpendTime());//学员用时
        this.setQuestionList(source.getQuestionList());//题目列表
        this.setUcount(source.getUnfinishedCount());//未完成题目数

    }

}
