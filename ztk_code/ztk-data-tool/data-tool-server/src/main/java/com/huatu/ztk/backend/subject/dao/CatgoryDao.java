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
 * Created by linkang on 3/7/17.
 */
@Repository
public class CatgoryDao {

    private static final Logger logger = LoggerFactory.getLogger(SubjectDao.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 考试类型列表
     * @return
     */
    public List<SubjectBean> findAll() {
        String sql = "SELECT * FROM v_new_catgory";
        return jdbcTemplate.query(sql,new CatgoryMapper());
    }


    /**
     * 新增考试类型
     * @return
     */
    public void insert(SubjectBean subjectBean) {
        logger.info("insert catgory={}", JsonUtil.toJson(subjectBean));
        String sql = "INSERT v_new_catgory(name,create_by,status) VALUES (?,?,?)";
        Object[] params = {
                subjectBean.getName(),
                subjectBean.getCreateBy(),
                SubjectStatus.AVAILABLE
        };

        jdbcTemplate.update(sql, params);
    }

    /**
     * 更新
     * @param subjectBean
     * @return
     */
    public void update(SubjectBean subjectBean) {

        logger.info("update catgory={}", JsonUtil.toJson(subjectBean));
        String sql = "UPDATE v_new_catgory set name=?,create_by=?,status=? WHERE id=?";
        Object[] params = {
                StringUtils.trimToEmpty(subjectBean.getName()),
                subjectBean.getCreateBy(),
                subjectBean.getStatus(),
                subjectBean.getId(),
        };

        jdbcTemplate.update(sql, params);
    }


    /**
     * 删除
     * @return
     */
    public void delete(int id) {
        String sql = "UPDATE v_new_catgory SET status=? WHERE id= ?";
        Object[] params = {
                SubjectStatus.DELETE,
                id
        };

        jdbcTemplate.update(sql, params);
    }

    class CatgoryMapper implements RowMapper<SubjectBean> {

        @Override
        public SubjectBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            SubjectBean subjectBean = SubjectBean.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .createBy(rs.getLong("create_by"))
                    .createTime(rs.getTimestamp("create_time"))
                    .status(rs.getInt("status"))
                    .build();
            return subjectBean;
        }
    }
}
