package com.huatu.ztk.user.dao;

import com.huatu.ztk.user.bean.Activity;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * 活动dao层
 */
@Repository
public class ActivityDao {
    @Autowired
    private JdbcTemplate mobileJdbcTemplate;

    /**
     * 查询手机端活动列表
     * @return
     */
    public List<Activity> queryMobileactivity(){
        //is_delete = 0表示未删除，online_status = 1表示上线,
        String sql = "select * " +
                "from ns_activity WHERE is_delete = 0 AND online_status = 1 ORDER BY create_time DESC ";
        return mobileJdbcTemplate.query(sql, new ActivityRowMapper());
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public Activity findById(long id) {
        String sql = "select * from ns_activity where id=?";
        Activity Activity = null;
        final List<Activity> activity = mobileJdbcTemplate.query(sql, new ActivityRowMapper(), id);
        if (CollectionUtils.isNotEmpty(activity)) {
            Activity = activity.get(0);
        }
        return Activity;
    }

    /**
     * 获得活动未读个数
     * @param readTimeString
     * @return
     */
    public int getUnReadActCount(String readTimeString) {
        //获得活动开始时间大于用户查看活动列表时间的活动个数
        String sql = String.format("SELECT COUNT(1) FROM ns_activity WHERE begin_date>'%s' and " +
                "is_delete = 0 AND online_status = 1",readTimeString);
        return mobileJdbcTemplate.queryForObject(sql, Integer.class);
    }

    /**
     * 点击量增加
     * @param aid 活动id
     */
    public void pvadd(long aid) {
        String updateSql = "UPDATE ns_activity SET activity_pv = activity_pv + 1 WHERE id = ?";
        Object[] params = {
          aid
        };
        mobileJdbcTemplate.update(updateSql,params);
    }


    class ActivityRowMapper implements RowMapper<Activity>{

        @Override
        public Activity mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Activity activity = Activity.builder().id(rs.getLong("id"))
                    .name(rs.getString("activity_name"))
                    .image("http://ns.huatu.com/advertising/pictures/" + rs.getString("activity_img"))
                    .link(rs.getString("activity_url"))
                    .createTime(rs.getTimestamp("create_time").getTime())
                    .beginTime(rs.getTimestamp("begin_date").getTime())
                    .endTime(rs.getTimestamp("end_date").getTime())
                    .pv(rs.getLong("activity_pv"))
                    .build();

            return activity;
        }
    }
}
