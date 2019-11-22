package com.huatu.ztk.user.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

/**
 * Created by linkang on 17-6-6.
 */

@Repository
public class SubjectDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public String findSubjectNameById(int subjectId) {
        String sql = "SELECT name FROM v_new_subject WHERE id=?";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, new Object[]{subjectId});

        if (sqlRowSet.next()) {
            return sqlRowSet.getString("name");
        }

        return "";
    }
}
