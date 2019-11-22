package com.huatu.ztk.backend.system.dao;

import com.huatu.ztk.backend.system.bean.RoleMessage;
import com.huatu.ztk.backend.system.bean.UserMessage;
import com.huatu.ztk.backend.system.common.error.UserAddError;
import com.huatu.ztk.commons.exception.BizException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-11-23  15:43 .
 */
@Repository
public class UserManageDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询后台管理系统的所有用户
     * @return
     */
    public List<UserMessage> findAllUser(){
        String sql = "SELECT * FROM v_c_sys_user";
        final List<UserMessage> userlist = jdbcTemplate.query(sql,new UserRowMapper());
        return userlist;
    }


    /**
     * 根据id，获取用户
     * @return
     */
    public UserMessage findUserById(int id){
        UserMessage userMessage = new UserMessage();
        String sql = "SELECT * FROM v_c_sys_user WHERE PUKEY=?";
        Object[] param = {id};
        final List<UserMessage> userlist = jdbcTemplate.query(sql,param,new UserRowMapper());
        if (CollectionUtils.isNotEmpty(userlist)) {
            userMessage = userlist.get(0);
        }
        return userMessage;
    }

    /**
     * 根据name，获取用户
     * @return
     */
    public List<UserMessage> findUserByName(String name){
        String sql = "SELECT * FROM v_c_sys_user WHERE uname=?";
        Object[] param = {name};
        final List<UserMessage> userlist = jdbcTemplate.query(sql,param,new UserRowMapper());
        return userlist;
    }


    /**
     * 根据account，获取用户
     * @return
     */
    public List<UserMessage> findUserByAccount(String account){
        String sql = "SELECT * FROM v_c_sys_user WHERE umark=?";
        Object[] param = {account};
        final List<UserMessage> userlist = jdbcTemplate.query(sql,param,new UserRowMapper());
        return userlist;
    }


    /**
     * 根据用户ID，删除后台管理系统的用户(将用户状态修改为0，即为该用户不可用)
     * @param id
     * @return
     */
    public boolean deleteUser( int id){
        boolean result = true;
        String sql = "UPDATE v_c_sys_user SET status=0 WHERE PUKEY=?";
        Object[] param = {id};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
            result=false;
        }
        return  result;
    }

    /**
     * 根据用户ID，删除后台管理系统的用户(将用户状态修改为0，即为该用户不可用)
     * @param userMessage
     * @return
     */
    public int addUser( UserMessage userMessage) throws BizException {
        String sqlQuery = "SELECT * FROM v_c_sys_user WHERE PUKEY=(SELECT MAX(PUKEY) from v_c_sys_user)";
        final List<UserMessage> userlist = jdbcTemplate.query(sqlQuery,new UserRowMapper());
        int pukey = -1;
        if (CollectionUtils.isNotEmpty(userlist)) {
            pukey = userlist.get(0).getId();
        }
        if(pukey==-1){
            throw new BizException(UserAddError.ADD_FAIL);
        }
        String sql = "INSERT v_c_sys_user (PUKEY,uname,passwd,status,umark,EB1B1,BB103,BB104,BB105) VALUES (?,?,?,?,?,?,?,?,?)";
        Object[] param = {pukey+1,
                userMessage.getUname(),
                userMessage.getPassword(),
                userMessage.getStatus(),
                userMessage.getAccount(),
                "",
                System.currentTimeMillis()/1000,
                userMessage.getCreator(),
                userMessage.getCreatorId()};
        jdbcTemplate.update(sql,param);
        return pukey+1;
    }


    /**
     * 根据userid，删除【用户、角色】对，即删除该用户的所有角色
     * @param userId
     * @return
     */
    public void deleteUserRole(int userId){
        String sql = "DELETE FROM v_c_new_user_role WHERE user_id =?";
        Object[] param = {userId};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据userId，roleId，插入【用户、角色】对
     * @param userId，roleiId
     * @return
     */
    public void insertUserRole(int userId,int roleId){
        String sql = "INSERT INTO v_c_new_user_role(user_id,role_id) VALUES (?,?)";
        Object[] param = {userId,roleId};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据userId，password，修改用户密码
     * @param userId，password
     * @return
     */
    public void editUserPassword(int userId,String password){
        String sql = "UPDATE v_c_sys_user SET passwd=? WHERE PUKEY=?";
        Object[] param = {password,userId};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据userId，password，修改用户密码
     * @param userId，password
     * @return
     */
    public void editUserStatus(int userId,int status){
        String sql = "UPDATE v_c_sys_user SET status=? WHERE PUKEY=?";
        Object[] param = {status,userId};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private class UserRowMapper implements RowMapper<UserMessage> {
        @Override
        public UserMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            String format =  "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sf = new SimpleDateFormat(format);
            Date creatTime = new Date(rs.getLong("BB103")*1000);
            Date updateTime = new Date(rs.getLong("BB106")*1000);
            Date lastLoginTime = new Date(rs.getLong("last_login_time")*1000);

            final UserMessage userMessage = UserMessage.builder()
                    .id(rs.getInt("PUKEY"))
                    .uname(rs.getString("uname"))
                    .account(rs.getString("umark"))
                    .status(rs.getInt("status"))
                    .lastLoginIp(rs.getString("last_login_ip"))
                    .lastLoginTime(sf.format(lastLoginTime))
                    .loginSuccessCount(rs.getInt("login_success_count"))
                    .creatTime(sf.format(creatTime))
                    .creator(rs.getString("BB104"))
                    .updateTime(sf.format(updateTime))
                    .updateer(rs.getString("BB107"))
                    .build();
            return userMessage;

        }
    }
}
