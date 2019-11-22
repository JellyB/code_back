package com.huatu.ztk.knowledge.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * Created by linkang on 17-4-18.
 */

//@Repository
public class SubjectDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int getCatgoryBySubjectId(int subject) {
        String sql = "SELECT * FROM v_new_subject WHERE id=" + subject;

        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);

        if (sqlRowSet.next()) {
            return sqlRowSet.getInt("catgory");
        }
        return -1;
    }
}
