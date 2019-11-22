package com.huatu.ztk.user.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.user.bean.PostAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 收货地址管理dao
 * Created by linkang on 10/11/16.
 */

@Repository
public class UserPostAddressDao {
    private static final Logger logger = LoggerFactory.getLogger(UserPostAddressDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根据userId查询所有收货地址
     *
     * @param userId
     * @return
     */
    public List<PostAddress> findAll(long userId) {
        //默认的排在第一位，其他按创建时间倒序
        String sql = "SELECT * FROM v_post_address WHERE uid= ? ORDER BY defalut ASC ,create_time DESC  ";
        Object[] params = {
                userId
        };

        return jdbcTemplate.query(sql, params, new UserPostAddressMapper());
    }


    /**
     * 根据userId查询默认收货地址
     *
     * @param userId
     * @return
     */
    public PostAddress findDefault(long userId) {
        String sql = "SELECT * FROM v_post_address WHERE uid= ? AND defalut=1";
        Object[] params = {
                userId
        };
        final List<PostAddress> results = jdbcTemplate.query(sql, params, new UserPostAddressMapper());
        PostAddress address = null;
        if (results != null && results.size() > 0) {
            address = results.get(0);
        }
        return address;
    }


    /**
     * 删除地址
     * @param userId
     * @param id
     */
    public void deleteOne(long userId, long id) {
        String sql = "DELETE FROM v_post_address WHERE uid=? AND id= ? ";
        Object[] params = {
                userId,
                id
        };
        jdbcTemplate.update(sql, params);
    }

    /**
     * 新建地址
     * @param postAddress
     */
    public void insert(PostAddress postAddress) {
        logger.info("insert address,obj={}", JsonUtil.toJson(postAddress));
        String sql = "INSERT v_post_address(uid,phone,consignee,province,city,address,defalut,create_time) VALUES (?,?,?,?,?,?,?,?)";
        Object[] params = {
                postAddress.getUid(),
                postAddress.getPhone(),
                postAddress.getConsignee(),
                postAddress.getProvince(),
                postAddress.getCity(),
                postAddress.getAddress(),
                postAddress.getDefalut(),
                postAddress.getCreateTime()
        };
        jdbcTemplate.update(sql, params);
    }

    /**
     * 更新一条地址
     * @param postAddress
     */
    public void update(PostAddress postAddress) {
        String sql = "UPDATE v_post_address SET phone = ?, consignee = ?,province = ?," +
                "city = ?,address = ? WHERE id= ?";
        Object[] params = {
                postAddress.getPhone(),
                postAddress.getConsignee(),
                postAddress.getProvince(),
                postAddress.getCity(),
                postAddress.getAddress(),
                postAddress.getId()
        };
        jdbcTemplate.update(sql, params);
    }


    /**
     *根据id设置是否为默认地址
     * @param id 地址id
     * @param value
     */
    public void setDefaultValue(long id, int value) {
        String sql = "UPDATE v_post_address SET defalut = ? WHERE id= ?";
        Object[] params = {
                value,
                id
        };
        jdbcTemplate.update(sql, params);
    }


    class UserPostAddressMapper implements RowMapper<PostAddress> {
        @Override
        public PostAddress mapRow(ResultSet rs, int i) throws SQLException {
            return PostAddress.builder()
                    .id(rs.getLong("id"))
                    .uid(rs.getLong("uid"))
                    .phone(rs.getString("phone"))
                    .consignee(rs.getString("consignee"))
                    .province(rs.getString("province"))
                    .city(rs.getString("city"))
                    .address(rs.getString("address"))
                    .defalut(rs.getInt("defalut"))
                    .createTime(rs.getTimestamp("create_time"))
                    .build();
        }
    }
}
