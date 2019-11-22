package com.huatu.ztk.backend.subject.dao;

import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.bean.SubjectStatus;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.lang3.StringUtils;
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
 * Created by ht on 2016/12/21.
 */
@Repository
public class SubjectDao {

    private static final Logger logger = LoggerFactory.getLogger(SubjectDao.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 科目列表
     * @return
     */
    public List<SubjectBean> findAll() {
        String sql = "SELECT * FROM v_new_subject ";
        return jdbcTemplate.query(sql,new SubjectMapper());
    }


    /**
     * 新增科目
     * @return
     */
    public void insert(SubjectBean SubjectBean) {
        logger.info("insert subject={}", JsonUtil.toJson(SubjectBean));
        String sql = "INSERT v_new_subject(name,catgory,create_by,status) VALUES (?,?,?,?)";
        Object[] params = {
                SubjectBean.getName(),
                SubjectBean.getCatgory(),
                SubjectBean.getCreateBy(),
                SubjectStatus.AVAILABLE
        };

        jdbcTemplate.update(sql, params);
    }

    /**
     * 更新
     * @param SubjectBean
     * @return
     */
    public void update(SubjectBean SubjectBean) {

        logger.info("update subject={}", JsonUtil.toJson(SubjectBean));
        String sql = "UPDATE v_new_subject set name=?,catgory=?,create_by=?,status=? WHERE id=?";
        Object[] params = {
                StringUtils.trimToEmpty(SubjectBean.getName()),
                SubjectBean.getCatgory(),
                SubjectBean.getCreateBy(),
                SubjectBean.getStatus(),
                SubjectBean.getId()
        };

        jdbcTemplate.update(sql, params);
    }


    /**
     * 删除
     * @return
     */
    public void delete(int id) {
        String sql = "UPDATE v_new_subject SET status=? WHERE id= ?";
        Object[] params = {
                SubjectStatus.DELETE,
                id
        };

        jdbcTemplate.update(sql, params);
    }

    class SubjectMapper implements RowMapper<SubjectBean> {

        @Override
        public SubjectBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            SubjectBean subjectBean = SubjectBean.builder()
                    .id(rs.getInt("id"))
                    .catgory(rs.getInt("catgory"))
                    .name(rs.getString("name"))
                    .createBy(rs.getLong("create_by"))
                    .createTime(rs.getTimestamp("create_time"))
                    .status(rs.getInt("status"))
                    .build();
            return subjectBean;
        }
    }
}

