package com.huatu.tiku.teacher.dao.provider.question;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

/**
 * Created by huangqingpeng on 2018/8/11.
 */
@Slf4j
public class ObjectiveDuplicatePartProvider {
    /**
     * 客观题信息
     * @param questionId 试题id
     * @return sql
     */
    public String findByQuestionId(@Param("questionId") long questionId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" question_duplicate_objective.stem as stem, ");
        sql.append(" question_duplicate_objective.choices as choices ,  ");
        sql.append(" question_duplicate_objective.judge_basis as judge_basis, ");
        sql.append(" question_duplicate_objective.answer as answer, ");
        sql.append(" question_duplicate_objective.analysis as analysis, ");
        sql.append(" question_duplicate_objective.extend as extend ");
        sql.append(" FROM ");
        sql.append(" question_duplicate_objective  ");
        sql.append(" INNER JOIN question_duplicate_relation ON question_duplicate_relation.duplicate_id = question_duplicate_objective.id ");
        sql.append(" AND question_duplicate_relation.`status` = 1 ");
        sql.append(" AND question_duplicate_objective.`status` = 1 ");
        sql.append(" AND question_duplicate_relation.question_id =  ").append(questionId);
        return sql.toString();
    }
}
