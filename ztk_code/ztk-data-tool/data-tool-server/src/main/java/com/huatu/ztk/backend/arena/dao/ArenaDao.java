package com.huatu.ztk.backend.arena.dao;

import com.huatu.ztk.arena.bean.ArenaUserSummary;
import com.huatu.ztk.user.bean.UserDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 竞技dao
 * Created by linkang on 11/18/16.
 */

@Repository
public class ArenaDao {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 根据用户id查询用户
     *
     * @param userId 用户id
     * @return 不存在则返回NULL
     */
    public UserDto findUserById(long userId) {
        UserDto userDto = null;
        String sql = "SELECT * FROM v_qbank_user qu where qu.PUKEY=?";
        Object[] params = {
                userId
        };
        final List<UserDto> userDtos = jdbcTemplate.query(sql, params, new UserRowMapper());
        if (userDtos != null && userDtos.size() > 0) {
            userDto = userDtos.get(0);
        }
        return userDto;
    }

    /**
     * 根据手机号，用户名，邮箱查询,id
     *
     * @param account
     * @return
     */
    public UserDto findUserByAny(String account) {
        if (StringUtils.isBlank(account)) {
            return null;
        }
        String sql = "SELECT * FROM v_qbank_user qu where reg_phone =? or reg_mail=? or uname=? or pukey=?";
        String[] params = {
                account,
                account,
                account,
                account
        };
        return findMethod(sql, params);
    }

    private UserDto findMethod(String sql, String[] params) {
        final List<UserDto> userDtos = jdbcTemplate.query(sql, params, new UserRowMapper());
        UserDto userDto = null;
        if (CollectionUtils.isNotEmpty(userDtos)) {
            if (userDtos.size() > 1) {//多用户
                for (UserDto userDto1 : userDtos) {
                    if (userDto1.getMobileUserId() > 0) {
                        userDto = userDto1;
                    }
                }
            }
            if (userDto == null) {
                userDto = userDtos.get(0);
            }
        }
        return userDto;
    }


    /**
     * 根据uid查询该用户所有的竞技统计
     *
     * @return
     */
    public List<ArenaUserSummary> findSummaryListByUserId(long userId) {
        Criteria criteria = Criteria.where("uid").is(userId);
        Query query = new Query(criteria).with(new Sort(Sort.Direction.DESC, "_id"));

        return mongoTemplate.find(query, ArenaUserSummary.class);
    }


    class UserRowMapper implements RowMapper<UserDto> {

        public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            final UserDto userDto = UserDto.builder()
                    .createTime(rs.getLong("bb103") * 1000)
                    .email(rs.getString("reg_mail"))
                    .id(rs.getInt("PUKEY"))
                    .mobile(rs.getString("reg_phone"))
                    .name(rs.getString("uname"))
                    .nativepwd(rs.getString("FB1Z5"))
                    .password(rs.getString("passwd"))
                    .status(rs.getInt("status"))
                    .nick(rs.getString("nick"))
                    .subject(rs.getInt("subject"))
                    .area(rs.getInt("area"))
                    .ucenterId(rs.getLong("BB108"))
                    .avatar(rs.getString("avatar"))
                    .isRobot(Boolean.valueOf(rs.getString("FB1Z3")))
                    .build();
            return userDto;
        }
    }

}
