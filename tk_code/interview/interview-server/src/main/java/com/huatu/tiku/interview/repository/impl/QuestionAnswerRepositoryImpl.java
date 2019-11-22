package com.huatu.tiku.interview.repository.impl;


import com.huatu.tiku.interview.constant.WXStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by junli on 2018/4/13.
 */
public class QuestionAnswerRepositoryImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findDataWithUserNameByQuestionId(List<Long> questionIdList) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT qa.id,qa.content,")
                .append("u.name AS 'userName',")
                .append("qa.question_id AS 'questionId'")
                .append(" FROM t_question_answer qa")
                .append(" LEFT JOIN t_user u on qa.open_id = u.open_id AND qa.status = ").append(WXStatusEnum.Status.NORMAL.getStatus())
                .append(" WHERE qa.status = ").append(WXStatusEnum.Status.NORMAL.getStatus())
                .append(" AND qa.question_id in (");
        for (int index = 0; index < questionIdList.size() - 1; index++) {
            sql.append(" '").append(questionIdList.get(index)).append("',");
        }
        sql.append("'").append(questionIdList.get(questionIdList.size() - 1)).append("')");
        sql.append(" ORDER BY qa.id DESC");

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql.toString());
        return mapList;
    }

    public List<Map<String,Object>> findDataWithUserNameByPushId(Long pushId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT qa.id,qa.content,qa.push_id,")
                .append("u.name AS 'userName',")
                .append("qa.question_id AS 'questionId'")
                .append(" FROM t_question_answer qa")
                .append(" LEFT JOIN t_user u on qa.open_id = u.open_id AND qa.status = ").append(WXStatusEnum.Status.NORMAL.getStatus())
                .append(" WHERE qa.status = ").append(WXStatusEnum.Status.NORMAL.getStatus())
                .append(" AND qa.push_id = ").append(pushId);
        sql.append(" ORDER BY qa.id DESC");

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql.toString());
        return mapList;
    }
}
