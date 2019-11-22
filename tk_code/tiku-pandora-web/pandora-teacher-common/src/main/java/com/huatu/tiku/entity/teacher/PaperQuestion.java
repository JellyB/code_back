package com.huatu.tiku.entity.teacher;

import com.huatu.common.bean.BaseEntity;
import lombok.*;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;

/**
 * 试题绑定关系
 * Created by huangqp on 2018\6\23 0023.
 */
@NoArgsConstructor
@Data
@Table(name = "paper_base_question")
public class PaperQuestion extends BaseEntity {

    /**
     * 试卷id
     */
    private Long paperId;

    /**
     * 试卷类型（1实体试卷2活动试卷）
     */
    private Integer paperType;

    /**
     * 试题id
     */
    private Long questionId;

    /**
     * 题序
     */
    private Integer sort;

    /**
     * 每道题的分数
     */
    private Double score;

    /**
     * 模块ID
     */
    private Integer moduleId;


    @Builder
    public PaperQuestion(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long paperId, Integer paperType, Long questionId, Integer sort, Double score, Integer moduleId) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.paperId = paperId;
        this.paperType = paperType;
        this.questionId = questionId;
        this.sort = sort;
        this.score = score;
        this.moduleId = moduleId;
    }
}

