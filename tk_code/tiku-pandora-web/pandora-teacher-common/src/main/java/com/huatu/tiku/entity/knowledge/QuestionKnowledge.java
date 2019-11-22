package com.huatu.tiku.entity.knowledge;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "base_question_knowledge")
public class QuestionKnowledge extends BaseEntity {
    /**
     * 试题id
     */
    private Long questionId;
    /**
     * 知识点id
     */
    private Long knowledgeId;
}
