package com.huatu.tiku.banckend.dao.provider;

import com.huatu.tiku.entity.AdviceBean;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * @author zhengyi
 * @date 2018/9/13 3:24 PM
 **/
public class AdviceSearchProvider {

    public String getList(AdviceBean questionBean) {
        Supplier<String> sqlSupplier = () -> {
            StringBuilder sqlCondition = new StringBuilder();
            sqlCondition.append(" where vq.status=1 ");
            sqlCondition
                    .append(" and vq.biz_status =").append(questionBean.getBizStatus());
            if (questionBean.getQuestionType() != -1) {
                sqlCondition
                        .append(" and vq.question_type =").append(questionBean.getQuestionType());
            }
            if (questionBean.getQuestionId() != -1) {
                sqlCondition
                        .append(" and vq.question_id =").append(questionBean.getQuestionId());
            }
            if (questionBean.getSubject() != -1) {
                sqlCondition
                        .append(" and subject_id =").append(questionBean.getSubject());
            }
            if (!"-1".equals(questionBean.getQuestionArea())) {
                sqlCondition
                        .append(" and question_area in (").append(questionBean.getQuestionArea()).append(") ");
            }
            if (StringUtils.isNotEmpty(questionBean.getStartTime())) {
                sqlCondition
                        .append(" and vq.gmt_create >").append("'").append(questionBean.getStartTime()).append("'");
            }
            if (StringUtils.isNotEmpty(questionBean.getEndTime())) {
                sqlCondition
                        .append(" and vq.gmt_create <").append("'").append(questionBean.getEndTime()).append("'");
            }
            if (!"".equals(questionBean.getKnowledgeId())) {
                sqlCondition
                        .append(" and bqk.knowledge_id in (").append(questionBean.getKnowledgeId()).append(")");
            }
            sqlCondition
                    .append(" group by vq.question_id order by vq.gmt_create ").append(questionBean.getOrderby());
            return sqlCondition.toString();
        };
        return getSearch(sqlSupplier);
    }

    public String getSearch(Supplier<String> sqlCondition) {
        String sqlHead = "select distinct\n" +
                "  vq.question_id   as questionId,\n" +
                "  bqk.knowledge_id as knowledgeId\n" +
                "from v_question_correction_log vq inner join base_question bq on vq.question_id = bq.id\n" +
                "  left join base_question_knowledge bqk on vq.question_id = bqk.question_id";
        StringBuffer sql = new StringBuffer(sqlHead);
        sql.append(sqlCondition.get());
        return String.valueOf(sql);
    }

    public String getQuestionSourceInfo(String questionIds) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT ");
        sql.append(" source,question_id as questionId");
        sql.append(" FROM ");
        sql.append(" view_question_source ");
        sql.append(" WHERE ");
        sql.append(" question_id in (").append(questionIds).append(")");
        return sql.toString();
    }

}