package com.huatu.ztk.user.dao;

import com.huatu.ztk.user.bean.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 移动端dao
 * Created by shaojieyue
 * Created time 2016-05-11 17:48
 */

@Repository
public class MobileUserDao {
    private static final Logger logger = LoggerFactory.getLogger(MobileUserDao.class);

    @Autowired
    private JdbcTemplate mobileJdbcTemplate;

    public List<UserDto> findForPage(long startId, int size){
        String sql = "SELECT * FROM ns_users WHERE id>? ORDER BY id ASC limit 0,?";//升序排
        Object[] params = {
                startId,size
        };
        final java.util.List<UserDto> userDtos = mobileJdbcTemplate.query(sql, params, new UserRowMapper());
        return userDtos;
    }

    class UserRowMapper implements RowMapper<UserDto> {

        public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            Date join_date = rs.getDate("join_date");
            if (join_date == null) {
                join_date = rs.getDate("last_login");
            }
            if (join_date == null) {
                join_date = new Date(System.currentTimeMillis());
            }
            final long udid = rs.getLong("udid");

            final String passport_mobile = rs.getString("passport_mobile");
            final String password = rs.getString("password");

            final UserDto userDto = UserDto.builder()
                    .createTime(join_date.getTime())
                    .email(rs.getString("passport_email"))
                    .id(rs.getLong("id"))
                    .mobile(passport_mobile)
                    .name(rs.getString("username"))
                    .ucenterId(udid)
                    .password(password)
                    .status(1).build();

            return userDto;
        }
    }
    /**
     * 获取移动端用户信息
     * @return
     */
    public UserDto getUserByPcId(long pcId){
        String sql="select * from ns_users where udid=? ";
        Object[] params={pcId};
        UserDto userDto=null;
        List<UserDto> userList= mobileJdbcTemplate.query(sql,params,new UserRowMapper());
        if(userList!=null&&userList.size()>0){
            userDto=(UserDto)userList.get(0);
        }
        return userDto;
    }
}
