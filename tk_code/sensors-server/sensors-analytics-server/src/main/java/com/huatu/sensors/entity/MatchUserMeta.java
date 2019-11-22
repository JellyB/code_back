package com.huatu.sensors.entity;


import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;

/**
 * Created by huangqingpeng on 2018/10/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "match_user_meta")
public class MatchUserMeta extends BaseEntity {
    /**
     * 模考大赛ID
     */
    private Integer matchId;

    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 地区ID
     */
    private Integer positionId;

    /**
     * 地区(职位)名称
     */
    private String positionName;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 报名时间
     */
    private Timestamp enrollTime;

    /**
     * 答题卡ID
     */
    private Long practiceId;

    /**
     * 答题卡创建时间
     */
    private Timestamp cardCreateTime;

    /**
     * 是否答题
     */
    private Integer isAnswer;

    /**
     * 答题卡交卷提交时间
     */
    private Timestamp submitTime;

    /**
     * 答题卡提交方式1、直接提交2、自动提交
     */
    private Integer submitType;

    /**
     * 分数
     */
    private Double score;
    //字符串类型
    @Transient
    private String scoreStr;


    /**
     * 全站排名
     */
    private Integer rank;

    /**
     * 地区排名
     */
    private Integer rankForPosition;


    /**
     * 最大值
     */
    @Transient
    private Double maxScore;
    @Transient
    private String maxScoreStr;

    /**
     * 平均值
     */
    @Transient
    private Double average;
    @Transient
    private String averageStr;

    /**
     * 全站排名人数
     */
    @Transient
    private Integer rankCount;

    /**
     * 地区排名人数
     */
    @Transient
    private Integer rankCountForPosition;
    /**
     * 科目ID
     */
    @Transient
    private Integer subjectId;
    /**
     * 标签ID
     */
    @Transient
    private Integer tagId;
    /**
     * 模考大赛名称
     */
    @Transient
    private String name;
    /**
     * 缓存redis空值标识（1标识空值2标识有值）
     */
    @Transient
    private String other;

    @Builder

    public MatchUserMeta(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Integer matchId, Integer userId, Integer positionId, String positionName, Integer schoolId, String schoolName, Timestamp enrollTime, Long practiceId, Timestamp cardCreateTime, Integer isAnswer, Timestamp submitTime, Integer submitType, Double score, Integer rank, Integer rankForPosition, Double maxScore, Double average, Integer rankCount, Integer rankCountForPosition, Integer subjectId, Integer tagId, String name,String scoreStr, String maxScoreStr, String averageStr) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.matchId = matchId;
        this.userId = userId;
        this.positionId = positionId;
        this.positionName = positionName;
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.enrollTime = enrollTime;
        this.practiceId = practiceId;
        this.cardCreateTime = cardCreateTime;
        this.isAnswer = isAnswer;
        this.submitTime = submitTime;
        this.submitType = submitType;
        this.score = score;
        this.rank = rank;
        this.rankForPosition = rankForPosition;
        this.maxScore = maxScore;
        this.average = average;
        this.rankCount = rankCount;
        this.rankCountForPosition = rankCountForPosition;
        this.subjectId = subjectId;
        this.tagId = tagId;
        this.name = name;
        this.scoreStr = scoreStr;
        this.averageStr = averageStr;
        this.maxScoreStr = maxScoreStr;
    }
}
