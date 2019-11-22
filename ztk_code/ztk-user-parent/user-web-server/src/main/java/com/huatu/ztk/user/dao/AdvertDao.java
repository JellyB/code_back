package com.huatu.ztk.user.dao;

import com.huatu.ztk.user.bean.Advert;
import org.apache.commons.collections.CollectionUtils;
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
 * 广告dao
 * Created by shaojieyue
 * Created time 2016-06-29 14:50
 */
@Repository
public class AdvertDao {
    private static final Logger logger = LoggerFactory.getLogger(AdvertDao.class);

    @Autowired
    private JdbcTemplate mobileJdbcTemplate;

    /**
     * 查询手机端广告列表
     * @return
     */
    public List<Advert> queryMobileAdverts(){
        String sql = "select * from ns_advert_manage where adver_position=1 and is_delete=0 order by adver_index";
        final List<Advert> adverts = mobileJdbcTemplate.query(sql, new AdvertRowMapper());
        return adverts;
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public Advert findById(long id) {
        String sql = "select * from ns_advert_manage where id=?";
        Advert advert = null;
        final List<Advert> adverts = mobileJdbcTemplate.query(sql, new AdvertRowMapper(), id);
        if (CollectionUtils.isNotEmpty(adverts)) {
            advert = adverts.get(0);
        }
        return advert;
    }

    class AdvertRowMapper implements RowMapper<Advert>{

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
        public Advert mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Advert advert = Advert.builder().id(rs.getLong("id"))
                    .image("http://ns.huatu.com/advertising/pictures/" + rs.getString("imagepath"))
                    .link("http://ns.huatu.com/u/v1/users/bc/"+rs.getLong("id")+"/detail")
                    .content(rs.getString("content"))
                    .createTime(rs.getTimestamp("updatetime").getTime())    //addtime不准确，以更新时间为准
                    .title(rs.getString("title"))
                    .build();
            return advert;
        }
    }
}
