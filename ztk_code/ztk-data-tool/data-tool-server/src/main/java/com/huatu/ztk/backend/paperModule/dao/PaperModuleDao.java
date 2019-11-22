package com.huatu.ztk.backend.paperModule.dao;

import com.huatu.ztk.backend.paperModule.bean.PaperModuleBean;
import com.huatu.ztk.backend.paperModule.bean.PaperModuleStatus;
import com.huatu.ztk.commons.JsonUtil;
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
import java.util.List;

/**
 * Created by ht on 2016/12/21.
 */
@Repository
public class PaperModuleDao {

    private static final Logger logger = LoggerFactory.getLogger(PaperModuleDao.class);


    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 列表
     * @return
     */
    public List<PaperModuleBean> findAll() {
        String sql = "SELECT * FROM v_new_paper_module ORDER BY id ASC";
        return jdbcTemplate.query(sql,new PaperModuleMapper());
    }

    /**
     * 列表
     * @return
     */
    public List<PaperModuleBean> findAvailableAll() {
        String sql = "SELECT * FROM v_new_paper_module WHERE status = 1 ORDER BY id ASC";
        return jdbcTemplate.query(sql,new PaperModuleMapper());
    }


    /**
     * id查询
     * @param id
     * @return
     */
    public PaperModuleBean findById(int id) {
        String sql = "SELECT * FROM v_new_paper_module where id = " + id;
        List<PaperModuleBean> list = jdbcTemplate.query(sql, new PaperModuleMapper());

        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 新增科目
     * @return
     */
    public void insert(PaperModuleBean PaperModuleBean) {
        logger.info("insert module={}", JsonUtil.toJson(PaperModuleBean));
        String sql = "INSERT v_new_paper_module (name,subject,description,create_by,status) VALUES (?,?,?,?,?)";
        Object[] params = {
                PaperModuleBean.getName(),
                PaperModuleBean.getSubject(),
                PaperModuleBean.getDescription(),
                PaperModuleBean.getCreateBy(),
                PaperModuleStatus.AVAILABLE
        };

        jdbcTemplate.update(sql, params);
    }

    /**
     * 更新
     * @param PaperModuleBean
     * @return
     */
    public void update(PaperModuleBean PaperModuleBean) {

        logger.info("update subject={}", JsonUtil.toJson(PaperModuleBean));
        String sql = "UPDATE v_new_paper_module set subject=?,name=?,description=?,create_by=?,status=? WHERE id=?";
        Object[] params = {
                PaperModuleBean.getSubject(),
                StringUtils.trimToEmpty(PaperModuleBean.getName()),
                PaperModuleBean.getDescription(),
                PaperModuleBean.getCreateBy(),
                PaperModuleBean.getStatus(),
                PaperModuleBean.getId()
        };

        jdbcTemplate.update(sql, params);
    }


    /**
     * 删除
     * @return
     */
    public void delete(int id) {
        String sql = "UPDATE v_new_paper_module SET status=? WHERE id= ?";
        Object[] params = {
                PaperModuleStatus.DELETE,
                id
        };

        jdbcTemplate.update(sql,params);
    }

    class PaperModuleMapper implements RowMapper<PaperModuleBean> {

        @Override
        public PaperModuleBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            PaperModuleBean dto = PaperModuleBean.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .subject(rs.getInt("subject"))
                    .createBy(rs.getLong("create_by"))
                    .createTime(rs.getTimestamp("create_time"))
                    .status(rs.getInt("status"))
                    .build();
            return dto;
        }
    }


}
