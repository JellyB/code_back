package com.huatu.tiku.entity.subject;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by huangqp on 2018\6\15 0015.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subject_question_type")
public class SubjectQuestionType extends BaseEntity {
    /**
     * 试题类型
     */
    private Long questionType;
    /**
     * 科目id
     */
    private Long subjectId;
    @Builder
    public SubjectQuestionType(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long questionType, Long subjectId) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.questionType = questionType;
        this.subjectId = subjectId;
    }
}

