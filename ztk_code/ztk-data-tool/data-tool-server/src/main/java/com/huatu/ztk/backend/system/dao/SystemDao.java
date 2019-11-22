package com.huatu.ztk.backend.system.dao;

import com.huatu.ztk.backend.system.bean.NsTextMsg;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.util.DateFormat;
import com.huatu.ztk.backend.util.FuncStr;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ht on 2016/11/21.
 */
@Repository
public class SystemDao {
    private static final Logger logger = LoggerFactory.getLogger(SystemDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取消息列表
     * @param msg
     * @return
     */
    public List<NsTextMsg> query(NsTextMsg msg){
        String sql = "SELECT * FROM ns_textmsg WHERE 1=1  ";
        if(msg!=null){
          if(StringUtils.isNoneEmpty(msg.getTitle())){
              sql+=" and title like '%"+msg.getTitle()+"%'";
          }
            if(msg.getCatgory()!=0){
                sql+=" and catgory ="+msg.getCatgory();
            }
        }
        sql+=" order by deadline desc limit 0,100 ";
        final List<NsTextMsg> msgList = jdbcTemplate.query(sql, new MessageRowMapper());
        if (CollectionUtils.isEmpty(msgList)) {
            return  new ArrayList<>();
        }
        return msgList;
    }

    /**
     * 删除系统消息
     * @param id
     * @return
     */
    public boolean delete(int id){
        boolean result=true;
        String sql = "delete from ns_textmsg WHERE tid= "+id;
        try {
            jdbcTemplate.execute(sql);
        }catch (Exception e){
            e.printStackTrace();
            result=false;
        }
        return  result;
    }

    /**
     * 添加消息
     * @param msg
     * @return
     */
    public int insert(NsTextMsg msg){
        String sql="insert into ns_textmsg(tid,msgtype,usetype,title,content,createtime,lastedittime,deadline,catgory) VALUES (?,?,?,?,?,?,?,?,?)";
        Object[] params = {
                msg.getId(),
                msg.getMsgType(),
                msg.getUseType(),
                msg.getTitle(),
                msg.getContent(),
                msg.getCreateTime(),
                msg.getLasteditTime(),
                msg.getDeadLine(),
                msg.getCatgory()
        };
        final int count = jdbcTemplate.update(sql, params);
        logger.info("insert ns_textmsg={}",msg);
        return count;
    }

    /**更改用户信息
     *  更改
     * @param msg
     * @return
     */
    public int update(NsTextMsg msg){
           String sql="update ns_textmsg set title=?,content=?,lastedittime=?,deadline=?,catgory=? where tid=?";
             Object[] params =new Object[] {
                    msg.getTitle(),
                    msg.getContent(),
                    DateFormat.getCurrentDate(),
                    DateFormat.strTOYMD(msg.getDeadLine()),
                    msg.getCatgory(),
                    msg.getId()
            };
        final int count = jdbcTemplate.update(sql, params);
        logger.info("update ns_textmsg={}",msg);
        return count;
    }

    /**
     * 获取系统消息
     * @param id
     * @return
     */
    public NsTextMsg findById(int id){
        String sql="SELECT * FROM ns_textmsg WHERE tid=? ";
        Object[] params={id};
        final List<NsTextMsg> msgList =jdbcTemplate.query(sql,params,new MessageRowMapper());
        NsTextMsg nsTextMsg=null;
        if (CollectionUtils.isNotEmpty(msgList)) {
            nsTextMsg=(NsTextMsg)msgList.get(0);
        }
        return nsTextMsg;
    }
    class MessageRowMapper implements RowMapper<NsTextMsg> {
        public NsTextMsg mapRow(ResultSet rs, int rowNum) throws SQLException {
            final NsTextMsg msg = NsTextMsg.builder()
                    .id(rs.getInt("tid"))
                    .msgType(rs.getString("msgtype"))
                    .useType(rs.getInt("usetype"))
                    .title(rs.getString("title"))
                    .content(rs.getString("content"))
                    .deadLine(DateFormat.dateTostr(rs.getDate("deadline")))
                    .catgory(rs.getInt("catgory"))
                    .build();
            return msg;
        }
    }

}
