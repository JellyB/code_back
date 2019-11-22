package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.base.BaseDao;
import org.springframework.stereotype.Repository;

/**
 * 表scm_server_instance_permissions 对应 的dao
 * @author shaojieyue
 * @date 2013-08-15 20:02:36
 */
@Repository("instancePermissionsDao")
public class InstancePermissionsDao extends BaseDao {
	
	/**
	 * 插入记录
	 * @param userCode
	 * @param instanceId
	 * @param permissions
	 * @param createBy
	 * @return
	 */
	public boolean insert(String userCode,String instanceId,int permissions,String createBy){
		String insertSql="INSERT INTO scm_server_instance_permissions(user_code,instance_id,permissions,"+
		"create_by) VALUES (?,?,?,?)";
		int count = this.getJdbcTemplate().update(insertSql, userCode, instanceId, permissions, createBy);
		return count>0;
	}
	
	/**
	 * 更新用户实例的权限
	 * @param userCode
	 * @param instanceId
	 * @param permissions
	 * @return
	 */
	public boolean update(String userCode,String instanceId,int permissions){
		String updateSql="update scm_server_instance_permissions set permissions = ? where user_code = ? and instance_id= ? ";
		int count = this.getJdbcTemplate().update(updateSql, permissions,userCode,instanceId);
		return count>0;
	}
}
