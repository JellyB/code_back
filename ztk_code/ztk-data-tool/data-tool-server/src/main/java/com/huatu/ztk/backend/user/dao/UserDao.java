package com.huatu.ztk.backend.user.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.user.bean.User;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shaojieyue
 * Created time 2016-11-04 16:18
 */

@Repository
public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findByAccount(String account) {
        String sql = "SELECT * FROM v_c_sys_user WHERE uname=? AND status=1";
        User user = null;
        final List<User> userList = jdbcTemplate.query(sql, new Object[]{account}, new UserRowMapper());
        if (CollectionUtils.isNotEmpty(userList)) {
            user = userList.get(0);
        }

        return user;
    }


    public User findById(long uid) {
        String sql = "SELECT * FROM v_c_sys_user WHERE PUKEY=?";
        User user = null;
        final List<User> userList = jdbcTemplate.query(sql, new Object[]{uid}, new UserRowMapper());
        if (CollectionUtils.isNotEmpty(userList)) {
            user = userList.get(0);
        }
        return user;
    }

    public List<User> findAllById(List<Integer> uids) {
        if (CollectionUtils.isNotEmpty(uids)) {
            Set<Integer> uidSet = new HashSet<>(uids);
            StringBuilder sbr = new StringBuilder();
            for (Integer uid : uidSet) {
                sbr.append(uid + ",");
            }
            String params = sbr.toString().substring(0, sbr.toString().lastIndexOf(","));
            String sql = "SELECT * FROM v_c_sys_user WHERE PUKEY in (" + params + ") ";
            return jdbcTemplate.query(sql, new UserRowMapper());
        }
        return Lists.newArrayList();
    }

    /**
     * 修改User
     * @param user
     */
    public void editUser(User user){
        String sql = "UPDATE v_c_sys_user SET last_login_ip=?,last_login_time=?,login_success_count=? WHERE PUKEY=?";
        Object[] param = {
                user.getLastLoginIp(),
                user.getLastLoginTime(),
                user.getSuccessLoginCount(),
                user.getId(),
        };
        try {
            jdbcTemplate.update(sql,param);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class UserRowMapper implements RowMapper<User> {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            final User user = User.builder()
                    .id(rs.getInt("pukey"))
                    .account(rs.getString("uname"))
                    .name(rs.getString("umark"))
                    .password(rs.getString("passwd"))
                    .successLoginCount(rs.getInt("login_success_count"))
                    .status(rs.getInt("status")).build();
            return user;
        }
    }
}
