package com.huatu.tiku.entity.teacher;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * 组卷 - 试卷-试题 关联关系
 * Created by lijun on 2018/8/16
 */
@NoArgsConstructor
@Data
@Table(name = "paper_assembly_question")
public class PaperAssemblyQuestion extends BaseEntity {

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * 排列序号
     */
    private Integer sort;

    @Builder
    public PaperAssemblyQuestion(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long paperId, Long questionId, Integer sort) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.paperId = paperId;
        this.questionId = questionId;
        this.sort = sort;
    }
}
