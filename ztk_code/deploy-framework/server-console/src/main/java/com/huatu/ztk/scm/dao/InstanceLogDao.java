/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.base.BaseDao;
import com.huatu.ztk.scm.dto.InstanceLog;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 表scm_instance_log对应的dao
 * @author wenpingliu
 * @version v 0.1 15/6/16 11:41 wenpingliu Exp $$
 */
@Repository("instanceLogDao")
public class InstanceLogDao  extends BaseDao {

    public boolean insert(InstanceLog instanceLog){
        String insertSql="INSERT INTO scm_instance_log(instance_id,project_id,user_id,oper_type,log_message,create_date,create_by)"
                         + "VALUES(?,?,?,?,?,?,?)";
        Object[] params = {
          instanceLog.getInstanceId(),
          instanceLog.getProjectId(),
          instanceLog.getUserId(),
          instanceLog.getOperType(),
          instanceLog.getLogMessage(),
          new Date(),
          instanceLog.getCreateBy(),
        };
        int count=this.getJdbcTemplate().update(insertSql,params);
        return count>0;
    }

    /**
     * 根据instanceId 查询其拥有的实例ip
     * @param instanceId
     * @return
     */
    public List<InstanceLog> queryByInstanceId(String instanceId){
        String sql = "select * from scm_instance_log where instance_id = ? order by create_date desc limit 20";
        String[] params = {instanceId};
        List<InstanceLog> list=this.getJdbcTemplate().query(sql, params, new InstanceLogRowMapper());
        if(list==null){
            list = new ArrayList<InstanceLog>(0);
        }
        return list;
    }

    class InstanceLogRowMapper implements RowMapper<InstanceLog> {
        public InstanceLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            InstanceLog instanceLog = new InstanceLog();
            instanceLog.setId(rs.getInt("id"));
            instanceLog.setInstanceId(rs.getString("instance_id"));
            instanceLog.setProjectId(rs.getInt("project_id"));
            instanceLog.setUserId(rs.getInt("user_id"));
            instanceLog.setOperType(rs.getInt("oper_type"));
            instanceLog.setLogMessage(rs.getString("log_message"));
            instanceLog.setCreateBy(rs.getString("create_by"));
            instanceLog.setCreateDate(rs.getTimestamp("create_date"));
            return instanceLog;
        }

    }
}
