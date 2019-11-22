package com.huatu.tiku.teacher.service.knowledge;

import com.huatu.tiku.entity.knowledge.QuestionKnowledge;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * 知识点-题目-关系
 * Created by huangqp on 2018\6\12 0012.
 */
public interface QuestionKnowledgeService extends BaseService<QuestionKnowledge> {
    /**
     * 批量添加 - 试题知识点关联表
     *
     * @param knowledgeIds
     * @param questionId
     */
    void insertQuestionKnowledgeInfo(List<Long> knowledgeIds, Long questionId);

    /**
     * 删除题目知识点绑定关系-物理删除
     * @param questionId
     * @return
     */
    int deleteByQuestionId(long questionId);



}

