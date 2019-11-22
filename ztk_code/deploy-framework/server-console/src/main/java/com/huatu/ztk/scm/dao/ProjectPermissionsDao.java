package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.base.BaseDao;
import org.springframework.stereotype.Repository;

/**
 * 表 scm_project_permissions dao
 * @author shaojieyue
 * @date 2013-08-15 16:58:02
 */
@Repository("projectPermissionsDao")
public class ProjectPermissionsDao extends BaseDao {
	
	/**
	 * 插入记录
	 * @param userCode
	 * @param projectId
	 * @param permissions
	 * @param createBy
	 * @return
	 */
	public boolean insert(String userCode ,String projectId,int permissions,String createBy ){
		String insertSql="INSERT INTO scm_project_permissions(user_code,project_id,permissions,create_by)VALUES(?,?,?,?)";
		int count = this.getJdbcTemplate().update(insertSql, userCode,projectId,permissions,createBy);
		return count>0;
	}
	
	/**
	 * 更新用户的指定project的权限
	 * @param userCode
	 * @param projectId
	 * @param permissions
	 * @return
	 */
	public boolean update(String userCode,String projectId,int permissions){
		String updateSql = "UPDATE scm_project_permissions SET permissions = ? where project_id = ? and user_code = ?";
		int count = this.getJdbcTemplate().update(updateSql, permissions,projectId,userCode);
		return count>0;
	}
	
}
