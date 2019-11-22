package com.huatu.ztk.scm.dao;

import com.huatu.ztk.scm.base.BaseDao;
import com.huatu.ztk.scm.dto.Ticket;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository("ticketDao")
public class TicketDao extends BaseDao {

    public List<Ticket> queryTicketsByServerName(String serverName){
        String sql = "select * from scm_ticket where server_name=? order by id desc limit 10";
        Object[] params = {
          serverName
        };
        List<Ticket> list=this.getJdbcTemplate().query(sql, params, new TicketRowMapper());
        if(list==null){
            list = new ArrayList<Ticket>(0);
        }
        return list;
    }

    public Ticket getTicketById(int id){
        String sql ="SELECT * FROM scm_ticket where id= ?";
        Object[] params ={id};
        List<Ticket> tickets = this.getJdbcTemplate().query(sql,params,new TicketRowMapper());
        if(tickets!=null&&tickets.size()>0){
            return tickets.get(0);
        }
        return null;
    }

    public List<Ticket> queryTicketsByPage(int page ,int pageSize){
        int start = (page - 1) * pageSize;
        String sql = "select * from scm_ticket order by id desc limit ?,?";
        Object[] params = {
          start,
          pageSize
        };
        List<Ticket> list=this.getJdbcTemplate().query(sql, params, new TicketRowMapper());
        if(list==null){
            list = new ArrayList<Ticket>(0);
        }
        return list;
    }

    public List<Ticket> queryTicketsByPageAndStatus(int page ,int pageSize,int status){
        int start = (page - 1) * pageSize;
        String sql = "select * from scm_ticket where status=? order by id desc limit ?,?";
        Object[] params = {
          status,
          start,
          pageSize
        };
        List<Ticket> list=this.getJdbcTemplate().query(sql, params, new TicketRowMapper());
        if(list==null){
            list = new ArrayList<Ticket>(0);
        }
        return list;
    }

    public boolean updateStatus(int ticketId,int status){
        String insertSql="update scm_ticket set status=? where id=?";
        Object[] params = {
          status,
          ticketId
        };
        int count=this.getJdbcTemplate().update(insertSql,params);
        return count>0;
    }

    public boolean insert(Ticket ticket){
        String insertSql="INSERT INTO scm_ticket(type,project_name,server_name,module,release_log,status,tester,create_by,deployer,branch)"
                         + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        Object[] params = {
          ticket.getType(),
          ticket.getProjectName(),
          ticket.getServerName(),
          ticket.getModule(),
          ticket.getReleaseLog(),
          ticket.getStatus(),
          ticket.getTester(),
          ticket.getCreateBy(),
          ticket.getDeployer(),
          ticket.getBranch()
        };
        int count=this.getJdbcTemplate().update(insertSql,params);
        return count>0;
    }

    class TicketRowMapper implements RowMapper<Ticket>{
        @Override
        public Ticket mapRow(ResultSet rs, int rowNum) throws SQLException {
            Ticket ticket = new Ticket();
            ticket.setId(rs.getInt("id"));
            ticket.setServerName(rs.getString("server_name"));
            ticket.setCreateBy(rs.getString("create_by"));
            ticket.setDeployer(rs.getString("deployer"));
            ticket.setTester(rs.getString("tester"));
            ticket.setReleaseLog(rs.getString("release_log"));
            ticket.setStatus(rs.getInt("status"));
            ticket.setProjectName(rs.getString("project_name"));
            ticket.setType(rs.getInt("type"));
            ticket.setBranch(rs.getString("branch"));
            ticket.setModule(rs.getString("module"));
            return ticket;
        }
    }


}
