package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.base.BaseDao;
import com.huatu.ztk.scm.dto.ServerInstance;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
@Repository("smcServerInstanceDao")
public class ServerInstanceDao extends BaseDao {
	
	public List<ServerInstance> getAllSmcServerInstance(String userCode) {
		String sql ="SELECT ssi.*,ssip.* FROM scm_server_instance ssi  left JOIN  scm_server_instance_permissions ssip  ON (ssi.id=ssip.instance_id and ssip.user_code= ? ) ";
		String[] params = {
				userCode
		};
		List<ServerInstance> instances = this.getJdbcTemplate().query(sql,params,new SmcServerInstanceRowMapper());
		return instances;
	}

    public List<ServerInstance> getAllSmcServerInstance() {
        String sql ="SELECT ssi.*,3 as permissions FROM scm_server_instance ssi";
        List<ServerInstance> instances = this.getJdbcTemplate().query(sql,new SmcServerInstanceRowMapper());
        return instances;
    }

    public List<ServerInstance>  getSmcServerByServerMode(String serverMode){
        String sql ="SELECT ssi.*,3 as permissions FROM scm_server_instance ssi where ssi.server_mode=?";
        String[] params = {
          serverMode
        };
        List<ServerInstance> instances = this.getJdbcTemplate().query(sql,params,new SmcServerInstanceRowMapper());
        return instances;
    }
	
	public List<ServerInstance> querySmcServerInstance(String userCode){
		String querySql="SELECT ssi.*,ssip.permissions FROM scm_server_instance ssi,scm_server_instance_permissions ssip "+
						"where ssi.id=instance_id and ssip.user_code=? and ssip.permissions > 1 order by ssi.server_name ";
		String[] params = {
				userCode
		};
		List<ServerInstance> instances = this.getJdbcTemplate().query(querySql,params,new SmcServerInstanceRowMapper());
		return instances;
	}
	
	public ServerInstance getServerInstanceById(String id){
		String sql ="SELECT * FROM scm_server_instance where id= ?";
		String[] params ={id};
		List<ServerInstance> instances = this.getJdbcTemplate().query(sql,params,new SmcServerInstanceRowMapper());
		if(instances!=null&&instances.size()>0){
			return instances.get(0);
		}
		return null;
	}


	
	/**
	 * 根据id删除实例模板
	 * @param instanceId
	 * @return
	 */
	public boolean delete(String instanceId){
		String delSql = "delete from scm_server_instance where id= ? ";
		int count = this.getJdbcTemplate().update(delSql,instanceId);
		return count>0;
	}
	
	public boolean insert(ServerInstance instance){
		String sql = "INSERT INTO scm_server_instance(id,remark,project_name,server_name,main_class,main_args,jvm_args,create_by,source_path,server_mode)"
					+"VALUES(?,?,?,?,?,?,?,?,?,?)";
		Object[] params = {
				instance.getId(),
				instance.getRemark(),
				instance.getProjectName(),
				instance.getServerName(),
				instance.getMainClass(),
				instance.getMainArgs(),
				instance.getJvmArgs(),
				instance.getCreateBy(),
				instance.getSourcePath(),
				instance.getServerMode()
		};
		int count=this.getJdbcTemplate().update(sql, params);
		return count>0;
	}
	
	/**
	 * 更新实例模板信息,注意:只能更新的字段有 source_path,main_args,jvm_args,remark
	 * @param instance
	 * @return
	 */
	public boolean update(ServerInstance instance){
		String updSql="UPDATE scm_server_instance SET source_path = ?, main_args = ?,jvm_args = ?,remark = ? WHERE id = ? ";
        Object[] params = {
				instance.getSourcePath(),
				instance.getMainArgs(),
				instance.getJvmArgs(),
				instance.getRemark(),
				instance.getId()
		};
		int count = this.getJdbcTemplate().update(updSql,params );
		return count>0;
	}
	
	class SmcServerInstanceRowMapper implements RowMapper<ServerInstance>{


		public ServerInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
			ServerInstance dto = new ServerInstance();
			dto.setId(rs.getString("id"));
			dto.setMainClass(rs.getString("main_class"));
			dto.setServerName(rs.getString("server_name"));
			dto.setMainArgs(rs.getString("main_args"));
			dto.setJvmArgs(rs.getString("jvm_args"));
			dto.setCreateBy(rs.getString("create_by"));
			dto.setProjectName(rs.getString("project_name"));
			dto.setSourcePath(rs.getString("source_path"));
			dto.setRemark(rs.getString("remark"));
            dto.setServerMode(rs.getString("server_mode"));
			try{
				dto.setPermissions(rs.getInt("permissions"));
			}catch(Exception e){
				
			}
			return dto;
		}
		
	}
}
