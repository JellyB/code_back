package com.huatu.ztk.search.dao;

import com.huatu.ztk.search.bean.KeywordSearchBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by renwenlong on 2016/9/8.
 */
@Repository
public class HotwordDao {
    private static final Logger logger = LoggerFactory.getLogger(HotwordDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根据科目查询热搜词
     *
     * @param catgory
     * @return
     */
    public List<String> query(int catgory) {
        //查询课程热搜词,热搜词以逗号分割
        String sql = "SELECT value FROM v_dict WHERE name = 'course_hot_words'";
        String value = "";
        final List<String> list = jdbcTemplate.query(sql, new HorWordsRowMapper());
        if (CollectionUtils.isNotEmpty(list)) {
            value = list.get(0);
        }
        logger.info("hot words:{}", list);
        return Arrays.stream(value.split(","))
                .filter(str -> StringUtils.isNoneBlank(str))
                .collect(Collectors.toList());
    }

    private class HorWordsRowMapper implements RowMapper<String>{
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
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("value");
        }
    }
}
