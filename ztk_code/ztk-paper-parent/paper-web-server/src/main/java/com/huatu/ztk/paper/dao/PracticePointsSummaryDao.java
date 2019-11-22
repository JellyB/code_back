package com.huatu.ztk.paper.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.PracticePointsSummary;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * Created by shaojieyue
 * Created time 2016-07-26 09:22
 */

@Repository
public class PracticePointsSummaryDao{
    private static final Logger logger = LoggerFactory.getLogger(PracticePointsSummaryDao.class);

    //知识点汇总表前缀
    public static final String TABLE_PREFIX = "v_practice_points_summary_";
    //分表个数
    public static final int TABLE_PARTITION_COUNT= 8;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 插入数据
    * @param practicePointsSummary
     */
    public void insert(PracticePointsSummary practicePointsSummary){
        /**
         * 如果存在多次提交
         */
        PracticePointsSummary pointsSummary = findByPracticeId(practicePointsSummary.getPracticeId());
        if (null != pointsSummary) {
            //存在数据
            String sql = "update " + getTable(practicePointsSummary.getPracticeId()) + " SET points = ? where practice_id = ?";
            Object[] params = {
                    JsonUtil.toJson(practicePointsSummary.getPoints()),
                    practicePointsSummary.getPracticeId()

            };
            jdbcTemplate.update(sql, params);

        } else {
            final String table = getTable(practicePointsSummary.getPracticeId());

            String sql = "insert into " + table + "(practice_id,points) values (?,?)";
            //logger.info("insert data to table:{}, data={}",table,JsonUtil.toJson(practicePointsSummary));
            Object[] params = {
                    practicePointsSummary.getPracticeId(),
                    JsonUtil.toJson(practicePointsSummary.getPoints())
            };
            jdbcTemplate.update(sql, params);
        }


    }


    /**
     * 插入数据
     * @param practicePointsSummary
     */
//    public void insert(PracticePointsSummary practicePointsSummary) {
//        final String table = getTable(practicePointsSummary.getPracticeId());
//
//        String insertSql = "INSERT INTO " + table + " (practice_id,points) SELECT ?,? FROM dual WHERE NOT EXISTS ( SELECT 1 FROM " + table + " WHERE practice_id = ? )";
//        Object[] params = {
//                practicePointsSummary.getPracticeId(),
//                JsonUtil.toJson(practicePointsSummary.getPoints())
//        };
//        int update = jdbcTemplate.update(insertSql, params);
//        if(update != 1){
//            //存在数据
//            String updateSql = "update " + getTable(practicePointsSummary.getPracticeId()) + " SET points = ? where practice_id = ?";
//            Object[] updateParams = {
//                    JsonUtil.toJson(practicePointsSummary.getPoints()),
//                    practicePointsSummary.getPracticeId()
//            };
//            jdbcTemplate.update(updateSql, updateParams);
//        }
//
//
//    }

    public PracticePointsSummary findByPracticeId(long practiceId){
        String sql = "select * from "+getTable(practiceId)+" where practice_Id=?";
        Object[] params = {
                practiceId
        };
        final List<PracticePointsSummary> summaries = jdbcTemplate.query(sql, params, new SummaryRowMapper());
        PracticePointsSummary practicePointsSummary = null;
        if (CollectionUtils.isNotEmpty(summaries)) {
            practicePointsSummary = summaries.get(0);
        }
        return practicePointsSummary;
    }

    class SummaryRowMapper implements RowMapper<PracticePointsSummary> {

        /**
         * Implementations must implement this method to map each row of data
         * in the ResultSet. This method should not call {@code next()} on
         * the ResultSet; it is only supposed to map values of the current row.
         *
         * @param rs     the ResultSet to map (pre-initialized for the current row)
         * @param rowNum the number of the current row
         * @return the result object for the current row
         * @throws SQLException if a SQLException is encountered getting
         *                      column values (that is, there's no need to catch SQLException)
         */
        @Override
        public PracticePointsSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            long practiceId = rs.getLong("practice_id");
            String points = rs.getString("points");
            final List<QuestionPointTree> questionPointTrees = JsonUtil.toList(points, QuestionPointTree.class);
            return PracticePointsSummary.builder()
                    .practiceId(practiceId)
                    .points(questionPointTrees)
                    .build();
        }
    }

    /**
     * 获取练习知识点汇总表前缀
     * @param practiceId 练习id
     * @return
     */
    private String getTable(long practiceId){
        return TABLE_PREFIX+(practiceId%TABLE_PARTITION_COUNT);
    }
}
