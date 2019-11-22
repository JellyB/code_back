package com.huatu.ztk.user.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.user.bean.UserMessage;
import com.huatu.ztk.user.common.UserMessageType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 系统消息dao
 * Created by shaojieyue
 * Created time 2016-06-17 09:11
 */

@Repository
public class SystemMessageDao {
    private static final Logger logger = LoggerFactory.getLogger(SystemMessageDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取系统消息
     * 只获取前20条
     *
     * @param cursor
     * @param size
     * @param catgory 科目
     * @return
     */
    public List<UserMessage> find(long cursor, int size, int catgory) {
        //只取未到期的消息
        String sql = "SELECT * FROM ns_textmsg WHERE catgory= ? AND  deadline >= ? ORDER BY createtime DESC limit ?,?";
        Object[] params = {
                catgory,
                DateFormatUtils.format(new Date(),"yyyy-MM-dd"),
                cursor,
                size
        };

        List<UserMessage> messages = jdbcTemplate.query(sql, params, new UserMessageRowMapper());
        if (messages == null) {
            messages = Lists.newArrayList();
        }
        return messages;
    }


    public List<UserMessage> findAll(int catgory) {
        //只取未到期的消息
        String sql = "SELECT * FROM ns_textmsg WHERE catgory= ? AND  deadline >= ? ORDER BY createtime DESC";
        Object[] params = {
                catgory,
                DateFormatUtils.format(new Date(),"yyyy-MM-dd"),
        };

        List<UserMessage> messages = jdbcTemplate.query(sql, params, new UserMessageRowMapper());
        return messages;
    }

    public UserMessage findById(long mid) {
        String sql = "SELECT * FROM ns_textmsg where tid=?";
        Object[] params = {
                mid
        };
        UserMessage userMessage = null;
        final List<UserMessage> messages = jdbcTemplate.query(sql, params, new UserMessageRowMapper());
        if (CollectionUtils.isNotEmpty(messages)) {
            userMessage = messages.get(0);
        }
        return userMessage;
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
                    .title(rs.getString("title"))
                    .content(rs.getString("content"))
                    .createTime(rs.getTimestamp("createtime").getTime())
                    .type(UserMessageType.SYSTEM_PERSONAL)
                    .build();
        }
    }
}
