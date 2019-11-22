package com.huatu.tiku.teacher.service.knowledge;

import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.service.BaseService;

import java.util.HashMap;
import java.util.List;

/**
 * 知识点 - 科目 - 关系
 * Created by huangqp on 2018\6\12 0012.
 */
public interface KnowledgeSubjectService extends BaseService<KnowledgeSubject> {

    /**
     * 选取复合科目的知识点
     *
     * @param knowledgeIds 知识点集合
     * @param subject      筛选条件
     * @return 复合的id
     */
    List<Long> choicesKnowledgeBySubject(List<Long> knowledgeIds, Long subject);

    /**
     * 物理删除关联关系
     *
     * @param knowledgeId
     */
    void deleteByKnowledge(long knowledgeId);

    /**
     * 获取关联的知识点信息
     *
     * @param subjectId 科目ID
     * @return 知识点信息
     */
    List<Knowledge> getFirstLevelKnowledgeBySubjectId(Long subjectId);

    /**
     * 获取所有知识点信息
     *
     * @param subjectId 科目ID
     * @return 兄弟节点包含的科目信息
     */
    List<HashMap<String, Object>> getAllFriendKnowledgeBySubjectId(Long subjectId);

    /**
     * 编辑关联关系
     * 会物理删除原始数据
     */
    void editRelation(final Long subjectId, List<Long> knowledgeIdList);
}
