package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.base.BaseDao;
import com.huatu.ztk.scm.dto.Project;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 表smc_project dao
 * @author shaojieyue
 * @date 2013-07-12 17:19:58
 */
@Repository("projectDao")
public class ProjectDao extends BaseDao {

	/**
	 * 查询所有可部署的project
	 * @return
	 */
	public List<Project> getAllProject(String userCode) {
		String sql ="select distinct sp.*,spp.permissions "+
				"from scm_project sp left join scm_project_permissions spp on ( sp.id=spp.project_id and spp.user_code = ?) order by sp.project_name ";
		String[] params = {
				userCode
		};
		List<Project> scmProject = this.getJdbcTemplate().query(sql,params,new ScmProjectRowMapper());
		return scmProject;
	}

    public List<Project> getAllProject() {
        String sql ="select distinct sp.*,3 as permissions from scm_project sp";
        List<Project> scmProject = this.getJdbcTemplate().query(sql,new ScmProjectRowMapper());
        return scmProject;
    }
	
	/**
	 * 根据用户账户查找project
	 * @param userCode
	 * @return
	 */
	public List<Project> queryProject(String userCode){
		String querySql = "select distinct sp.*,spp.permissions from scm_project sp ,scm_project_permissions spp "+
				" where sp.id=spp.project_id "+
				" and spp.user_code = ? and spp.permissions >1 order by sp.project_name";
		String[] params = {
				userCode
		};
		List<Project> list = this.getJdbcTemplate().query(querySql,params, new ScmProjectRowMapper());
		if(list == null){
			list=new ArrayList(0);
		}
		
		return list;
	}
	
	public Project get(String id){
		String sql ="SELECT * FROM scm_project where id=?";
		String[] params = {
				id
		};
		List<Project> scmProject  = this.getJdbcTemplate().query(sql,params,new ScmProjectRowMapper());
		if(scmProject!=null&&scmProject.size()>0){
			return scmProject.get(0);
		}
		return null;
	}

    public Project getByName(String projectName){
        String sql ="SELECT * FROM scm_project where project_name=?";
        String[] params = {
          projectName
        };
        List<Project> scmProject  = this.getJdbcTemplate().query(sql,params,new ScmProjectRowMapper());
        if(scmProject!=null&&scmProject.size()>0){
            return scmProject.get(0);
        }
        return null;
    }
	
	/**
	 * 更新project的当前tag号
	 * @param projectId
	 * @param tag
	 * @return
	 */
	public boolean updateAllTag(String projectId, String tag){
		String updateSql="update scm_project t set t.current_tag = ?,t.newest_tag = ? where t.id=?";
		int count = this.getJdbcTemplate().update(updateSql, tag, tag, projectId);
		return count>0;
	}

    public boolean updateTestBranch(String projectId,String branch){
        String updateSql="update scm_project t set t.test_branch = ? where t.id=?";
        int count = this.getJdbcTemplate().update(updateSql, branch, projectId);
        return count>0;
    }

    public boolean updateDevelopBranch(String projectId,String branch){
        String updateSql="update scm_project t set t.develop_branch = ? where t.id=?";
        int count = this.getJdbcTemplate().update(updateSql, branch, projectId);
        return count>0;
    }

    public boolean updateCurrentTag(String projectId, String tag){
        String updateSql="update scm_project t set t.current_tag = ? where t.id=?";
        int count = this.getJdbcTemplate().update(updateSql, tag,projectId);
        return count>0;
    }
	
	/**
	 * 根据git地址获取Project
	 * @param gitUrl
	 * @return
	 */
	public Project getByGitUrl(String gitUrl){
		String sql ="SELECT * FROM scm_project where git_url=?";
		String[] params = {
				gitUrl
		};
		List<Project> scmProject  = this.getJdbcTemplate().query(sql,params,new ScmProjectRowMapper());
		if(scmProject!=null&&scmProject.size()>0){
			return scmProject.get(0);
		}
		return null;
	}
	
	public boolean insert(Project project){
		String insertSql = "INSERT INTO scm_project (git_url,project_name, remark,create_by) "+
				" VALUES (?,?,?, ?)";
		Object[] args = {
				project.getGitUrl(),
				project.getProjectName(),
				StringUtils.trimToEmpty(project.getRemark()),
				project.getCreateBy()
		};
		int count=this.getJdbcTemplate().update(insertSql, args);
		return count>0;
	}
	
	class ScmProjectRowMapper implements RowMapper<Project>{

		public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
			Project scmProject = new Project();
			scmProject.setId(rs.getInt("id"));
			scmProject.setProjectName(rs.getString("project_name"));
			scmProject.setRemark(rs.getString("remark"));
			scmProject.setCreateBy(rs.getString("create_by"));
			scmProject.setCurrentTag(rs.getString("current_tag"));
			scmProject.setGitUrl(rs.getString("git_url"));
            scmProject.setNewestTag(rs.getString("newest_tag"));
            scmProject.setTestBranch(rs.getString("test_branch"));
            scmProject.setDevelopBranch(rs.getString("develop_branch"));


            try{
				scmProject.setPermissions(rs.getInt("permissions"));
			}catch(Exception e){
				
			}
			return scmProject;
		}
		
	}
}
