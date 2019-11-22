package com.huatu.ztk.backend.system.dao;

import com.huatu.ztk.backend.system.bean.OperateMessage;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-25  14:43 .
 */
@Repository
public class OperateDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询所有operate
     */
    public List<OperateMessage> findAllOperate(){
        String sql = "SELECT a.* ,b.name AS actionName FROM v_c_new_sys_operate_message AS a,v_c_new_sys_action AS b WHERE a.actionId=b.id";
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 查询所有operate(不包括名字)
     */
    public List<OperateMessage> findAllOperateNoName(){
        String sql = "SELECT a.*,a.name AS actionName FROM v_c_new_sys_operate_message AS a";
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,new OpertateRowMapper());
        return operateList;

    }

    /**
     * 根据id，查找操作
     * @param id
     */
    public OperateMessage findOperateById(int id){
        String sql = "SELECT a.* ,b.name AS actionName FROM v_c_new_sys_operate_message AS a,v_c_new_sys_action AS b WHERE a.actionId=b.id AND a.id=?";
        Object[] param = {id};
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        OperateMessage operate = null;
        if (CollectionUtils.isNotEmpty(operateList)) {
            operate = operateList.get(0);
        }
        return operate;
    }

    /**
     * 查询所有operate(不包括名字)
     */
    public OperateMessage findOperateByIdNoName(int id){
        String sql = "SELECT a.* ,a.name AS actionName FROM v_c_new_sys_operate_message AS a WHERE a.id=?";
        Object[] param = {id};
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        OperateMessage operate = null;
        if (CollectionUtils.isNotEmpty(operateList)) {
            operate = operateList.get(0);
        }
        return operate;
    }

    /**
     * 根据url，查找操作operate
     * @param url
     */
    public List<OperateMessage> findOperateByUrl(String url){
        String sql = "SELECT a.* ,b.name AS actionName FROM v_c_new_sys_operate_message AS a,v_c_new_sys_action AS b WHERE a.actionId=b.id AND a.url LIKE '%"+url+"'";
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据url，查找操作operate(正常)
     * @param url
     */
    public List<OperateMessage> findOperateNormalByUrl(String url){
        String sql = "SELECT a.* ,b.name AS actionName FROM v_c_new_sys_operate_message AS a,v_c_new_sys_action AS b WHERE a.actionId=b.id AND a.status=1 AND a.url LIKE '%"+url+"'";
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据name，查找操作operate
     * @param name
     */
    public List<OperateMessage> findOperateByName(String name){
        String sql = "SELECT a.* ,b.name AS actionName FROM v_c_new_sys_operate_message AS a,v_c_new_sys_action AS b WHERE a.actionId=b.id AND a.name=?";
        Object[] param = {name};
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据actionId，查找操作operate
     * @param actionId
     */
    public List<OperateMessage> findOperateByActionId(int actionId){
        String sql = "SELECT a.* ,b.name AS actionName FROM v_c_new_sys_operate_message AS a,v_c_new_sys_action AS b WHERE a.actionId=b.id AND a.actionId=?";
        Object[] param = {actionId};
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据actionId，查找操作operate
     * @param actionName
     */
    public List<OperateMessage> findOperateByActionName(String actionName){
        String sql = "SELECT a.* ,b.name AS actionName FROM v_c_new_sys_operate_message AS a,v_c_new_sys_action AS b WHERE a.actionId=b.id AND b.name=?";
        Object[] param = {actionName};
        final List<OperateMessage> operateList = jdbcTemplate.query(sql,param,new OpertateRowMapper());
        return operateList;
    }

    /**
     * 根据id，删除操作，即将操作的状态置为0
     * @param id
     */
    public void deleteOperate(int id){
        String sql = "UPDATE v_c_new_sys_operate_message SET status=0 WHERE id=?";
        Object[] param = {id};
        jdbcTemplate.update(sql,param);
    }

    /**
     * 根据OperateMessage，修改操作
     * @param operate
     */
    public void editOperate(OperateMessage operate){
        String sql = "UPDATE v_c_new_sys_operate_message SET name=?,discription=?,url=?,actionId=?,status=? WHERE id=?";
        Object[] param = {operate.getName(),
                operate.getDiscription(),
                operate.getUrl(),
                operate.getActionId(),
                operate.getStatus(),
                operate.getId()};
        jdbcTemplate.update(sql,param);
    }

    /**
     * 根据operate，插入操作
     * @param operate
     */
    public void addOperate(OperateMessage operate){
        String sql = "INSERT INTO v_c_new_sys_operate_message (name,discription,url,actionId) VALUES (?,?,?,?)";
        Object[] param = {operate.getName(),
                operate.getDiscription(),
                operate.getUrl(),
                operate.getActionId()};
        jdbcTemplate.update(sql,param);
    }

    private class OpertateRowMapper implements RowMapper<OperateMessage> {
        @Override
        public OperateMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            final OperateMessage operate = OperateMessage.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .discription(rs.getString("discription"))
                    .url(rs.getString("url"))
                    .actionId(rs.getInt("actionId"))
                    .status(rs.getInt("status"))
                    .actionName(rs.getString("actionName"))
                    .build();
            return operate;
        }
    }
}
