package com.huatu.ztk.knowledge.cacheTask.dao.impl;

import com.huatu.ztk.knowledge.cacheTask.dao.QuestionPersistenceDao;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by junli on 2018/3/19.
 */
@Repository
public class QuestionPersistenceDaoImpl implements QuestionPersistenceDao {
    private static final Logger log = LoggerFactory.getLogger(QuestionPersistenceDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void save(String userId, String questionPointId, String questionId, QuestionPersistenceEnum.TableName tableName) {
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        QuestionPersistenceModel questionPersistenceModel = findById(userId, questionPointId, tableName);
        if (null != questionPersistenceModel) {
            //一旦出现了修改操作,则把数据设置成 可用
            questionPersistenceModel.setQuestionId(questionId);
            questionPersistenceModel.setQuestionPointId(questionPointId);
            questionPersistenceModel.setState(QuestionPersistenceEnum.DataState.IN_USING);
            questionPersistenceModel.setUpdateTime(new Date());
            updateData(questionPersistenceModel, tableName);
        } else {
            QuestionPersistenceModel model = new QuestionPersistenceModel(userId, questionPointId, questionId);
            insertData(model, tableName);
        }
    }

    @Override
    public List<String> getQuestionIdByUserIdAndPointId(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName) {
        QuestionPersistenceModel model = findById(userId, questionPointId, tableName);
        if (null == model || StringUtils.isEmpty(model.getQuestionId())) {
            return new ArrayList<>();
        }
        return Arrays.asList(model.getQuestionId().split(","));
    }

    @Override
    public void delete(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName) {
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        QuestionPersistenceModel questionPersistenceModel = findById(userId, questionPointId, tableName);
        if (null != questionPersistenceModel) {
            questionPersistenceModel.setState(QuestionPersistenceEnum.DataState.UN_USING);
            updateData(questionPersistenceModel, tableName);
        }
    }

    @Override
    public void deletePhysics(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" delete FROM ").append(tableName.getTableName())
                .append(" WHERE USER_ID = ").append(userId)
                .append(" AND QUESTION_POINT_ID = ").append(questionPointId);
        jdbcTemplate.update(sql.toString());
    }

    /**
     * 查询一条数据
     *
     * @param userId          用户ID
     * @param questionPointId 知识信息
     * @param tableName       表名称
     * @return
     */

    private QuestionPersistenceModel findById(String userId, String questionPointId, QuestionPersistenceEnum.TableName tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select user_id,question_id ")
                .append(" FROM ").append(tableName.getTableName())
                .append(" WHERE USER_ID = ? ")
                .append(" AND QUESTION_POINT_ID = ? ");
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql.toString(), new Object[]{userId, questionPointId});
        while (sqlRowSet.next()) {
            QuestionPersistenceModel questionPersistenceModel = new QuestionPersistenceModel();
            questionPersistenceModel.setUserId(sqlRowSet.getString("user_id"));
            questionPersistenceModel.setQuestionId(sqlRowSet.getString("question_id"));
            return questionPersistenceModel;
        }
        return null;
    }

    /**
     * 插入一条数据
     *
     * @param model     插入数据
     * @param tableName 表名称
     */
    private void insertData(QuestionPersistenceModel model, QuestionPersistenceEnum.TableName tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" INSERT INTO ")
                .append(tableName.getTableName())
                .append(" VALUES  (?, ?, ?, ?, ?) ");
        jdbcTemplate.update(sql.toString(),
                model.getUserId(),
                model.getQuestionPointId(),
                model.getQuestionId(),
                model.getUpdateTime(),
                model.getState());
    }

    /**
     * 修改一条数据
     *
     * @param model     待修改的数据
     * @param tableName 表名称
     */
    private void updateData(QuestionPersistenceModel model, QuestionPersistenceEnum.TableName tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" UPDATE ")
                .append(tableName.getTableName())
                .append(" SET QUESTION_ID = ?")
                .append(" , STATE = ?")
                .append(" , UPDATE_TIME = ?")
                .append(" WHERE USER_ID = ?")
                .append(" AND QUESTION_POINT_ID = ?");
        jdbcTemplate.update(sql.toString(),
                model.getQuestionId(),
                model.getState(),
                model.getUpdateTime(),
                model.getUserId(),
                model.getQuestionPointId()
        );
    }


    /**
     *
     * 根据用户ID查询其知识点
     * @param userId    用户ID
     * @param tableName 表名称
     * @return
     */
    @Override
    public List<QuestionPersistenceModel> findByUserId(String userId, QuestionPersistenceEnum.TableName tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select user_id,question_id,question_point_id,update_time ")
                .append(" FROM ").append(tableName.getTableName())
                .append(" WHERE USER_ID = ? ");

        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql.toString(), new Object[]{userId});
        List<QuestionPersistenceModel> collect = new ArrayList<>();
        while (sqlRowSet.next()) {
            QuestionPersistenceModel questionPersistenceModel = new QuestionPersistenceModel();
            questionPersistenceModel.setUserId(sqlRowSet.getString("user_id"));
            questionPersistenceModel.setQuestionId(sqlRowSet.getString("question_id"));
            questionPersistenceModel.setQuestionPointId(sqlRowSet.getString("question_point_id"));
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //加上时间
            try {
                Date date = sDateFormat.parse(sqlRowSet.getString("update_time").toString());
                questionPersistenceModel.setUpdateTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            collect.add(questionPersistenceModel);
        }
        return collect;
    }
}
