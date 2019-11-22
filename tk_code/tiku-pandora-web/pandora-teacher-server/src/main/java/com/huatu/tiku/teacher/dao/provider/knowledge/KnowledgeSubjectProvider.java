package com.huatu.tiku.teacher.dao.provider.knowledge;

import com.huatu.tiku.baseEnum.BaseStatusEnum;
import com.huatu.tiku.entity.subject.Subject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/2/13
 */
public class KnowledgeSubjectProvider {

    public static String selectKnowledgeBySubjectId(List<Subject> subjectList) {
        final SQL sql = new SQL();
        //构建In sql 语句
        Function<List<Subject>, String> buildSubjectIdInSql = (subjectInfoList) -> {
            final StringBuilder stringBuilder = new StringBuilder(256);
            stringBuilder.append(" 1 = 1");
            String whereSql = subjectInfoList.stream()
                    .map(subject -> "(`subject`.id = " + subject.getId() + " AND " + "`subject`.level = " + subject.getLevel() + ")")
                    .collect(Collectors.joining(" OR "));
            return stringBuilder.append(" AND (").append(whereSql).append(")").toString();
        };

        sql.SELECT("knowledge.id," +
                "CONCAT( `subject`.`name`, '-', knowledge.`name` ) AS `name`," +
                "knowledge.`level`,knowledge.`level`,knowledge.is_leaf,knowledge.parent_id")
                .FROM("knowledge")
                .INNER_JOIN("knowledge_subject ON knowledge.id = knowledge_subject.knowledge_id AND knowledge_subject.`status` = 1")
                .INNER_JOIN("`subject` ON knowledge_subject.subject_id = `subject`.id  AND `subject`.`status` = 1 ")
                .WHERE("knowledge.`status` = 1")
                .AND()
                .WHERE(buildSubjectIdInSql.apply(subjectList));
        return sql.toString();
    }

    /**
     * 根据试题ID
     *
     * @param questionIds
     * @return
     */
    public String selectKnowledgeNameByQuestionId(List<Long> questionIds) {
        String questionsStr = "";
        if (CollectionUtils.isNotEmpty(questionIds)) {
            questionsStr = questionIds.stream()
                    .map(id -> String.valueOf(id))
                    .collect(Collectors.joining(","));
        }
        //TODO
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT knowledge.name FROM base_question_knowledge   question ");
        sql.append(" left JOIN knowledge knowledge on question.knowledge_id = knowledge.id  ");
        sql.append(" where question.status=");
        sql.append(BaseStatusEnum.NORMAL.getCode());
        sql.append(" and knowledge.`status`=");
        sql.append(BaseStatusEnum.NORMAL.getCode());
        sql.append(" and question.question_id  in (");
        sql.append(questionsStr);
        sql.append(")");
        System.out.println("sql是:{}" + sql.toString());
        return sql.toString();
    }


    public String updateKnowledgeIdByQuestionId(Long rightKnowledgeId, List<Long> questionIds) {
        String questionsStr = "";
        if (CollectionUtils.isNotEmpty(questionIds)) {
            questionsStr = questionIds.stream()
                    .map(id -> String.valueOf(id))
                    .collect(Collectors.joining(","));
        }

        StringBuffer sql = new StringBuffer();
        sql.append(" UPDATE base_question_knowledge b  set  b.knowledge_id = ");
        sql.append(rightKnowledgeId);
        sql.append(" where b.question_id  in(");
        sql.append(questionsStr);
        sql.append(")");
        System.out.println("sql是:{}" + sql.toString());
        return sql.toString();
    }
}
