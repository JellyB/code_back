package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.base.BaseDao;
import org.springframework.stereotype.Repository;

/**
 * 用户 菜单对应关系dao
 * @author shaojieyue
 * @date 2013-08-16 15:33:46
 */
@Repository("userMenuDao")
public class UserMenuDao extends BaseDao {
	public boolean insert(String userCode,String menuId,String createBy){
		String insertSql="INSERT INTO scm_user_menu(user_code,menu_id,create_by) values (?,?,?)";
        Object[] params = {
				userCode,
				menuId,
				createBy
		};
		int count = this.getJdbcTemplate().update(insertSql,params);
		return count>0;
	}
	
	public boolean deleteAllUserMenu(String userCode){
		String insertSql="DELETE FROM scm_user_menu where user_code = ?";
        Object[] params = {
				userCode,
		};
		int count = this.getJdbcTemplate().update(insertSql,params);
		return count>=0;
	}
	
	public boolean delete(String userCode,String menuId){
		String insertSql="DELETE FROM scm_user_menu where user_code = ? and menu_id = ?";
        Object[] params = {
				userCode,
				menuId,
		};
		int count = this.getJdbcTemplate().update(insertSql,params);
		return count>0;
	}
}
