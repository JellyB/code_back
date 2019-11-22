package com.huatu.ztk.user.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.user.bean.UcenterBind;
import com.huatu.ztk.user.bean.UcenterMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ucenterDao
 * Created by linkang on 7/11/16.
 */

@Repository
public class UcenterDao {
    private static final Logger logger = LoggerFactory.getLogger(UcenterDao.class);

    @Autowired
    private JdbcTemplate ucenterJdbcTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * ucenter保存用户信息
     *
     * @param ucenterMember
     * @return 返回带id的UcenterMember对象
     */
    public UcenterMember saveMember(UcenterMember ucenterMember) {
        insertDto(ucenterMember);
        UcenterMember dto = findMemberByUsername(ucenterMember.getUsername());

        logger.info("save member : {}", JsonUtil.toJson(dto));
        return dto;
    }


    public int insertDto(UcenterMember ucenterMember) {
        String sql = "INSERT uc_members (username,password,email,myid,myidkey,regip,regdate," +
                "lastloginip,lastlogintime,salt,secques,appid,credit)" +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Object[] params = {
                ucenterMember.getUsername(),
                ucenterMember.getPassword(),
                ucenterMember.getEmail(),
                ucenterMember.getMyid(),
                ucenterMember.getMyidkey(),
                ucenterMember.getRegip(),
                ucenterMember.getRegdate(),
                ucenterMember.getLastloginip(),
                ucenterMember.getLastlogintime(),
                ucenterMember.getSalt(),
                ucenterMember.getSecques(),
                ucenterMember.getAppid(),
                ucenterMember.getCredit()
        };

        final int count = ucenterJdbcTemplate.update(sql, params);
        return count;
    }


    /**
     * 根据username查找用户
     *
     * @param username
     * @return
     */
    public UcenterMember findMemberByUsername(String username) {
        String sql = "SELECT * FROM uc_members WHERE username = ? limit 1";
        String[] params = {username};
        return findObject(sql, params, new UcenterMemberMapper());
    }


    /**
     * 根据email
     *
     * @param username
     * @return
     */
    public UcenterMember findMemberByEmail(String username) {
        String sql = "SELECT * FROM uc_members WHERE email = ? limit 1";
        String[] params = {
                username
        };
        return findObject(sql, params, new UcenterDao.UcenterMemberMapper());
    }

    /**
     * @param username
     * @return
     */
    public UcenterMember findMemberByNameAndEmail(String username) {
        String sql = "SELECT * FROM uc_members WHERE username = ? OR email = ? limit 1";
        String[] params = {
                username,
                username
        };
        return findObject(sql, params, new UcenterMemberMapper());
    }


    /**
     * 根据手机号查用户绑定信息
     *
     * @param mobile
     * @return
     */
    public UcenterBind findBind(String mobile) {
        String sql = "SELECT * FROM common_user_bd WHERE phone = ? limit 1";
        String[] params = {mobile};
        return findObject(sql, params, new UcenterBindMapper());
    }

    /**
     * 根据手机号查用户绑定信息
     */
    public List<UcenterBind> findBindByMobileList(List<String> mobileList) {
        String searchSql = mobileList.stream()
                .map(mobile -> "'" + mobile + "'")
                .collect(Collectors.joining(","));
        String sql = "SELECT * FROM common_user_bd WHERE phone in (" + searchSql + ")";
        return ucenterJdbcTemplate.query(sql, new UcenterBindMapper());
    }


    /**
     * @param account
     * @return
     */
    public UcenterBind findAnyBind(String account) {
        String sql = "SELECT * FROM common_user_bd WHERE phone = ? OR username = ? OR email = ? limit 1";
        String[] params = {
                account,
                account,
                account
        };
        return findObject(sql, params, new UcenterBindMapper());
    }


    private <T> T findObject(String sql, String[] params, RowMapper<T> mapper) {
        final List<T> results = ucenterJdbcTemplate.query(sql, params, mapper);
        T ret = null;
        if (results != null && results.size() > 0) {
            ret = results.get(0);
        }
        return ret;
    }

    /**
     * 更新用户密码
     */
    public void updateUserPwd(String username, String newPassword, String salt) {
        String sql = "UPDATE uc_members SET password = ? , salt = ? WHERE username = ?";
        Object[] params = {
                newPassword,
                salt,
                username
        };

        ucenterJdbcTemplate.update(sql, params);
    }

    /**
     * uc绑定
     *
     * @param ucenterBind
     */
    public void ucbind(UcenterBind ucenterBind) {
        String sql = "INSERT common_user_bd (userid,username,email,phone,bd) VALUES (?,?,?,?,?)";
        Object[] params = {
                ucenterBind.getUserid(),
                ucenterBind.getUsername(),
                ucenterBind.getEmail(),
                ucenterBind.getPhone(),
                ucenterBind.getBd()
        };
        ucenterJdbcTemplate.update(sql, params);
    }


    /**
     * 测试---
     *
     * @param phone
     */
    public void delUser(String phone) {
        UcenterBind ucenterBind = findAnyBind(phone);
        if (ucenterBind != null) {
            String username = ucenterBind.getUsername();
            String sql = "DELETE FROM common_user_bd WHERE username=?";
            Object[] params = {
                    username
            };
            ucenterJdbcTemplate.update(sql, params);

            String sql2 = "DELETE FROM uc_members WHERE username=?";
            Object[] params2 = {
                    username
            };
            ucenterJdbcTemplate.update(sql2, params2);
        }

        String sql3 = "DELETE FROM v_qbank_user WHERE reg_phone=?";
        Object[] params3 = {
                phone
        };
        jdbcTemplate.update(sql3, params3);


        String sql4 = "DELETE FROM uc_members WHERE username=?";
        Object[] params4 = {
                phone
        };
        ucenterJdbcTemplate.update(sql4, params4);
    }

    /**
     * 更新绑定表的手机号
     *
     * @param uname
     * @param mobile
     */
    public void updateMobile(String uname, String mobile) {
        String sql = "update common_user_bd set phone=? where username=?";
        ucenterJdbcTemplate.update(sql, new Object[]{mobile, uname});
    }


    class UcenterMemberMapper implements RowMapper<UcenterMember> {
        @Override
        public UcenterMember mapRow(ResultSet rs, int rowNumber) throws SQLException {
            final UcenterMember ucenterMember = UcenterMember.builder()
                    .uid(rs.getInt("uid"))
                    .username(rs.getString("username"))
                    .password(rs.getString("password"))
                    .email(rs.getString("email"))
                    .salt(rs.getString("salt"))
                    .build();
            return ucenterMember;
        }
    }

    class UcenterBindMapper implements RowMapper<UcenterBind> {
        @Override
        public UcenterBind mapRow(ResultSet rs, int i) throws SQLException {
            final UcenterBind ucenterBind = UcenterBind.builder()
                    .id(rs.getInt("id"))
                    .username(rs.getString("username"))
                    .userid(rs.getInt("userid"))
                    .phone(rs.getString("phone"))
                    .email(rs.getString("email"))
                    .bd(rs.getString("bd"))
                    .build();
            return ucenterBind;
        }
    }

    /*************以下部分为第三方接口提供代码****************/
    /**
     * 分页查询
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @param pageNum   页号
     * @param pageSize  页面大小
     * @return 查询数据
     */
    public List<Map<String, Object>> pageData(long beginTime, long endTime, int pageNum, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT um.username,um.appid,um.regdate ");
        sql.append(" FROM ");
        sql.append(bulidSelectBody());
        sql.append("  WHERE ");
        sql.append(buildWhereCacse(beginTime, endTime));
        sql.append(" order by um.regdate ");
        sql.append(" Limit ").append((pageNum - 1) * pageSize).append(",").append(pageSize);
        logger.info("pageData sql = {}", sql.toString());
        List<Map<String, Object>> mapList = ucenterJdbcTemplate.queryForList(sql.toString());
        logger.info("分页查询数据,beginTime = {},endTime = {},pageNum = {},pageSize={},data = {}",
                beginTime, endTime, pageNum, pageSize,
                mapList.size()
        );
        return mapList;
    }

    /**
     * 计算数量
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 返回查询结果集
     */
    public long countNum(long beginTime, long endTime) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT COUNT(1)");
        sql.append(" FROM ");
        sql.append(bulidSelectBody());
        sql.append("  WHERE ");
        sql.append(buildWhereCacse(beginTime, endTime));
        logger.info("countNum sql = {}", sql.toString());
        Long num = ucenterJdbcTemplate.queryForObject(sql.toString(), Long.class);
        return num;
    }

    private static String bulidSelectBody() {
        StringBuilder sql = new StringBuilder();
        sql.append(" uc_members um ");
        //sql.append("inner join common_user_bd cub on um.username = cub.username AND cub.phone is not null");
        return sql.toString();
    }

    private static String buildWhereCacse(long beginTime, long endTime) {
        StringBuilder sql = new StringBuilder();
        sql.append(" 1= 1 ");
        if (0 != beginTime) {
            sql.append(" AND um.regdate >= ").append(beginTime);
        }
        if (0 != endTime) {
            sql.append(" AND um.regdate <= ").append(endTime);
        }
        return sql.toString();
    }
    
    public List<UcenterBind> findBindList(String mobile) {
        String sql = "SELECT * FROM common_user_bd WHERE phone = ? ";
        String[] params = {mobile};
        return ucenterJdbcTemplate.query(sql, params,new UcenterBindMapper());
    }
    
    public List<Map<String, Object>> findBindList(int uid) {
        String sql = "SELECT phone FROM common_user_bd GROUP BY phone HAVING count(phone) =2 ";
       // Object[] params = {uid};
        return ucenterJdbcTemplate.queryForList(sql);
    }


    /**
     * 删除uc绑定表
     * @param id
     */
	public void delUserById(int id) {
        String sql = "DELETE FROM common_user_bd WHERE id = ?";
        Object[] params = {
        		id
        };
        ucenterJdbcTemplate.update(sql, params);
	}

}
