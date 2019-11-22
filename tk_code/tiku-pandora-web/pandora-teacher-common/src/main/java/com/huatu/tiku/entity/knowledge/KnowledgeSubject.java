package com.huatu.tiku.entity.knowledge;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * 知识点科目多对多关联表
 * Created by huangqp on 2018\5\16 0016.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "knowledge_subject")
public class KnowledgeSubject extends BaseEntity {
    /**
     * 科目id
     */
    private Long subjectId;
    /**
     * 知识点id
     */
    private Long knowledgeId;

}
