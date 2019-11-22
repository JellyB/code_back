package com.huatu.tiku.teacher.dao.provider.question;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

/**
 * Created by x6 on 2018/8/7.
 */
@Slf4j
public class SubjectiveDuplicatePartProvider {

    /**
     * 主观题信息
     * @param id 试题id
     * @return sql
     */
    public String findByQuestionId(@Param("id") long id){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" question_duplicate_relation.question_id AS questionId, ");
        sql.append(" question_duplicate_subjective.analyze_question as referAnalysis, ");
        sql.append(" question_duplicate_subjective.answer_comment as answerComment, ");
        sql.append(" question_duplicate_subjective.answer_request as answerRequire, ");
        sql.append(" question_duplicate_subjective.bestow_point_explain as examPoint, ");
        sql.append(" question_duplicate_subjective.stem as stem, ");
        sql.append(" question_duplicate_subjective.extend as extend, ");
        sql.append(" question_duplicate_subjective.train_thought as train_thought ");
        sql.append(" FROM ");
        sql.append(" question_duplicate_relation  ");
        sql.append(" LEFT JOIN question_duplicate_subjective  ON question_duplicate_relation.duplicate_id = question_duplicate_subjective.id ");
        sql.append(" AND question_duplicate_subjective.`status` = 1 ");
        sql.append(" where  question_duplicate_relation.`status` = 1 ");
        sql.append(" AND question_duplicate_relation.question_id =  ").append(id);
        return sql.toString();
    }

}
