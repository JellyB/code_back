package com.huatu.ztk.scm.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.huatu.ztk.scm.base.BaseDao;
import com.huatu.ztk.scm.dto.Menu;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * 表 scm_menu Dao
 * @author shaojieyue
 * @date 2013-08-16 14:16:37
 */
@Repository("menuDao")
public class MenuDao extends BaseDao {
	
	/**
	 * 查询全部菜单
	 * @return
	 */
	public List<Menu> queryAllMenu(String userCode){
		String querySql="select sm.*,smu.user_code  from scm_menu sm left join  scm_user_menu smu "+ 
				"on ( sm.id=smu.menu_id and smu.user_code = ?)";
		String[] params = {
				userCode
		};
		List<Menu> menus  = this.getJdbcTemplate().query(querySql,params, new MenuRowMapper());
		return menus;
	}
	
	/**
	 * 查询用户所拥有的菜单
	 * @param userCode
	 * @return
	 */
	public List<Menu> queryMenu(String userCode){
		String querySql="select sm.* from scm_menu sm,scm_user_menu smu where sm.id=smu.menu_id and smu.user_code = ?";
		String[] params = {
				userCode
		};
		List<Menu> menus  = this.getJdbcTemplate().query(querySql,params, new MenuRowMapper());
		return menus;
	}
	
	class MenuRowMapper implements RowMapper<Menu>{
		public Menu mapRow(ResultSet rs, int rowNum) throws SQLException {
			Menu menu = new Menu();
			menu.setId(rs.getInt("id"));
			menu.setCreateBy(rs.getString("create_by"));
			menu.setLevel(rs.getInt("level"));
			menu.setMenuName(rs.getString("menu_name"));
			menu.setMenuUrl(rs.getString("menu_url"));
			menu.setRemark(rs.getString("remark"));
			try{
				menu.setUserCode(rs.getString("user_code"));
			}catch(Exception e){
				
			}
			return menu;
		}
	}
}
