package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.base.BaseDao;
import com.huatu.ztk.scm.dto.InstanceIp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * 表scm_instance_ip对应的dao
 * @author shaojieyue
 * @date 2013-07-18 15:58:52
 */
@Repository("instanceIpDao")
public class InstanceIpDao extends BaseDao {
	/**
	 * 根据instanceId 查询其拥有的实例ip
	 * @param instanceId
	 * @return
	 */
	public List<InstanceIp> queryByInstanceId(String instanceId){
		String sql = "select * from scm_instance_ip where instance_id = ? order by ip ";
		String[] params = {instanceId};
		List<InstanceIp> list=this.getJdbcTemplate().query(sql, params, new InstanceIpRowMapper());
		if(list==null){
			list = new ArrayList<InstanceIp>(0);
		}
		return list;
	}
	
	/**
	 * 删除指定实例的server
	 * @param instanceId
	 * @param ip
	 * @return
	 */
	public boolean delete(String instanceId,String ip){
		String delSql="delete from scm_instance_ip where instance_id = ? and ip = ? ";
		int count=this.getJdbcTemplate().update(delSql, instanceId,ip);
		return count>0;
	}
	
	/**
	 * 删除一个实例模板下的所有实例
	 * @param instanceId
	 * @return
	 */
	public boolean delete(String instanceId){
		String delSql="delete from scm_instance_ip where instance_id = ? ";
		int count=this.getJdbcTemplate().update(delSql, instanceId);
		return count>0;
	}
	
	public boolean insert(InstanceIp instance){
		String insertSql="INSERT INTO scm_instance_ip(ip,instance_id,create_by)VALUES(?,?,?)";
        Object[] params = {
				instance.getIp(),
				instance.getInstanceId(),
				instance.getCreateBy()
		};
		int count=this.getJdbcTemplate().update(insertSql,params);
		return count>0;
	}
	
	class InstanceIpRowMapper implements RowMapper<InstanceIp>{

		public InstanceIp mapRow(ResultSet rs, int rowNum) throws SQLException {
			InstanceIp instance = new InstanceIp();
			instance.setId(rs.getInt("id"));
			instance.setInstanceId(rs.getString("instance_id"));
			instance.setIp(rs.getString("ip"));
			return instance;
		}
		
	}
}
