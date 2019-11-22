package com.huatu.ztk.user.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.user.bean.Feedback;
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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 意见反馈dao层
 * BB102为删除标识,1为正常状态,0为删除,,,
 * Created by shaojieyue
 * Created time 2016-06-06 18:24
 */

@Repository
public class FeedbackDao {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert(Feedback feedback, String enviromentStr, int catgory) {
        logger.info("insert feedback,data={}", JsonUtil.toJson(feedback));
        String sql = "INSERT v_user_feedback(title,content,contact_mail,uid,BB103,uname,FB1Z2,imgs,EB104,catgory,type,BB102,issolve) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Object[] params = {
                StringUtils.substring(
                        feedback.getContent(), 0, 10),
                feedback.getContent(),
                feedback.getContacts(),
                feedback.getUid(),
                feedback.getCreateTime() / 1000,
                feedback.getUname(),
                enviromentStr,
                feedback.getImgs(),
                StringUtils.isNotBlank(feedback.getLog()) ? feedback.getLog() : "-",
                catgory,
                feedback.getType(),
                1,
                1
        };
        jdbcTemplate.update(sql, params);
    }

    public Long getFeedbackCount(Integer type, Integer processed, Integer isSolve, String content, Long id, long start, long end) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(1) from v_user_feedback where BB102 = 1 ");
        LinkedList<Object> paramList = new LinkedList<>();
        if (id != -1) {
            sql.append(" and PUKEY =  " + id);
        }
        if (type != 0) {
            sql.append(" and type =  " + type);
        }

        //已回复
        if (processed == 1) {
            sql.append(" and reply_num > 0 ");
            //未回复
        } else if (processed == 2) {
            sql.append(" and reply_num = 0 ");
        }
        if (isSolve != 0) {
            //已关闭
            sql.append(" and issolve = -1");
        }

        //内容匹配
        if (StringUtils.isNotEmpty(content)) {
            sql.append(" and content like '%" + content + "%' ");
            paramList.add(content);
        }

        //时间匹配
        if (start > 0) {
            sql.append(" and  BB103 > " + start);
        }
        if (end > 0) {
            sql.append(" and  BB103 < " + end);
        }
        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class);

        return count;

    }

    public List<Feedback> getFeedbacksByTypeAndPage(Integer type, Integer processed, Integer size, Integer page, Integer isSolve, String content, Long id, long start, long end) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT PUKEY,title,message_ids,content,contact_mail,uid,uname,type,BB103,FB1Z2,reply_num,imgs,issolve,BB107,EB104,BB106 from v_user_feedback where BB102 = 1 ");
        LinkedList<Object> paramList = new LinkedList<>();
        if (id != -1) {
            sql.append(" and PUKEY =  " + id);
        }

        if (type != 0) {
            sql.append(" and type =  " + type);
        }

        //已回复
        if (processed == 1) {
            sql.append(" and reply_num > 0 ");
            //未回复
        } else if (processed == 2) {
            sql.append(" and reply_num = 0 ");
        }
        if (isSolve != 0) {
            //已关闭
            sql.append(" and issolve = -1");
        }

        //内容匹配
        if (StringUtils.isNotEmpty(content)) {
            sql.append(" and content like '%" + content + "%' ");
        }

        //时间匹配
        if (start > 0) {
            sql.append(" and  BB103 > " + start);
        }
        if (end > 0) {
            sql.append(" and  BB103 < " + end);
        }
        sql.append(" order by BB103 DESC limit ?, ?");
        paramList.add(size * (page - 1));
        paramList.add(size);
        List<Feedback> list = jdbcTemplate.query(sql.toString(), paramList.toArray(), new FeedbackRowMapper());

        return list;
    }

    public void delFeedback(Integer id, String modifier) {
        String sql = "UPDATE `v_user_feedback` SET `BB102`='0',BB107 = ?,BB106 = ? WHERE `PUKEY`= " + id;
        int update = jdbcTemplate.update(sql, new Object[]{modifier, System.currentTimeMillis() / 1000});
        logger.info("更新条数: {}", update);
    }

    public Feedback findById(int id) {
        String sql = "SELECT PUKEY,title,content,contact_mail,uid,uname,type,BB103,FB1Z2,reply_num,imgs,message_ids,issolve,BB107,EB104,BB106 from v_user_feedback where PUKEY = ?";
        Object[] params = {id};
        List<Feedback> list = jdbcTemplate.query(sql, params, new FeedbackRowMapper());

        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }


    public List<Feedback> findByIds(List<Integer> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        StringBuffer sql = new StringBuffer();
        String idParams = ids.stream().map(id -> String.valueOf(id)).collect(Collectors.joining(","));
        sql.append("SELECT PUKEY,title,content,contact_mail,uid,uname,type,BB103,FB1Z2,reply_num,imgs,message_ids,issolve,BB107,EB104,BB106 from v_user_feedback where PUKEY  in (");
        sql.append(idParams);
        sql.append(")");
        logger.info("findByIds", sql.toString());
        List<Feedback> list = jdbcTemplate.query(sql.toString(), new FeedbackRowMapper());
        logger.info("反馈内容是:{}", JsonUtil.toJson(list));
        return list;

    }

    public int updateFeedbackByReply(int id, String msgId, String modifier) {
        String sql = "update v_user_feedback set message_ids= ?,reply_num = reply_num+1,BB107 = ? ,BB106 = ? where PUKEY=?";
        return jdbcTemplate.update(sql, new Object[]{msgId, modifier, System.currentTimeMillis() / 1000, id});
    }

    public void setSolve(Integer id, Integer solve, String modifier) {
        String sql = "update v_user_feedback set issolve = ?,BB107 = ?,BB106 = ? where PUKEY = ?";
        jdbcTemplate.update(sql, new Object[]{solve, modifier, System.currentTimeMillis() / 1000, id});
    }


    class LongMapper implements RowMapper<Long> {

        @Override
        public Long mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getLong("count");
        }
    }

    class FeedbackRowMapper implements RowMapper<Feedback> {

        @Override
        public Feedback mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Feedback feedback = Feedback.builder()
                    .id(rs.getInt("PUKEY"))
                    .uname(rs.getString("uname"))
                    .content(rs.getString("content"))
                    .contacts(rs.getString("contact_mail"))
                    .uid(rs.getLong("uid"))
                    .imgs(rs.getString("imgs"))
                    .type(rs.getInt("type"))
                    .createTime(rs.getLong("BB103"))
                    .environmentMap(rs.getString("FB1Z2"))
                    .replyNum(rs.getInt("reply_num"))
                    .msgIds(rs.getString("message_ids"))
                    .isSolve(rs.getInt("issolve"))
                    .modifier(rs.getString("BB107"))
                    .pushLog(rs.getString("EB104"))
                    .modifyTime(rs.getLong("BB106"))
                    .build();
            return feedback;
        }
    }
}
