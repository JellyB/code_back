package com.huatu.ztk.backend.system.dao;

import com.huatu.ztk.backend.system.bean.*;
import com.huatu.ztk.backend.system.service.RoleManageService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-04  16:46 .
 */
@Repository
public class RoleManageDao {
    private static final Logger logger = LoggerFactory.getLogger(RoleManageDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;




    /**
     * 查询后台管理系统的所有角色
     * @return
     */
    public List<RoleMessage> findAllRole(){
        String sql = "SELECT * FROM v_c_new_sys_role ";
        final List<RoleMessage> roleList = jdbcTemplate.query(sql,new RoleRowMapper());
        return roleList;
    }

    /**
     * 查询后台管理系统的所有角色
     * @return
     */
    public List<Catgory> findAllCatgory(){
        String sql = "SELECT * FROM v_new_catgory WHERE status=1";
        final List<Catgory> catgoryList = jdbcTemplate.query(sql,new CatgoryRowMapper());
        return catgoryList;
    }

    /**
     * 查询角色对应的所有考试类型
     * @return
     */
    public List<Catgory> findAllCatgoryByRoleId(int roleId){
        String sql = "SELECT a.*,b.lookup FROM v_new_catgory AS a JOIN v_c_new_role_catgory AS b ON a.id=b.catgoryId AND b.roleId=?";
        Object[] param = {roleId};
        final List<Catgory> catgoryList = jdbcTemplate.query(sql,param,new CatgoryRowMapper());
        return catgoryList;
    }

    /**
     * 查询用户id对应的所有考试类型
     * @return
     */
    public List<Catgory> findAllCatgoryByUserId(int userId){
        String sql = "SELECT a.*,b.lookup FROM v_new_catgory AS a JOIN v_c_new_role_catgory AS b ON a.id=b.catgoryId AND b.roleId IN\n" +
                "(SELECT role_id FROM v_c_new_user_role WHERE user_id=?)";
        Object[] param = {userId};
        final List<Catgory> catgoryList = jdbcTemplate.query(sql,param,new CatgoryRowMapper());
        return catgoryList;
    }

    /**
     * 查询后台管理系统的所有角色
     * @return
     */
    public List<RoleMessage> findAllRoleValid(){
        String sql = "SELECT * FROM v_c_new_sys_role WHERE is_system=1";
        final List<RoleMessage> roleList = jdbcTemplate.query(sql,new RoleRowMapper());
        return roleList;
    }

    /**
     * 根据角色名称，返回角色
     * @return
     */
    public List<RoleMessage> findRoleByName(String name){
        String sql = "SELECT * FROM v_c_new_sys_role WHERE name=?";
        Object[] param = {name};
        final List<RoleMessage> roleList = jdbcTemplate.query(sql,param,new RoleRowMapper());
        return roleList;
    }

    /**
     * 根据用户ID，返回角色列表
     * @return
     */
    public List<RoleMessage> findRoleByUserId(int userId){
        String sql = "SELECT * FROM v_c_new_sys_role WHERE is_system = 1 AND PUKEY IN (SELECT role_id FROM v_c_new_user_role WHERE user_id = ?)";
        Object[] param = {userId};
        final List<RoleMessage> roleList = jdbcTemplate.query(sql,param,new RoleRowMapper());
        return roleList;
    }


    /**
     * 根据角色ID，返回角色信息
     * @param id
     * @return
     */
    public RoleMessage findRoleById(int id){
        String sql = "SELECT * FROM v_c_new_sys_role WHERE PUKEY=?";
        Object[] param = {id};
        RoleMessage roleMessage = new RoleMessage();
        try {
            final List<RoleMessage> roleList = jdbcTemplate.query(sql,param,new RoleRowMapper());
            roleMessage = roleList.get(0);
        }catch (Exception e){
            e.printStackTrace();
        }
        return roleMessage;
    }

    /**
     * 根据角色ID，删除角色信息
     * @param roleId
     * @return
     */
    public void deleteRoleById(int roleId){
        String sql = "DELETE FROM v_c_new_sys_role WHERE PUKEY =?";
        Object[] param = {roleId};
        try{
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 根据角色ID，删除【用户、角色】对
     * @param roleId
     * @return
     */
    public void deleteRoleAction(int roleId){
        String sql = "DELETE FROM v_c_new_role_action WHERE roleId= ?";
        Object[] param = {roleId};
        try{
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据角色ID，删除【用户、考试类型】对
     * @param roleId
     * @return
     */
    public void deleteRoleCatgory(int roleId){
        String sql = "DELETE FROM v_c_new_role_catgory WHERE roleId= ?";
        Object[] param = {roleId};
        try{
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 根据角色信息，修改角色信息
     * @param role
     * @return
     */
    public void editRole(RoleMessage role){
        int roleId = role.getId();
        String name = role.getName();
        String intro = role.getIntro();
        String updaterName = role.getUpdateer();
        int status = role.getStatus();
        int updateTime = (int) (System.currentTimeMillis()/1000);
        String sql = "UPDATE v_c_new_sys_role SET name=?,introduction=?,is_system=?,update_time=?,updater_name=? WHERE PUKEY=?";
        Object[] param = {
                name,
                intro,
                status,
                updateTime,
                updaterName,
                roleId};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 根据角色id，将状态进行修改为锁定，表示该角色无效
     * @param roleId,updaterName
     * @return
     */
    public void toInvalidRole(int roleId,String updaterName){

        int updateTime = (int) (System.currentTimeMillis()/1000);

        String sql = "UPDATE v_c_new_sys_role SET is_system = 0,update_time=?,updater_name=? WHERE PUKEY=?";
        Object[] param = {
                updateTime,
                updaterName,
                roleId};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据角色信息，新增角色
     * @param role
     * @return
     */
    public void addRole(RoleMessage role){
        String name = role.getName();
        String intro = role.getIntro();
        String createrName = role.getCreater();
        int creatTime = (int) (System.currentTimeMillis()/1000);
        String sql = "INSERT INTO v_c_new_sys_role (name,introduction,creat_time,creater_name) VALUES(?,?,?,?)";
        Object[] param = {
                name,
                intro,
                creatTime,
                createrName,
                };
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据角色id、菜单id，新增【角色、菜单】对
     * @param roleId、menuId
     * @return
     */
    public void addRoleMenu(int roleId,int menuId){
        String sql = "INSERT INTO v_c_new_role_menu(role_id,menu_id) VALUES(?,?)";
        Object[] param = {
                roleId,
                menuId
        };
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据角色id、功能id，新增【角色、功能】对
     * @param roleId、actionId
     * @return
     */
    public void addRoleAction(int roleId,int actionId){
        String sql = "INSERT INTO v_c_new_role_action(roleId,actionId) VALUES(?,?)";
        Object[] param = {
                roleId,
                actionId
        };
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据角色id、功能ids，批量新增【角色、功能】对
     * @param roleId、actionId
     * @return
     */
    public void addRoleActions(int roleId,List<Integer> actionIds){
        String prefix = "INSERT INTO v_c_new_role_action(roleId,actionId) VALUES";
        // 保存sql后缀
        StringBuffer suffix = new StringBuffer();
        for(int i=0;i<actionIds.size();i++){
            suffix.append("(" + roleId + "," +actionIds.get(i)+"),");
        }
        String sql = prefix + suffix.substring(0, suffix.length() - 1);

        try {
            jdbcTemplate.update(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据角色id、功能ids，批量新增【角色、考试类型】对
     * @param roleId、catgoryIds
     * @return
     */
    public void addRoleCatgorys(int roleId,List<Integer> catgoryIds,int lookup){
        String prefix = "INSERT INTO v_c_new_role_catgory(roleId,catgoryId,lookup) VALUES";
        // 保存sql后缀
        StringBuffer suffix = new StringBuffer();
        for(int i=0;i<catgoryIds.size();i++){
            suffix.append("(" + roleId + "," +catgoryIds.get(i)+ "," +lookup+"),");
        }
        String sql = prefix + suffix.substring(0, suffix.length() - 1);

        try {
            jdbcTemplate.update(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private class RoleRowMapper implements RowMapper<RoleMessage> {
        @Override
        public RoleMessage mapRow(ResultSet rs, int rowNum) throws SQLException {


            String format =  "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sf = new SimpleDateFormat(format);
            Date creatTime = new Date((rs.getLong("creat_time"))*1000);
            Date updateTime = new Date((rs.getLong("update_time"))*1000);

            final RoleMessage roleMessage = RoleMessage.builder()
                    .id(rs.getInt("PUKEY"))
                    .name(rs.getString("name"))
                    .intro(rs.getString("introduction"))
                    .status(rs.getInt("is_system"))
                    .creatTime(sf.format(creatTime))
                    .creater(rs.getString("creater_name"))
                    .updateTime(sf.format(updateTime))
                    .updateer(rs.getString("updater_name"))
                    .build();
            return roleMessage;

        }
    }

    private class CatgoryRowMapper implements RowMapper<Catgory> {
        @Override
        public Catgory mapRow(ResultSet rs, int rowNum) throws SQLException {

            ResultSetMetaData rsmd;
            rsmd = rs.getMetaData();
            int numCols = rsmd.getColumnCount();
            int lookup = 0;
            if(numCols<6){
                lookup = 1;
            }else{
                lookup = rs.getInt("lookup");
            }
            Date createTime = new Date((rs.getLong("create_time"))*1000);
            final Catgory catgory = Catgory.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .createBy(rs.getLong("create_by"))
                    .status(rs.getInt("status"))
                    .createTime(createTime)
                    .lookup(lookup)
                    .build();
            return catgory;
        }
    }
}
