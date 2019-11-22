package com.huatu.tiku.entity.common;

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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "question_type")
public class QuestionType extends BaseEntity {
    /**
     * 题型名称
     */
    private String name;
    /**
     * 业务类型（saveType）--1未分配1客观选择类2判断类3主观类4连线类5填空类6复合类
     */
    private Integer bizType;
    /**
     * 复用数据类型-1未分配1客观判断类
     */
    private Integer duplicateType;
    @Builder
    public QuestionType(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, String name, Integer bizType, Integer duplicateType) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.name = name;
        this.bizType = bizType;
        this.duplicateType = duplicateType;
    }
}

