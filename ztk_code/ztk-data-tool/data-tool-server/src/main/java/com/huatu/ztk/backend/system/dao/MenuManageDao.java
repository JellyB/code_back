package com.huatu.ztk.backend.system.dao;

import com.huatu.ztk.backend.system.bean.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-04  11:03 .
 */
@Repository
public class MenuManageDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询后台管理系统的所有顶级菜单项
     * @return
     */
    public List<Menu> findAllTopMenu(){
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE parentid = 0";
        final List<Menu> allTopMenulist = jdbcTemplate.query(sql,new MenuRowMapper());
        return allTopMenulist;
    }

    /**
     * 根据菜单id，返回菜单信息
     * @param id
     * @return
     */
    public Menu findMenuById(int id){
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE PUKEY =?";
        Object[] param = {id};
        final List<Menu> menuList = jdbcTemplate.query(sql,param,new MenuRowMapper());
        Menu menu = menuList.get(0);
        return menu;
    }


    /**
     * 根据用户name，返回该用户有权限查看的菜单信息
     * @param uname
     * @return
     */
    public List<Menu> findMenuByUname(String uname){
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE status=1 AND PUKEY IN\n" +
                "(\n" +
                "  SELECT menu_id FROM v_c_new_role_menu WHERE role_id IN\n" +
                "  (\n" +
                "     SELECT a.role_id FROM v_c_new_user_role AS a, v_c_new_sys_role AS b WHERE a.role_id = b.PUKEY AND b.is_system = 1 AND user_id =\n" +
                "     (\n" +
                "         SELECT PUKEY FROM v_c_sys_user WHERE uname =?" +
                "     )\n" +
                "  )\n" +
                ")";
        Object[] param = {uname};
        final List<Menu> menuList = jdbcTemplate.query(sql,param,new MenuRowMapper());
        return menuList;
    }

    /**
     * 根据用户id，返回该用户有权限查看的菜单信息
     * @param uid
     * @return
     */
    public List<Menu> findMenuByUid(int uid){
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE status=1 AND templateUrl IN (\n" +
                "    SELECT url FROM v_c_new_sys_operate_message WHERE status=1 AND actionId!=1 AND actionId IN (\n" +
                "          SELECT actionId FROM v_c_new_role_action WHERE roleId IN (\n" +
                "              SELECT role_id FROM v_c_new_user_role WHERE user_id =?\n" +
                "                )\n" +
                "    )\n" +
                ") UNION SELECT * FROM v_c_new_sys_menu WHERE  status=1 AND PUKEY IN (\n" +
                "  SELECT parentid FROM v_c_new_sys_menu WHERE status=1 AND templateUrl IN (\n" +
                "    SELECT url FROM v_c_new_sys_operate_message WHERE status=1 AND  actionId!=1 AND actionId IN (\n" +
                "          SELECT actionId FROM v_c_new_role_action WHERE roleId IN (\n" +
                "              SELECT role_id FROM v_c_new_user_role WHERE user_id =?\n" +
                "                )\n" +
                "    )\n" +
                ")\n" +
                ")";
        Object[] param = {uid,uid};
        final List<Menu> menuList = jdbcTemplate.query(sql,param,new MenuRowMapper());
        return menuList;
    }



    /**
     * 根据菜单name,返回菜单列表
     * @param
     * @return
     */
    public List<Menu> findMenuByName(String name){
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE name = ?";
        Object[] param = {name};
        final List<Menu> menuList = jdbcTemplate.query(sql,param,new MenuRowMapper());
        return menuList;
    }

    /**
     * 根据菜单id，返回菜单的所有子菜单
     * @param id
     * @return
     */
    public List<Menu> findSubmenuById(int id){
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE parentid = ?";
        Object[] param = {id};
        final List<Menu> subMenu = jdbcTemplate.query(sql,param,new MenuRowMapper());
        return subMenu;
    }


    /**
     * 根据菜单name，返回菜单的所有子菜单
     * @param name
     * @return
     */
    public List<Menu> findSubmenuByName(String name){
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE target=?";
        Object[] param = {name};
        final List<Menu> subMenu = jdbcTemplate.query(sql,param,new MenuRowMapper());
        return subMenu;
    }

    /**
     * 根据菜单id，删除菜单
     * @param id
     * @return
     */
    public void deleteMenu(int id){
        String sql = "UPDATE v_c_new_sys_menu SET status=0 WHERE PUKEY = ?";
        Object[] param = {id};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 根据菜单id，删除【角色、菜单】对，即删除该角色对所有菜单的权限
     * @param id
     * @return
     */
    public void deleteRoleMenu(int id){
        String sql = "DELETE FROM v_c_new_role_menu WHERE menu_id = ?";
        Object[] param = {id};
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 查询后台管理系统的所有菜单项
     * @return
     */
    public List<Menu> findAllMenu(){
        String sql = "SELECT * FROM v_c_new_sys_menu";
        final List<Menu> allMenulist = jdbcTemplate.query(sql,new MenuRowMapper());
        return allMenulist;
    }

    /**
     * 查询后台管理系统的所有菜单项(状态为正常，即status=1)
     * @return
     */
    public List<Menu> findAllMenuNormal(){
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE status=1";
        final List<Menu> allMenulist = jdbcTemplate.query(sql,new MenuRowMapper());
        return allMenulist;
    }



    /**
     * 根据角色id，返回该角色拥有权限的所有菜单项
     * @param roleId
     * @return
     */
    public List<Menu> findMenusByRoleId(int roleId){
        List<Menu> menuList = new ArrayList<>();
        String sql = "SELECT * FROM v_c_new_sys_menu WHERE PUKEY IN (SELECT menu_id FROM v_c_new_role_menu WHERE role_id=?)";
        Object[] param = {roleId};
        try {
            menuList = jdbcTemplate.query(sql,param,new MenuRowMapper());
        }catch (Exception e){
            e.printStackTrace();
        }
        return menuList;
    }


    /**
     * 根据菜单，修改菜单信息
     * @param menu
     * @return
     */
    public void editMenu(Menu menu){
        int updateTime = (int) (System.currentTimeMillis()/1000);
        String sql = "UPDATE v_c_new_sys_menu SET name=?,discription=?,target=?,parentid=?,sref=?,update_time=?,updater_name=?,templateUrl=? WHERE PUKEY=?";
        Object[] param = {
                menu.getText(),
                menu.getIntro(),
                menu.getParentName(),
                menu.getParentId(),
                menu.getSref(),
                updateTime,
                menu.getUpdateer(),
                menu.getTemplateUrl(),
                menu.getId()};
        jdbcTemplate.update(sql,param);
    }

    /**
     * 新增菜单
     * @param menu
     * @return
     */
    public void addMenu(Menu menu){
        int creatTime = (int) (System.currentTimeMillis()/1000);
        String sql = "INSERT INTO v_c_new_sys_menu (name,discription,target,parentid,sref,creat_time,creater_name) VALUES (?,?,?,?,?,?,?)";
        Object[] param = {
                menu.getText(),
                menu.getIntro(),
                menu.getParentName(),
                menu.getParentId(),
                menu.getSref(),
                creatTime,
                menu.getCreater()};
        jdbcTemplate.update(sql,param);
    }

    public List<String> findTemplateUrlByUid(int uid) {
        String sql = "select DISTINCT o.url from v_c_new_user_role r,v_c_new_role_action a,v_c_new_sys_operate_message o where r.role_id = a.roleId and a.actionId = o.actionId and r.user_id =  ?";
        Object[] params = {uid};
        return jdbcTemplate.queryForList(sql,params,String.class);
    }


    private class MenuRowMapper implements RowMapper<Menu> {
        @Override
        public Menu mapRow(ResultSet rs, int rowNum) throws SQLException {
            String format =  "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sf = new SimpleDateFormat(format);
            Date creatTime = new Date(rs.getLong("creat_time")*1000);
            Date updateTime = new Date(rs.getLong("update_time")*1000);

            final Menu menu = Menu.builder()
                    .id(rs.getInt("PUKEY"))
                    .text(rs.getString("name"))
                    .sref(rs.getString("sref"))
                    .templateUrl(rs.getString("templateUrl"))
                    .parentId(rs.getInt("parentid"))
                    .parentName(rs.getString("target"))
                    .intro(rs.getString("discription"))
                    .creatTime(sf.format(creatTime))
                    .creater(rs.getString("creater_name"))
                    .updateTime(sf.format(updateTime))
                    .updateer(rs.getString("updater_name"))
                    .status(rs.getInt("status"))
                    .build();
            return menu;

        }
    }
}
