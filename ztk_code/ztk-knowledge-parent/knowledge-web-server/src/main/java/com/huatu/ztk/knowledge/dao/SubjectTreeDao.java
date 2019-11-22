package com.huatu.ztk.knowledge.dao;

import com.huatu.ztk.knowledge.bean.SubjectTree;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 科目树dao
 * Created by linkang on 17-5-15.
 */

//@Repository
public class SubjectTreeDao {


    @Autowired
    private JdbcTemplate jdbcTemplate;


    public SubjectTree findById(int sid) {
        String sql = "SELECT * FROM v_new_subject_tree WHERE status =1 and id=?";
        List<SubjectTree> list = jdbcTemplate.query(sql, new Object[]{sid}, new SubjectTreeMapper());
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    public List<SubjectTree> findChildrens(int sid) {
        String sql = "SELECT * FROM v_new_subject_tree WHERE status =1 and  parent=?";
        List<SubjectTree> list = jdbcTemplate.query(sql, new Object[]{sid}, new SubjectTreeMapper());
        return list;
    }



    class SubjectTreeMapper implements RowMapper<SubjectTree> {

        @Override
        public SubjectTree mapRow(ResultSet rs, int rowNum) throws SQLException {
            SubjectTree newBean = SubjectTree.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .status(rs.getInt("status"))
                    .parent(rs.getInt("parent"))
                    .build();
            return newBean;
        }
    }
}
