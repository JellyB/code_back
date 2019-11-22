package com.huatu.tiku.entity.duplicate;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by huangqp on 2018\6\12 0012.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "question_duplicate_relation")
public class QuestionDuplicate extends BaseEntity{
    /**
     * 主表id
     */
    private Long questionId;
    /**
     * 复用数据id
     */
    private Long duplicateId;
    /**
     * 复用类型（存储类型）（1客观选择类,2判断类,3主观类,4连线类,5填空类,6复合类）
     */
    private Integer duplicateType;
    @Builder
    public QuestionDuplicate(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Long questionId, Long duplicateId, Integer duplicateType) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.questionId = questionId;
        this.duplicateId = duplicateId;
        this.duplicateType = duplicateType;
    }
}

