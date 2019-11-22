package com.huatu.ztk.backend.subject.dao;

import com.huatu.ztk.backend.subject.bean.SubjectTreeBean;
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
public class SubjectTreeDao {

    private static final Logger logger = LoggerFactory.getLogger(SubjectTreeDao.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 科目列表
     *
     * @return
     */
    public List<SubjectTreeBean> findAll() {
        String sql = "SELECT * FROM v_new_subject_tree ";
        return jdbcTemplate.query(sql, new SubjectNewMapper());
    }


    /**
     * 新增科目
     *
     * @return
     */
    public void insertOld(SubjectTreeBean bean) {
        logger.info("insert subject to tree ={}", JsonUtil.toJson(bean));
        String sql = "INSERT v_new_subject_tree(id,name,status,parent) VALUES (?,?,?,?)";
        Object[] params = {
                bean.getId(),
                bean.getName(),
                SubjectStatus.AVAILABLE,
                bean.getParent()
        };

        jdbcTemplate.update(sql, params);
    }



    /**
     * 新增科目
     *
     * @return
     */
    public void insertNew(SubjectTreeBean bean) {
        logger.info("insert subject to tree ={}", JsonUtil.toJson(bean));
        String sql = "INSERT v_new_subject_tree(name,status,parent) VALUES (?,?,?)";
        Object[] params = {
                bean.getName(),
                SubjectStatus.AVAILABLE,
                bean.getParent()
        };

        jdbcTemplate.update(sql, params);
    }

    /**
     * 更新
     * @return
     */
    public void update(SubjectTreeBean bean) {

        logger.info("update subject={}", JsonUtil.toJson(bean));
        String sql = "UPDATE v_new_subject_tree set name=? WHERE id=?";
        Object[] params = {
                StringUtils.trimToEmpty(bean.getName()),
                bean.getId()
        };

        jdbcTemplate.update(sql, params);
    }


    /**
     * 删除
     * @return
     */
    public void delete(int id) {
        String sql = "DELETE FROM v_new_subject_tree WHERE id= ?";
        Object[] params = {
                id
        };
        jdbcTemplate.update(sql, params);
    }

    public void setStatus(int id, int status) {
        String sql = "UPDATE v_new_subject_tree set status=? WHERE id=?";
        Object[] params = {
                status,
                id
        };

        jdbcTemplate.update(sql, params);
    }


    class SubjectNewMapper implements RowMapper<SubjectTreeBean> {

        @Override
        public SubjectTreeBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            SubjectTreeBean newBean = SubjectTreeBean.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .status(rs.getInt("status"))
                    .parent(rs.getInt("parent"))
                    .build();
            return newBean;
        }
    }
}

