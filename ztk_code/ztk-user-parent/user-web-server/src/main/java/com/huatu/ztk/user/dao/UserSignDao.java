package com.huatu.ztk.user.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.user.bean.UserSign;
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
 * Created by linkang on 2017/10/16 下午2:39
 */
@Repository
public class UserSignDao {
    private static final Logger logger = LoggerFactory.getLogger(UserSignDao.class);



    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * @param userId
     * @param date
     * @return
     */
    public UserSign findByUidAndDate(long userId, Date date) {

        String sql = "select * from v_user_sign where uid=? and DATE_FORMAT(sign_time, '%Y%m%d') =?";

        Object[] params = {
                userId,
                DateFormatUtils.format(date,"yyyyMMdd")
        };

        List<UserSign> list = jdbcTemplate.query(sql, params, new UserSignRowMapper());

        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;

    }

    /**
     * @param userSign
     */
    public void insert(UserSign userSign) {
        logger.info("insert usersign={}", JsonUtil.toJson(userSign));

        String sql = "INSERT v_user_sign (uid, sign_time, type, sign_number) " +
                "VALUES (?, ?, ?, ?)";

        Object[] params = {
                userSign.getUid(),
                userSign.getSignTime(),
                userSign.getType(),
                userSign.getNumber()
        };

        jdbcTemplate.update(sql, params);
    }

    class UserSignRowMapper implements RowMapper<UserSign> {

        public UserSign mapRow(ResultSet rs, int rowNum) throws SQLException {
            return UserSign.builder()
                    .number(rs.getInt("sign_number"))
                    .uid(rs.getLong("uid"))
                    .signTime(rs.getTimestamp("sign_time"))
                    .type(rs.getInt("type"))
                    .build();
        }
    }
}
