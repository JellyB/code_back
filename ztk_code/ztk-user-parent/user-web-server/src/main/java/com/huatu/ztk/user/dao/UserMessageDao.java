package com.huatu.ztk.user.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.user.bean.UserMessage;
import com.huatu.ztk.user.common.UserMessageType;
import com.huatu.ztk.user.utils.ZlibCompressUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by shaojieyue
 * Created time 2016-06-17 09:10
 */

@Repository
public class UserMessageDao {
    private static final Logger logger = LoggerFactory.getLogger(UserMessageDao.class);

    @Autowired
    private JdbcTemplate mobileJdbcTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取个人所有信息
     * 只获取前20条
     *
     * @param userId
     * @return
     */
    public List<UserMessage> find(long userId) {
        String sql = "SELECT * FROM ns_usermsg where userid=? AND  deadline >= ? ORDER BY createtime DESC limit 0,10";
        Object[] params = {
                userId,
                DateFormatUtils.format(new Date(), "yyyy-MM-dd")
        };

        List<UserMessage> messages = mobileJdbcTemplate.query(sql, params, new UserMessageRowMapper());
        if (messages == null) {
            messages = Lists.newArrayList();
        }
        return messages;
    }

    public UserMessage findById(long mid) {
        String sql = "SELECT * FROM ns_usermsg where tid=?";
        Object[] params = {
                mid
        };
        UserMessage userMessage = null;
        final List<UserMessage> messages = mobileJdbcTemplate.query(sql, params, new UserMessageRowMapper());
        if (CollectionUtils.isNotEmpty(messages)) {
            userMessage = messages.get(0);
        }
        return userMessage;
    }

    /**
     * 反馈回复信息
     *
     * @param userid
     * @return
     */
    public List<UserMessage> getUserFeedBackMsg(long userid) {
        String sql = "SELECT" +
                "  m.id AS tid," +
                "  mpr.recv_uid AS userid," +
                "  m.descrp  AS title," +
                "  m.content," +
                "  m.init_time AS createtime" +
                " FROM msg_person_rel mpr LEFT JOIN msg m ON m.id = mpr.id" +
                " WHERE mpr.recv_uid = ? and mpr.status=1 and m.message_type=2 " +
                " ORDER BY m.init_time DESC LIMIT 0, 5; ";
        Object[] params = {
                userid
        };
        List<UserMessage> feedbackList = jdbcTemplate.query(sql, params, new UserFeedBackMasMapper());
        if (feedbackList == null) {
            feedbackList = Lists.newArrayList();
        }
        return feedbackList;
    }


    /**
     * 查询用户反馈回复信息V2
     *
     * @param userid
     * @return
     */
    public List<UserMessage> getUserFeedBackMsgV2(long userid, long start, int end) {
        String sql = "SELECT" +
                "  m.id AS tid," +
                "  mpr.recv_uid AS userid," +
                "  m.descrp  AS title," +
                "  m.content," +
                "  m.init_time AS createtime" +
                " FROM msg_person_rel mpr LEFT JOIN msg m ON m.id = mpr.id" +
                " WHERE mpr.recv_uid = ? and mpr.status=1 and m.message_type=2 " +
                " ORDER BY m.init_time DESC LIMIT ?, ?; ";
        Object[] params = {
                userid, start, end
        };
        List<UserMessage> feedbackList = jdbcTemplate.query(sql, params, new UserFeedBackMasMapper());
        if (feedbackList == null) {
            feedbackList = Lists.newArrayList();
        }
        return feedbackList;
    }


    /**
     * 单个反馈回复信息
     *
     * @param mid
     * @return
     */
    public UserMessage findUserFeedBackMsgById(long mid) {
        String sql = "SELECT" +
                "  m.id AS tid," +
                "  mpr.recv_uid AS userid," +
                "  m.descrp  AS title," +
                "  m.content," +
                "  m.init_time AS createtime" +
                " FROM msg_person_rel mpr LEFT JOIN msg m ON m.id = mpr.id" +
                " WHERE mpr.id = ? ";
        Object[] params = {
                mid
        };
        List<UserMessage> rets = jdbcTemplate.query(sql, params, new UserFeedBackMasMapper());

        UserMessage userMessage = null;
        if (CollectionUtils.isNotEmpty(rets)) {
            userMessage = rets.get(0);
        }
        return userMessage;
    }

    public int insertMsg(String content, String title) {
        String sql = "INSERT INTO msg (content,resp,descrp,url," +
                "message_type,init_time,end_time,tombstone," +
                "level,status,area_id)" +
                "VALUES(?,?,?,?," +
                "?,?,?,?," +
                "?,?,?);";
        content = ZlibCompressUtils.compress("<p>" + content + "</p>");
        logger.info("压缩后的content：{}", content);
        Object[] params = {
                content, "", title, "", 2, System.currentTimeMillis() / 1000, 2147483647, 0, 0, 1, ""
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            java.sql.PreparedStatement ps = con.prepareStatement(sql, new String[]{"content", "resp", "descrp", "url",
                    "message_type", "init_time", "end_time", "tombstone",
                    "level", "status", "area_id"});
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        logger.info("新插入的消息id={}", id);
        return id;
    }

    public int insertMsgRel(int id, long uid) {
        logger.info("插入的消息用户关系记录，id={}", id);
        String sql = "INSERT INTO msg_person_rel (id, init_time, end_time, recv_uid, send_uid, status, tombstone, level, area_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] param = {id, System.currentTimeMillis() / 1000, 2147483647, uid, 0, 1, 0, 0, 0};
        return jdbcTemplate.update(sql, param);
    }


    class UserMessageRowMapper implements RowMapper<UserMessage> {


        /**
         * Implementations must implement this method to map each row of data
         * in the ResultSet. This method should not call {@code next()} on
         * the ResultSet; it is only supposed to map values of the current row.
         *
         * @param rs     the ResultSet to map (pre-initialized for the current row)
         * @param rowNum the number of the current row
         * @return the result object for the current row
         * @throws SQLException if a SQLException is encountered getting
         *                      column values (that is, there's no need to catch SQLException)
         */
        @Override
        public UserMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return UserMessage.builder()
                    .id(rs.getLong("tid"))
                    .uid(rs.getLong("userid"))
                    .title(rs.getString("title"))
                    .content(rs.getString("content"))
                    .createTime(rs.getTimestamp("createtime").getTime())
                    .type(UserMessageType.MESSAGE_PERSONAL)
                    .build();
        }

    }

    class UserFeedBackMasMapper implements RowMapper<UserMessage> {
        @Override
        public UserMessage mapRow(ResultSet rs, int i) throws SQLException {
            String content = rs.getString("content");
            content = ZlibCompressUtils.uncompress(content);

            return UserMessage.builder()
                    .id(rs.getLong("tid"))
                    .uid(rs.getLong("userid"))
                    .title("【问题反馈】" + rs.getString("title"))
                    .content(StringUtils.trimToEmpty(content))
                    .createTime(rs.getLong("createtime") * 1000)
                    .type(UserMessageType.MESSAGE_PERSONAL)
                    .build();
        }
    }

    /**
     * 批量查询
     *
     * @param msgIds
     * @return
     */
    public List<UserMessage> findUserFeedBackMsgByIds(List<Integer> msgIds) {

        if (CollectionUtils.isEmpty(msgIds)) {
            return Lists.newArrayList();
        }
        String idParams = msgIds.stream().map(id -> String.valueOf(id)).collect(Collectors.joining(","));
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT m.id AS tid,mpr.recv_uid AS userid,m.descrp  AS title,m.content,m.init_time AS createtime");
        sql.append(" FROM msg_person_rel mpr LEFT JOIN msg m ON m.id = mpr.id WHERE mpr.id in ( ");
        sql.append(idParams);
        sql.append(")");
        logger.info("sql是:{}", sql.toString());
        List<UserMessage> result = jdbcTemplate.query(sql.toString(), new UserFeedBackMasMapper());
        return result;
    }

}
