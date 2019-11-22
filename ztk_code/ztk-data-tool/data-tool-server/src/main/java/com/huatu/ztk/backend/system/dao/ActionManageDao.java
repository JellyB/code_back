package com.huatu.ztk.backend.system.dao;

import com.huatu.ztk.backend.system.bean.Action;
import com.huatu.ztk.backend.system.bean.Menu;
import com.huatu.ztk.backend.system.bean.Operate;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-22  19:30 .
 */
@Repository
public class ActionManageDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询所有功能
     */
    public List<Action> findAllAction(){
        String sql = "SELECT * FROM v_c_new_sys_action";
        final List<Action> actionList = jdbcTemplate.query(sql,new ActionRowMapper());
        return actionList;
    }

    /**
     * 查询所有功能
     */
    public List<Action> findAllActionValid(){
        String sql = "SELECT * FROM v_c_new_sys_action WHERE status=1";
        final List<Action> actionList = jdbcTemplate.query(sql,new ActionRowMapper());
        return actionList;
    }

    /**
     * 查询所有顶级功能
     */
    public List<Action> findAllTopAction(){
        String sql = "SELECT * FROM v_c_new_sys_action WHERE parentId=0 AND status=1";
        final List<Action> actionList = jdbcTemplate.query(sql,new ActionRowMapper());
        return actionList;
    }

    /**
     * 根据type,content，查找功能
     * @param type
     * @param content
     */
    public List<Action> findActionByType(int type,String content){
        String sql = "";
        if(type==1){//根据功能名字查询
            sql = "SELECT * FROM v_c_new_sys_action WHERE name LIKE '%"+content+"%'";
        }else if(type==2){//根据父级功能名字查询
            sql = "SELECT * FROM v_c_new_sys_action WHERE parentName LIKE '%"+content+"%'";
        }
        final List<Action> actionList = jdbcTemplate.query(sql,new ActionRowMapper());
        return actionList;
    }

    /**
     * 根据id，删除操作，即将操作的状态置为0
     * @param id
     */
    public void deleteAction(int id){
        String sql = "UPDATE v_c_new_sys_action SET status=0 WHERE id=?";
        Object[] param = {id};
        jdbcTemplate.update(sql,param);
    }


    /**
     * 根据action，插入操作
     * @param action
     */
    public void addAction(Action action){
        String sql = "INSERT INTO v_c_new_sys_action (name,discription,parentId,parentName,status) VALUES (?,?,?,?,?)";
        Object[] param = {action.getName(),
                action.getDiscription(),
                action.getParentId(),
                action.getParentName(),
                action.getStatus()};
        jdbcTemplate.update(sql,param);
    }

    /**
     * 根据用户id，返回该用户有权限查看的菜单信息
     * @param uid
     * @return
     */
    public List<Action> findActionByUid(int uid){
        String sql = "\n" +
                "SELECT t1.* FROM v_c_new_sys_action t1 RIGHT JOIN (SELECT t3.actionId FROM v_c_new_role_action t3 RIGHT JOIN\n" +
                "(SELECT role_id FROM v_c_new_user_role WHERE user_id =?) t4  on t3.roleId=t4.role_id) t2 on t1.id = t2.actionId ORDER BY t1.id";
        Object[] param = {uid};
        final List<Action> actionList = jdbcTemplate.query(sql,param,new ActionRowMapper());
        return actionList;
    }

    /**
     * 根据id，查找功能
     * @param id
     */
    public Action findActionById(int id){
        String sql = "SELECT * FROM v_c_new_sys_action WHERE id=?";
        Object[] param = {id};
        final List<Action> actionList = jdbcTemplate.query(sql,param,new ActionRowMapper());
        Action action = null;
        if (CollectionUtils.isNotEmpty(actionList)) {
            action = actionList.get(0);
        }
        return action;
    }

    /**
     * 根据action，修改功能
     * @param action
     */
    public void editAction(Action action){
        String sql = "UPDATE v_c_new_sys_action SET name=?,discription=?,parentId=?,parentName=?,status=? WHERE id=?";
        Object[] param = {action.getName(),
                action.getDiscription(),
                action.getParentId(),
                action.getParentName(),
                action.getStatus(),
                action.getId()};
        jdbcTemplate.update(sql,param);
    }

    /**
     * 根据角色id，返回该角色拥有权限的所有功能
     * @param roleId
     * @return
     */
    public List<Action> findActionsByRoleId(int roleId){
        List<Action> actionList = new ArrayList<>();
        String sql = "SELECT * FROM v_c_new_sys_action WHERE id IN (SELECT actionId FROM v_c_new_role_action WHERE roleId=?)";
        Object[] param = {roleId};
        try {
            actionList = jdbcTemplate.query(sql,param,new ActionRowMapper());
        }catch (Exception e){
            e.printStackTrace();
        }
        return actionList;
    }



    private class ActionRowMapper implements RowMapper<Action> {
        @Override
        public Action mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Action action = Action.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .discription(rs.getString("discription"))
                    .parentId(rs.getInt("parentId"))
                    .status(rs.getInt("status"))
                    .parentName(rs.getString("parentName"))
                    .build();
            return action;
        }
    }
}
