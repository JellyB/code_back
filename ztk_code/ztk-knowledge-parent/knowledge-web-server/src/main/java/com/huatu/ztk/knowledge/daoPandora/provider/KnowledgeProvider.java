package com.huatu.ztk.knowledge.daoPandora.provider;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by lijun on 2018/8/22
 */
@Slf4j
public class KnowledgeProvider {

    /**
     * 获取某个科目下的一级知识点信息
     */
    public String getFirstLevelBySubjectId(int subjectId) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT ");
        sql.append(" knowledge.id,knowledge.`name`,knowledge.parent_id AS 'parentId',")
                .append("knowledge.`level`,knowledge.is_leaf AS 'isLeaf',")
                .append("knowledge.`status`,knowledge.`biz_status` AS 'bizStatus'");
        sql.append(" FROM ");
        sql.append(" knowledge LEFT JOIN knowledge_subject ON knowledge.id = knowledge_subject.knowledge_id AND knowledge_subject.`status` = 1 ");
        sql.append(" WHERE ");
        sql.append(" knowledge_subject.subject_id = ").append(subjectId)
                .append(" AND knowledge.`level` = 1 ")
                .append(" ORDER BY knowledge.sort_num,knowledge.id");
        log.info("getFirstLevelBySubjectId sql = {}", sql.toString());
        return sql.toString();
    }

    /**
     * 查询一个知识点详情 - 附带试题数量
     */
    public String getKnowledgeInfoById(int knowledgeId) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT ");
        sql.append(" knowledge.id,knowledge.`name`,knowledge.parent_id AS `parentId`,")
                .append("knowledge.`level`,knowledge.is_leaf AS `isLeaf`,")
                .append("knowledge.`status`,knowledge.`biz_status` AS `bizStatus`,")
                .append("COUNT(1) AS 'questionNum'");
        sql.append(" FROM ");
        sql.append(" knowledge LEFT JOIN base_question_knowledge bqk ON knowledge.id = bqk.knowledge_id AND bqk.`status` = 1");
        sql.append(" WHERE ");
        sql.append(" knowledge.id = ").append(knowledgeId);
        log.info("getKnowledgeInfoById sql = {}", sql.toString());
        return sql.toString();
    }

    /**
     * 查询某个科目的用户知识点ID
     */
    public String getKnowledgeBySubjectId(String knowledgeIds, int subjectId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT k.id FROM knowledge k LEFT JOIN knowledge_subject ks on k.id=ks.knowledge_id");
        sql.append("  and k.`status`=1 and ks.status=1");
        sql.append("  WHERE  ks.subject_id=");
        sql.append(subjectId);
        sql.append(" and ks.knowledge_id in( ");
        sql.append(knowledgeIds);
        sql.append(")");
        log.info("结果是:{}", sql.toString());
        return sql.toString();
    }
}
