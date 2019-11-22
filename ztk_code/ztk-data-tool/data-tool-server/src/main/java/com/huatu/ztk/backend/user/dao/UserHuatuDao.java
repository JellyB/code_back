package com.huatu.ztk.backend.user.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.user.bean.UserDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户dao层
 * Created by shaojieyue
 * Created time 2016-05-06 09:14
 */

@Repository
public class UserHuatuDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int insert(UserDto userDto) {
        String sql = "INSERT v_qbank_user(uname,reg_phone,reg_mail,status,BB102,BB103,BB108,BB105,FB1Z5,subject,area,nick) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Object[] params = {
                userDto.getName(),
                userDto.getMobile(),
                userDto.getEmail(),
                userDto.getStatus(), 1,
                userDto.getCreateTime() / 1000,
                userDto.getMobileUserId(),
                userDto.getUcenterId(),
                userDto.getNativepwd(),
                userDto.getSubject(),
                userDto.getArea(),
                userDto.getNick()
        };
        final int count = jdbcTemplate.update(sql, params);

        logger.info("insert user={}", userDto);
        return count;
    }


    List<Object[]> paramsList = Lists.newArrayList();

    /**
     * 更新手机用户和pc的对应关系
     *
     * @param mobileUserId
     * @param id
     * @return
     */
    public synchronized int updateMobileId(long mobileUserId, long id, long ucenterId, String phone, String mail, String name) {
        logger.info("update user mapping mobileUserId={},id={}", mobileUserId, id);
        Object[] params = new Object[]{
                mobileUserId, ucenterId, phone, mail, name, id
        };
        String sql = "UPDATE v_qbank_user set nick=uname, BB108 = ?,BB105=?,reg_phone=?,reg_mail=?,uname=? where PUKEY = ?";
        final int count = jdbcTemplate.update(sql, params);
        return count;
    }

    /**
     * 查询用户
     *
     * @return
     */
    public List<UserDto> findUsers(int offset, int limit) {
        String sql = "SELECT * FROM v_qbank_user  WHERE  bb102=1 ORDER  BY pukey ASC limit ?,?";
        Integer[] params = {
                offset, limit
        };
        return jdbcTemplate.query(sql, params, new UserRowMapper());

    }

    /**
     * 查询用户
     *
     * @return
     */
    public long countUsers() {
        String sql = "SELECT COUNT(*) FROM v_qbank_user  WHERE  bb102=1";
        Integer[] params = {
        };
        return jdbcTemplate.queryForObject(sql, long.class);

    }

    /**
     * 根据uname查询用户
     *
     * @return
     */
    public UserDto findByName(String name) {
        String sql = "SELECT * FROM v_qbank_user qu where uname=?";
        String[] params = {
                name
        };
        return findMethod(sql, params);
    }


    /**
     * 通过phone,mail查询用户
     *
     * @param account
     * @return
     */
    public UserDto find(String account) {

        if (StringUtils.isBlank(account)) {
            return null;
        }
        String sql = "SELECT * FROM v_qbank_user qu where reg_phone =? or reg_mail=?";
        String[] params = {
                account, account
        };
        return findMethod(sql, params);
    }

    /**
     * 根据手机号，用户名，邮箱查询
     *
     * @param account
     * @return
     */
    public UserDto findAny(String account) {
        if (StringUtils.isBlank(account)) {
            return null;
        }
        String sql = "SELECT * FROM v_qbank_user qu where reg_phone =? or reg_mail=? or uname=?";
        String[] params = {
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

            //todo 此处主要是解决多用户问题，数据修复后可以去除
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
     * 重置密码
     *
     * @param userId
     * @param password
     */
    public void resetpwd(long userId, String password) {
        logger.info("reset password. userId = {}, password = {}", userId, password);
        String updatesql = "update v_qbank_user set FB1Z5=? where PUKEY=?";
        Object[] params = {
                password,
                userId
        };
        jdbcTemplate.update(updatesql, params);
    }

    /**
     * 更新密码和最后登录时间
     *
     * @param userId
     * @param password
     */
    public void resetPasswordAndLastLoginTime(long userId, String password) {
        logger.info("reset password. userId = {}, password = {}", userId, password);
        String updateSql = "update v_qbank_user set FB1Z5=?,last_login_time=?  where PUKEY=?";
        Object[] params = {
                password,
                System.currentTimeMillis() / 1000,
                userId
        };
        jdbcTemplate.update(updateSql, params);
    }

    /**
     * 更新用户考试科目
     *
     * @param userId 用户id
     * @param area   所属区域
     */
    public void updateSubject(long userId, int area) {
        logger.info("update user subject, userId = {}, area = {}", userId, area);
        Object[] params = {
                area,
                userId
        };
        String updatesql = "UPDATE v_qbank_user set area=? where pukey=?";
        jdbcTemplate.update(updatesql, params);
    }

    /**
     * 更新最后登录时间
     *
     * @param userId 用户id
     */
    public void updateLastLoginTime(long userId) {
        logger.info("update user last_login_time, userId = {}", userId);
        Object[] params = {
                System.currentTimeMillis() / 1000,
                userId
        };
        String updateSql = "UPDATE v_qbank_user set last_login_time=? where pukey=?";
        jdbcTemplate.update(updateSql, params);
    }

    /**
     * 根据用户id查询用户
     *
     * @param userId 用户id
     * @return 不存在则返回NULL
     */
    public UserDto findById(long userId) {
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
     * 更新用户普通信息
     *
     * @param uid      用户id
     * @param password 账户密码
     * @param nick     昵称
     * @param status   用户状态
     */
    public void updateCommon(long uid, String password, String nick, int status) {
        Object[] params = {
                password,
                nick,
                status,
                uid
        };
        String updatesql = "UPDATE v_qbank_user set FB1Z5=?,nick=?,status=? where pukey=?";
        jdbcTemplate.update(updatesql, params);
    }

    /**
     * 根据手机号查询用户信息
     *
     * @param mobile
     * @return
     */
    public UserDto findByMobile(String mobile) {
        String sql = "SELECT * FROM v_qbank_user qu where reg_phone =?";
        String[] params = {
                mobile
        };
        final List<UserDto> userDtos = jdbcTemplate.query(sql, params, new UserRowMapper());
        UserDto userDto = null;
        if (userDtos != null && userDtos.size() > 0) {
            userDto = userDtos.get(0);
        }

        return userDto;
    }

    /**
     * 根据手机号查询用户信息
     *
     * @param mobile
     * @return
     */
    public List<UserDto> findByMobiles(List<String> mobile) {
        String sql = "select * from v_qbank_user where reg_phone in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", mobile);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedJdbcTemplate.query(sql, parameters, new UserRowMapper());
    }

    /**
     * 修改昵称
     *
     * @param userId   用户id
     * @param nickname 昵称
     */
    public void modifyNickname(long userId, String nickname) {
        logger.info("modify nickname. userId = {}, nickname = {}", userId, nickname);
        String updatesql = "update v_qbank_user set nick=? where PUKEY=?";
        Object[] params = {
                nickname,
                userId
        };
        jdbcTemplate.update(updatesql, params);
    }

    /**
     * 更新注册手机号
     *
     * @param username
     * @param mobile
     */
    public void updateMobile(String username, String mobile) {
        logger.info("update mobile , username={},mobile={}", username, mobile);
        String sql = "update v_qbank_user set reg_phone=? WHERE uname=?";
        Object[] params = {
                mobile,
                username
        };
        jdbcTemplate.update(sql, params);
    }

    /**
     * 上传头像更新url
     *
     * @param userId
     * @param url
     */
    public void updateAvatar(long userId, String url) {
        logger.info("update avatar,userId={},url={}", userId, url);
        String sql = "update v_qbank_user set avatar =? where PUKEY=?";
        Object[] params = {
                url,
                userId
        };
        jdbcTemplate.update(sql, params);
    }

    /**
     * lizhenjuan
     *
     * @param ids
     * @return
     */
    public List<UserDto> findByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        //注册时间大于某一时间
        // String sql = "select * from v_qbank_user where pukey in (:ids) and BB103>" + onlineTime;
        String sql = "select * from v_qbank_user where pukey in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedJdbcTemplate.query(sql, parameters, new UserRowMapper());
    }

    public List<UserDto> findByNames(List<String> unames) {
        String sql = "select * from v_qbank_user where uname in (:ids)";
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", unames);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedJdbcTemplate.query(sql, parameters, new UserRowMapper());
    }

    class UserRowMapper implements RowMapper<UserDto> {

        @Override
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
