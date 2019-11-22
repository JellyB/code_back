package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.dto.TicketLog;
import com.huatu.ztk.scm.base.BaseDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository("ticketLogDao")
public class TicketLogDao extends BaseDao {

    public List<TicketLog> queryTicketLogsByPage(int page , int pageSize){
        String sql = "select * from scm_ticket_log order by id desc limit 20";
        Object[] params = {};
        List<TicketLog> list=this.getJdbcTemplate().query(sql, params, new TicketLogRowMapper());
        if(list==null){
            list = new ArrayList<TicketLog>(0);
        }
        return list;
    }

    public boolean insert(TicketLog ticketlog){
        String insertSql="INSERT INTO scm_ticket_log(ticket_id,note,create_by)"
                         + "VALUES(?,?,?)";
        Object[] params = {
          ticketlog.getTicketId(),
          ticketlog.getNote(),
          ticketlog.getCreateBy(),
        };
        int count=this.getJdbcTemplate().update(insertSql,params);
        return count>0;
    }

    class TicketLogRowMapper implements RowMapper<TicketLog>{
        @Override
        public TicketLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            TicketLog ticketlog = new TicketLog();
            ticketlog.setId(rs.getInt("id"));
            ticketlog.setNote(rs.getString("note"));
            ticketlog.setCreateBy(rs.getString("create_by"));
            ticketlog.setTicketId(rs.getInt("ticket_id"));
            return ticketlog;
        }
    }


}
