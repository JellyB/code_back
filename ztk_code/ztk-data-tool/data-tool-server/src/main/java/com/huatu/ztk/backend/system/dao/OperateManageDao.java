package com.huatu.ztk.backend.system.dao;

import com.huatu.ztk.backend.system.bean.Menu;
import com.huatu.ztk.backend.system.bean.Operate;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-01-16  09:52 .
 */
@Repository
public class OperateManageDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询所有operate
     */
    public List<Operate> findAllOperate(){
        String sql = "SELECT a.* ,b.name AS menuName FROM v_c_new_sys_operate AS a,v_c_new_sys_menu AS b WHERE a.menu_id=b.PUKEY";
        final List<Operate> operateList = jdbcTemplate.query(sql,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据id，查找操作
     * @param id
     */
    public Operate findOperateById(int id){
        String sql = "SELECT a.* ,b.name AS menuName FROM v_c_new_sys_operate AS a,v_c_new_sys_menu AS b WHERE a.menu_id=b.PUKEY AND a.id=?";
        Object[] param = {id};
        final List<Operate> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        Operate operate = null;
        if (CollectionUtils.isNotEmpty(operateList)) {
            operate = operateList.get(0);
        }
        return operate;
    }


    /**
     * 根据url，查找操作operate
     * @param url
     */
    public List<Operate> findOperateByUrl(String url){
        String sql = "SELECT a.* ,b.name AS menuName FROM v_c_new_sys_operate AS a,v_c_new_sys_menu AS b WHERE a.menu_id=b.PUKEY AND a.url LIKE '%"+url+"'";
        final List<Operate> operateList = jdbcTemplate.query(sql,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据url，查找操作operate(正常)
     * @param url
     */
    public List<Operate> findOperateNormalByUrl(String url){
        String sql = "SELECT a.* ,b.name AS menuName FROM v_c_new_sys_operate AS a,v_c_new_sys_menu AS b WHERE a.menu_id=b.PUKEY AND a.status=1 AND a.url LIKE '%"+url+"'";
        final List<Operate> operateList = jdbcTemplate.query(sql,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据name，查找操作operate
     * @param name
     */
    public List<Operate> findOperateByName(String name){
        String sql = "SELECT a.* ,b.name AS menuName FROM v_c_new_sys_operate AS a,v_c_new_sys_menu AS b WHERE a.menu_id=b.PUKEY AND a.name=?";
        Object[] param = {name};
        final List<Operate> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据menu_id，查找操作operate
     * @param menuId
     */
    public List<Operate> findOperateByNMenuId(int menuId){
        String sql = "SELECT a.* ,b.name AS menuName FROM v_c_new_sys_operate AS a,v_c_new_sys_menu AS b WHERE a.menu_id=b.PUKEY AND a.menu_id=?";
        Object[] param = {menuId};
        final List<Operate> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据menu_id，查找操作operate
     * @param menuName
     */
    public List<Operate> findOperateByNMenuName(String menuName){
        String sql = "SELECT a.* ,b.name AS menuName FROM v_c_new_sys_operate AS a,v_c_new_sys_menu AS b WHERE a.menu_id=b.PUKEY AND b.name=?";
        Object[] param = {menuName};
        final List<Operate> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据id，删除操作，即将操作的状态置为0
     * @param id
     */
    public void deleteOperate(int id){
        String sql = "UPDATE v_c_new_sys_operate SET status=0 WHERE id=?";
        Object[] param = {id};
        jdbcTemplate.update(sql,param);
    }

    /**
     * 根据Operate，修改操作
     * @param operate
     */
    public void editOperate(Operate operate){
        String sql = "UPDATE v_c_new_sys_operate SET name=?,discription=?,url=?,menu_id=?,status=? WHERE id=?";
        Object[] param = {operate.getName(),
                          operate.getDiscription(),
                          operate.getUrl(),
                          operate.getMenuId(),
                          operate.getStatus(),
                          operate.getId()};
        jdbcTemplate.update(sql,param);
    }

    /**
     * 根据operate，插入操作
     * @param operate
     */
    public void addOperate(Operate operate){
        String sql = "INSERT INTO v_c_new_sys_operate (name,discription,url,menu_id) VALUES (?,?,?,?)";
        Object[] param = {operate.getName(),
                operate.getDiscription(),
                operate.getUrl(),
                operate.getMenuId()};
        jdbcTemplate.update(sql,param);
    }

    private class OpertateRowMapper implements RowMapper<Operate> {
        @Override
        public Operate mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Operate operate = Operate.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .discription(rs.getString("discription"))
                    .url(rs.getString("url"))
                    .menuId(rs.getInt("menu_id"))
                    .status(rs.getInt("status"))
                    .menuName(rs.getString("menuName"))
                    .build();
            return operate;
        }
    }
}
