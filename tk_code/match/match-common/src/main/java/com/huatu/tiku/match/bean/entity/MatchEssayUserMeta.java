//package com.huatu.tiku.match.bean.entity;
//
//import com.huatu.common.bean.BaseEntity;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.Table;
//import java.sql.Timestamp;
//
///**
// * Created by huangqingpeng on 2018/10/16.
// */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "match_user_meta_essay")
//public class MatchEssayUserMeta extends BaseEntity {
//    /**
//     * 模考大赛ID
//     */
//    private Long essayPaperId;
//
//    /**
//     * tag id
//     */
//    private Integer tagId;
//    /**
//     * 用户ID
//     */
//    private Integer userId;
//    /**
//     * 地区ID
//     */
//    private Integer positionId;
//    /**
//     * 地区(职位)名称
//     */
//    private String positionName;
//    /**
//     * 报名时间
//     */
//    private Timestamp enrollTime;
//    /**
//     * 答题卡ID
//     */
//    private Long practiceId;
//    /**
//     * 答题卡创建时间
//     */
//    private Timestamp cardCreateTime;
//    /**
//     * 是否答题
//     */
//    private Integer isAnswer;
//    /**
//     * 答题卡交卷提交时间
//     */
//    private Timestamp submitTime;
//    /**
//     * 答题卡提交方式1、直接提交2、自动提交
//     */
//    private Integer submitType;
//    /**
//     * 分数
//     */
//    private Double score;
//    @Builder
//    public MatchEssayUserMeta(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long essayPaperId, Integer tagId, Integer userId, Integer positionId, String positionName, Timestamp enrollTime, Long practiceId, Timestamp cardCreateTime, Integer isAnswer, Timestamp submitTime, Integer submitType, Double score) {
//        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
//        this.essayPaperId = essayPaperId;
//        this.tagId = tagId;
//        this.userId = userId;
//        this.positionId = positionId;
//        this.positionName = positionName;
//        this.enrollTime = enrollTime;
//        this.practiceId = practiceId;
//        this.cardCreateTime = cardCreateTime;
//        this.isAnswer = isAnswer;
//        this.submitTime = submitTime;
//        this.submitType = submitType;
//        this.score = score;
//    }
//}
