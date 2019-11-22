package com.huatu.ztk.user.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.user.bean.SimpleUserDto;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.bean.UserNameAndRegFromEntity;
import com.huatu.ztk.user.bean.UserSearchRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 用户dao层
 * Created by shaojieyue
 * Created time 2016-05-06 09:14
 */

@Repository
public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int insert(UserDto userDto) {
        long l = System.currentTimeMillis();
        logger.info(" userDao insert {},time = {}",userDto.getMobile(),System.currentTimeMillis());
        String sql = "INSERT v_qbank_user(uname,reg_phone,reg_mail,status,BB102,BB103,BB108,BB105,FB1Z5,subject,area,nick,reg_from,FB1Z1) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                userDto.getNick(),
                StringUtils.isBlank(userDto.getRegFrom()) ? "0" : userDto.getRegFrom(),
                userDto.getDeviceToken()
        };
        final int count = jdbcTemplate.update(sql, params);
        
        logger.info("insert user={},expendTime = {},time = {}", userDto,System.currentTimeMillis() - l,System.currentTimeMillis());
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
     * 根据userId查询用户
     *
     * @return
     */
    public UserDto findByUserId(String userId) {
        String sql = "SELECT * FROM v_qbank_user qu where PUKEY=?";
        String[] params = {
                userId
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
        //logger.info("查询sql = {},参数 = {}",sql,account);
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
     * 更新最后登录时间 & 用户头像
     *
     * @param userId 用户id
     */
    public void updateLastLoginTimeAndAvatar(long userId, String avatar) {
        logger.info("update user last_login_time, avatar, userId = {}", userId);
        Object[] params = {
                System.currentTimeMillis() / 1000,
                avatar,
                userId
        };
        String updateSql = "UPDATE v_qbank_user set last_login_time=?, avatar=? where pukey=?";
        jdbcTemplate.update(updateSql, params);
    }
    
    /**
     * 更新用户表ucenterId
     * @param userId
     * @param ucenterId
     */
    public void updateBB105(long userId,long ucenterId) {
        logger.info("update user BB105, userId = {}", userId);
        Object[] params = {
        		ucenterId,
                userId
        };
        String updateSql = "UPDATE v_qbank_user set BB105=? where pukey=?";
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
     * 修改昵称和个性签名
     *
     * @param userId    用户id
     * @param nickname  昵称
     * @param signature 个性签名
     */
    public void modifyUserAll(long userId, String nickname, String signature) {
        logger.info("modify nickname and signature. userId = {}, nickname = {},signature={}", userId, nickname, signature);
        String updatesql = "update v_qbank_user set nick=?,EB102=? where PUKEY=?";
        Object[] params = {
                nickname,
                signature,
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
     * 统计注册数量
     */
    public List<Map<String, Object>> countNum(String regFrom, long beginTime, long endTime) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT COUNT(1)  as 'num',");
        sql.append(" FROM_UNIXTIME(v_qbank_user.BB103,'%Y-%m-%d') as 'time'");
        sql.append(" FROM ");
        sql.append(" v_qbank_user ");
        sql.append("  WHERE ");
        sql.append(" BB102 = 1");
        if (StringUtils.isNotBlank(regFrom)) {
            sql.append(" AND reg_from = ").append(regFrom);
        }
        sql.append(" AND BB103 >=" + beginTime);
        sql.append(" AND BB103 <=" + endTime);
        sql.append(" GROUP BY FROM_UNIXTIME(v_qbank_user.BB103,'%Y-%m-%d')");
        sql.append(" order BY FROM_UNIXTIME(v_qbank_user.BB103,'%Y-%m-%d')");
        logger.info("countNum sql = {}", sql.toString());
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql.toString());
        return maps;
    }

    public void modifySignature(long userId, String signature) {
        logger.info("modify signature. userId = {}, signature = {}", userId, signature);
        String updatesql = "update v_qbank_user set EB102=? where PUKEY=?";
        Object[] params = {
                signature,
                userId
        };
        jdbcTemplate.update(updatesql, params);
    }

    /**
     * 查询PHP 中的错误数据
     */
    public List<UserDto> getPHPBadData(int page) {
        String sql = "select * from `v_qbank_user` where `uname` LIKE '0%'  and last_login_time!=0 AND `reg_phone` IS NULL AND LENGTH(`uname`) = 12 ORDER BY v_qbank_user.PUKEY limit " + (page - 1) * 1000 + ",1000 ";
        final List<UserDto> userDtos = jdbcTemplate.query(sql, new UserRowMapper());
        return userDtos;
    }

    public Object findUserListForRegFromAndTime(UserSearchRequest userSearchRequest) {
        StringBuilder sql = new StringBuilder();
        sql.append("select uname as name,BB103 as regTime from v_qbank_user where BB102 = 1 ");
        if (StringUtils.isNotEmpty(userSearchRequest.getRegFrom())) {
            sql.append("and reg_from= ").append(userSearchRequest.getRegFrom());
        }
        sql.append(" and BB103> ").append(userSearchRequest.getBegin());
        sql.append(" and BB103< ").append(userSearchRequest.getEnd());
        sql.append(" ORDER BY regTime DESC LIMIT ");
        sql.append(userSearchRequest.getPage()).append(",").append(userSearchRequest.getSize());
        return jdbcTemplate.query(sql.toString(), new UserNameAndRegFrom());
    }

    class UserNameAndRegFrom implements RowMapper<UserNameAndRegFromEntity> {

        @Override
        public UserNameAndRegFromEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return UserNameAndRegFromEntity.builder()
                    .name(resultSet.getString("name"))
                    .regFrom(resultSet.getInt("regTime"))
                    .build();
        }
    }


    /**
     * 批量查询简单用户信息
     *
     * @param parameters
     * @return
     */
    public List<SimpleUserDto> getUserInfoByUserNameBatch(MapSqlParameterSource parameters) {
        List<SimpleUserDto> simpleUserDtos = Lists.newArrayList();
        try {
            NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
            String sql = "SELECT qu.PUKEY as PUKEY, qu.uname as uname FROM v_qbank_user qu where qu.uname IN (:uNames)";
            List<SimpleUserDto> list = namedParameterJdbcTemplate.query(sql, parameters, new SimpleUserRowMapper());
            simpleUserDtos.addAll(list);
            return simpleUserDtos;
        } catch (Exception e) {
            logger.error("batch user info error", e);
        }
        return simpleUserDtos;

    }


    /**
     * 根据用户id批量查询用户信息
     * @return
     */
    public List<UserDto> findByIds(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return null;
        }

        String sql = "SELECT * FROM v_qbank_user qu where pukey in ("+ ids + ")";

        final List<UserDto> userDtos = jdbcTemplate.query(sql, new UserRowMapper());
        return userDtos;
    }
    
    /**
     * 修改用户用户名
     * @param userId
     * @param userName
     */
    public void modifyUname(long userId, String userName) {
        logger.info("modify username. userId = {}, username = {}", userId, userName);
        String updatesql = "update v_qbank_user set uname=? where PUKEY=?";
        Object[] params = {
        		userName,
                userId
        };
        jdbcTemplate.update(updatesql, params);
    }


    class SimpleUserRowMapper implements RowMapper<SimpleUserDto> {
        @Override
        public SimpleUserDto mapRow(ResultSet resultSet, int i) throws SQLException {
            SimpleUserDto simpleUserDto = new SimpleUserDto();
            simpleUserDto.setUserId(resultSet.getInt("PUKEY"));
            simpleUserDto.setUserName(resultSet.getString("uname"));
            return simpleUserDto;
        }
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
                    .ucenterId(rs.getLong("BB105"))//由BB108转为BB105 之前写叉劈了 by scott
                    .avatar(rs.getString("avatar"))
                    .isRobot(Boolean.valueOf(rs.getString("FB1Z3")))
                    .signature(rs.getString("EB102"))
                    .regFrom(rs.getString("reg_from"))
                    .build();
            return userDto;
        }
    }

    /**
     * 根据用户名/手机号查询
     *
     * @param params 用户名/手机号
     * @return 用户列表
     */
    public List<UserDto> findByUsernameOrMobile(List<String> params) {
        List<UserDto> userDtos = Lists.newArrayListWithExpectedSize(params.size());

        params.forEach(param -> {
            String sql = "SELECT * FROM v_qbank_user qu where uname = '" + param + "' or reg_phone = '" + param + "'";

            UserDto userDto = jdbcTemplate.queryForObject(sql, new UserRowMapper());

            userDtos.add(userDto);
        });

        return userDtos;
    }
}
