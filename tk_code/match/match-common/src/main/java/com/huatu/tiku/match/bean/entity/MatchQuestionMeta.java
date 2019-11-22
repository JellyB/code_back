package com.huatu.tiku.match.bean.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by huangqingpeng on 2018/10/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "match_question_meta")
public class MatchQuestionMeta extends BaseEntity {
    /**
     * 模考大赛ID
     */
    private Integer matchId;
    /**
     * 用户ID
     */
    private Integer questionId;
    /**
     * 试题详情
     */
    private String detail;

    @Builder
    public MatchQuestionMeta(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Integer matchId, Integer questionId, String detail) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.matchId = matchId;
        this.questionId = questionId;
        this.detail = detail;
    }
}
