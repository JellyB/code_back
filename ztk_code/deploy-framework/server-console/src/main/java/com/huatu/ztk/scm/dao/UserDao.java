package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.dto.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * smc_user表dao
 * @author shaojieyue
 * @date 2013-08-20 15:20:17
 */
@Repository("scmUserDao")
public class UserDao  {
	@Resource
	private JdbcTemplate jdbcTemplate;
	/**
	 * 查询所有用户
	 * @return
	 */
	public List<User> queryAllUser(){
		String querysql="select * from scm_user";
		List<User> list = this.jdbcTemplate.query(querysql, new ScmUserRowMapper());
		if(list==null){
			list = new ArrayList(0);
		}
		return list;
	}
	
	/**
	 * 新增用户
	 * @param passport
	 * @param reateBy
	 * @return
	 */
	public boolean insert(String passport,String reateBy,String userName){
		String insertSql="INSERT INTO scm_user(passport,status,create_by,user_name)VALUES(?,?,?,?)";
		int count  = this.jdbcTemplate.update(insertSql, passport, 1, reateBy, userName);
		return count>0;
	}
	
	/**
	 * 删除用户
	 * @param passport
	 * @return
	 */
	public boolean delete(String passport){
		String delSql="DELETE FROM scm_user where passport = ? ";
		int count  = this.jdbcTemplate.update(delSql,passport);
		return count>0;
	}

	/**
	 * 域登录
	 * @param
	 * @return
	 */
	public User getUserByPassportInc (String passportInc) throws DataAccessException
	{
		String sql = "SELECT * FROM scm_user WHERE status=1 and passport_inc=?";
		List<User> list = this.jdbcTemplate.query(sql, new Object[]{passportInc}, new ScmUserRowMapper());
		if(list.size()>0)
			return  list.get(0);
		else
			return  null;
	}
	/**
	 * 域登录新增用户
	 * @param
	 * @return
	 */
	public boolean insertByPassportInc (User user) throws DataAccessException{
		String sql = "INSERT INTO scm_user (status,passport,user_name,create_date, passport_inc) VALUES(?,?,?,?,?)";
		int ret = this.jdbcTemplate.update(sql, new Object[]{user.getStatus(),user.getPassport(),user.getUserName(),new Date(),user.getPassportInc()});
		if(ret>0) {
            return true;
        }
		else {
            return false;
        }
	}
    
    /**
     * 检查用户是否存在
     * @param
     * @return
     */
    public boolean userExist(String passport){
         return get(passport)!=null;
    }
    
    public User get(String passport){
    	String sql = "SELECT * FROM scm_user t where t.passport=?  and t.status=1";
   	 	String[] params = {passport};
        List<User> scmUsers = this.jdbcTemplate.query(sql, params, new ScmUserRowMapper());
        User user = null;
        if(scmUsers!=null&&scmUsers.size()>0){
        	user = scmUsers.get(0);
        }
        
        return user;
    }

    class ScmUserRowMapper implements RowMapper<User> {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {

            User scmUser = new User();
            scmUser.setId(rs.getInt("id"));
            scmUser.setPassport(rs.getString("passport"));
            scmUser.setStatus(rs.getInt("status"));
            scmUser.setCreateBy(rs.getString("create_by"));
            scmUser.setUserName(rs.getString("user_name"));
            scmUser.setPwd(rs.getString("password"));
            scmUser.setRole(rs.getInt("role"));
            return scmUser;
        }

    }

}
