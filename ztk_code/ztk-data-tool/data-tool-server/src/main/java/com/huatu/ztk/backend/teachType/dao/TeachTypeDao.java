package com.huatu.ztk.backend.teachType.dao;

import com.huatu.ztk.backend.teachType.bean.TeachTypeBean;
import com.huatu.ztk.backend.teachType.bean.TeachTypeStatus;
import com.huatu.ztk.commons.JsonUtil;
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
public class TeachTypeDao {

    private static final Logger logger = LoggerFactory.getLogger(TeachTypeDao.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 科目列表
     * @return
     */
    public List<TeachTypeBean> findAll() {
        String sql = "SELECT * FROM v_new_teach_type ";
        return jdbcTemplate.query(sql,new TeachTypeMapper());
    }


    /**
     * 新增科目
     * @return
     */
    public void insert(TeachTypeBean teachTypeBean) {
        logger.info("insert subject={}", JsonUtil.toJson(teachTypeBean));
        String sql = "INSERT v_new_teach_type(name,subject,create_by,status) VALUES (?,?,?,?)";
        Object[] params = {
                teachTypeBean.getName(),
                teachTypeBean.getSubject(),
                teachTypeBean.getCreateBy(),
                TeachTypeStatus.AVAILABLE
        };

        jdbcTemplate.update(sql, params);
    }

    /**
     * 更新
     * @param teachTypeBean
     * @return
     */
    public void update(TeachTypeBean teachTypeBean) {

        logger.info("update subject={}", JsonUtil.toJson(teachTypeBean));
        String sql = "UPDATE v_new_teach_type set name=?,subject=?,create_by=?,status=? WHERE id=?";
        Object[] params = {
                teachTypeBean.getName(),
                teachTypeBean.getSubject(),
                teachTypeBean.getCreateBy(),
                teachTypeBean.getStatus(),
                teachTypeBean.getId()
        };

        jdbcTemplate.update(sql, params);
    }


    /**
     * 删除
     * @return
     */
    public void delete(int id) {
        String sql = "UPDATE v_new_teach_type SET status=? WHERE id= ?";
        Object[] params = {
                TeachTypeStatus.DELETE,
                id
        };

        jdbcTemplate.update(sql, params);
    }

    class TeachTypeMapper implements RowMapper<TeachTypeBean> {

        @Override
        public TeachTypeBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            TeachTypeBean teachTypeBean = TeachTypeBean.builder()
                    .id(rs.getInt("id"))
                    .subject(rs.getInt("subject"))
                    .name(rs.getString("name"))
                    .createBy(rs.getLong("create_by"))
                    .createTime(rs.getTimestamp("create_time"))
                    .status(rs.getInt("status"))
                    .build();
            return teachTypeBean;
        }
    }
}

