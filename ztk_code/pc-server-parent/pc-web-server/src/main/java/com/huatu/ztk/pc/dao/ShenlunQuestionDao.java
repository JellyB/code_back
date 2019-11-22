package com.huatu.ztk.pc.dao;

import com.huatu.ztk.pc.bean.ShenlunMultiQuestion;
import com.huatu.ztk.pc.bean.ShenlunSingleQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by renwenlong on 2016/9/27.
 */
@Repository
public class ShenlunQuestionDao {
    private static final Logger logger = LoggerFactory.getLogger(ShenlunQuestionDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根据id查询普通试题
     *
     * @param id
     * @return
     */
    public ShenlunSingleQuestion findSingleQuestionById(int id) {
        String sql = "select * from v_sub_question where pukey=?";
        Object[] param = {id};
        List<ShenlunSingleQuestion> singleQuestionList = jdbcTemplate.query(sql, param, new SingleQuestionMapper());
        ShenlunSingleQuestion singleQuestion = null;
        if (singleQuestionList != null && singleQuestionList.size() > 0) {
            singleQuestion = singleQuestionList.get(0);
        }
        if (singleQuestion == null) {
            logger.info("findSingleQuestion failed,Id={}", id);
        }
        return singleQuestion;
    }

    /**
     * 根据id查询复合试题
     *
     * @param id
     * @return
     */
    public ShenlunMultiQuestion findMultiQuestionById(int id) {
        String sql = "select * from v_multi_question where pukey=?";
        Object[] param = {id};
        ShenlunMultiQuestion multiQuestion = new ShenlunMultiQuestion();
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, param);
        if (rs.first()) {
            multiQuestion.setId(rs.getInt("pukey"));
            multiQuestion.setStem(rs.getString("stem"));
        }
        if (multiQuestion == null) {
            logger.info("findMultiQuestion failed,Id={}", id);
        }
        return multiQuestion;
    }


    class SingleQuestionMapper implements RowMapper<ShenlunSingleQuestion> {

        @Override
        public ShenlunSingleQuestion mapRow(ResultSet rs, int rowNum) throws SQLException {
            final ShenlunSingleQuestion singleQuestion = ShenlunSingleQuestion.builder()
                    .answer(rs.getString("answer_comment"))
                    .restrict(rs.getString("answer_require"))
                    .analysis(rs.getString("answer_think"))
                    .scorePoint(rs.getString("bestow_point_explain"))
                    .wordLimit(rs.getInt("input_word_num"))
                    .build();
            singleQuestion.setId(rs.getInt("pukey"));
            singleQuestion.setStem(rs.getString("stem"));
            singleQuestion.setType(rs.getInt("type_id"));
            return singleQuestion;
        }
    }

}
