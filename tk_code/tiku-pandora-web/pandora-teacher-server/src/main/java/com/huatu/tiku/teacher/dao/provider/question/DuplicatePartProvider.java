package com.huatu.tiku.teacher.dao.provider.question;


import com.huatu.tiku.enums.SubjectInfoEnum;
import com.huatu.tiku.teacher.controller.admin.util.ImportController;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/12/12
 * @描述
 */

@Slf4j
public class DuplicatePartProvider {

    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);


    public String buildObjective(String questionIds, int questionType) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT base.id as questionId,");
        stringBuffer.append(" base.question_type as questionType,");
        stringBuffer.append(" base.subject_id as subjectId,");
        stringBuffer.append(" objective.id as duplicateId,");
        stringBuffer.append(" objective.stem,objective.answer,objective.choices as choicesStr,");
        stringBuffer.append(" objective.analysis,objective.extend, s.name as subjectName,s.parent");
        stringBuffer.append(" FROM base_question base");
        stringBuffer.append(" INNER JOIN question_duplicate_relation relation ");
        stringBuffer.append(" ON base.id=relation.question_id AND base.status=1 and relation.STATUS=1 ");
        stringBuffer.append(" AND relation.duplicate_type=1");
        stringBuffer.append(" INNER JOIN question_duplicate_objective objective ON ");
        stringBuffer.append(" relation.duplicate_id=objective.id AND objective.`status`=1");
        stringBuffer.append(" LEFT join  `subject` s  ON s.id=base.subject_id and s.status=1 and s.`level`= ");
        stringBuffer.append(SubjectInfoEnum.SubjectLevel.LEVEL_TWO.getCode());
        stringBuffer.append(" WHERE base.id in (");
        stringBuffer.append(questionIds);
        stringBuffer.append(")");
        logger.info("getObjectiveInfo是：{}", stringBuffer.toString());
        return stringBuffer.toString();
    }


    public String buildSubjective(String questionIds, int questionType) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT base.id AS questionId,");
        stringBuffer.append(" base.question_type AS questionType,");
        stringBuffer.append(" base.subject_id AS subjectId,");
        stringBuffer.append(" relation.duplicate_id AS duplicateId,");
        stringBuffer.append(" subjective.stem,");
        stringBuffer.append(" subjective.answer_comment as answer,");
        stringBuffer.append(" '' as choices,");
        stringBuffer.append(" subjective.analyze_question as analysis,s.name as subjectName,s.parent,");
        stringBuffer.append(" subjective.extend ");
        stringBuffer.append(" FROM base_question base");
        stringBuffer.append(" left JOIN question_duplicate_relation relation ON base.id = relation.question_id ");
        stringBuffer.append(" AND base.`status` = 1 ");
        stringBuffer.append(" AND relation.`status` = 1 ");
        stringBuffer.append(" AND relation.duplicate_type = 2");
        stringBuffer.append(" INNER JOIN question_duplicate_subjective subjective ON relation.duplicate_id = subjective.id ");
        stringBuffer.append(" AND subjective.`status` = 1 ");
        stringBuffer.append(" LEFT join  `subject` s  ON s.id=base.subject_id and s.status=1 and s.`level`= ");
        stringBuffer.append(SubjectInfoEnum.SubjectLevel.LEVEL_TWO.getCode());
        stringBuffer.append(" WHERE base.id IN(");
        stringBuffer.append(questionIds);
        stringBuffer.append(")");
        logger.info("buildSubjective 是：{}", stringBuffer.toString());
        return stringBuffer.toString();
    }


}
