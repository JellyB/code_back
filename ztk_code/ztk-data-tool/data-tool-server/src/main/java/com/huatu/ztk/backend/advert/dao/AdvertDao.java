package com.huatu.ztk.backend.advert.dao;

import com.huatu.ztk.backend.advert.bean.Advert;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by renwenlong on 2016/11/16.
 */
@Repository
public class AdvertDao {
    private static final Logger logger = LoggerFactory.getLogger(AdvertDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取所有广告列表
     *
     * @return
     */
    public List<Advert> getAllAds() {
        String sql = "SELECT * FROM v_advert ORDER BY `create_time` DESC ";
        final List<Advert> adverts = jdbcTemplate.query(sql, new AdvertRowMapper());
        if (CollectionUtils.isEmpty(adverts)) {
            return new ArrayList<>();
        }
        return adverts;
    }

    /**
     * 根据科目获取广告列表
     *
     * @param catgory
     * @return
     */
    public List<Advert> getAdsByCatgory(int catgory) {
        String sql = "SELECT * FROM v_advert WHERE catgory = ? ORDER BY `create_time` DESC ";
        Object[] param = {catgory};
        final List<Advert> adverts = jdbcTemplate.query(sql, param, new AdvertRowMapper());
        return adverts;
    }

    public List<Advert> getAllByType(int type,int catgory){
        String sql = "SELECT * FROM v_advert WHERE type = ? and catgory = ? ORDER BY `create_time` DESC ";
        Object[] param = {type};
        final List<Advert> adverts = jdbcTemplate.query(sql, param, new AdvertRowMapper());
        return adverts;
    }

    /**
     * 根据科目和广告类型查询广告列表
     *
     * @param catgory
     * @param type
     * @return
     */
    public List<Advert> getAdsByCatgoryAndType(int catgory, int type) {
        String sql = "SELECT * FROM v_advert WHERE catgory = ? AND type = ? ORDER BY `TYPE` DESC,`INDEX` DESC ";
        Object[] param = {catgory, type};
        final List<Advert> adverts = jdbcTemplate.query(sql, param, new AdvertRowMapper());
        return adverts;
    }

    /**
     * 根据id获取广告相关信息
     *
     * @param id
     * @return
     */
    public Advert findById(long id) {
        String sql = "SELECT * FROM v_advert WHERE id=? ";
        Object[] param = {id};
        final List<Advert> adverts = jdbcTemplate.query(sql, param, new AdvertRowMapper());
        if (CollectionUtils.isNotEmpty(adverts)) {
            return adverts.get(0);
        }
        return new Advert();
    }

    /**
     * 上传广告图片
     *
     * @param advert
     * @return
     */
    public void upload(Advert advert) {
        //    int index = getAllAds().size() + 1;
        Object[] params = {
                advert.getImage(),
                advert.getCatgory(),
                advert.getType(),
                advert.getTitle(),
                advert.getTarget(),
                advert.getParams(),
                advert.getStatus(),
                advert.getIndex(),
                advert.getNewVersion(),
                advert.getAppType(),
                advert.getOnlineTime(),
                advert.getOfflineTime()
        };
        String saveSql = "INSERT INTO v_advert(image,catgory,type,title,target,params,status,`index`,new_version,app_type,on_line_time,off_line_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(saveSql, params);
        //TODO 感觉需要做个缓存吧到时候再说
        logger.info("upload advert success");
    }

    /**
     * 修改广告的展示状态
     *
     * @param status
     * @return
     */
    public void modStatusById(Integer status, long id) {
        String modSql = "UPDATE v_advert set status=? WHERE id = ?";
        Object[] params = {status, id};
        jdbcTemplate.update(modSql, params);
        logger.info("status mod success,id={}", id);
    }

    /**
     * 更新广告信息
     *
     * @param advert
     * @return
     */
    public void update(Advert advert) {
        Object[] params = {
                getAllAds().stream().mapToLong(ad -> ad.getId()).max().getAsLong() + 1,
                advert.getImage(),
                advert.getTitle(),
                advert.getTarget(),
                advert.getCatgory(),
                advert.getType(),
                advert.getParams(),
                advert.getNewVersion(),
                advert.getAppType(),
                advert.getIndex(),
                advert.getOnlineTime(),
                advert.getOfflineTime(),
                advert.getId()
        };
        String updateSql = "UPDATE v_advert set id=?,image=?,title=?,target=?,catgory=?,type=?,params=?,new_version=?,app_type=?,`index`=?,on_line_time=?,off_line_time=? WHERE id=?";
        jdbcTemplate.update(updateSql, params);
        logger.info("update success,id={}", advert.getId());
    }

    /**
     * 删除广告
     *
     * @param id
     * @return
     */
    public void deleteById(long id) {
        String delSql = "DELETE FROM v_advert WHERE id=?";
        Object[] param = {id};
        jdbcTemplate.update(delSql, param);
        logger.info("delete success,id={}", id);
    }

//    /**
//     * 更新首页弹出广告(对应的id要进行更新)
//     *
//     * @param advert
//     */
//    public void updatePopupAdvert(Advert advert) {
//        Object[] params = {
//                getAllAds().parallelStream().mapToLong(ad -> ad.getId()).max().getAsLong() + 1,
//                advert.getImage(),
//                advert.getTitle(),
//                advert.getTarget(),
//                advert.getCatgory(),
//                advert.getType(),
//                advert.getParams(),
//                advert.getId()
//        };
//        String updateSql = "UPDATE v_advert set id=?,image=?,title=?,target=?,catgory=?,type=?,params=? WHERE id=?";
//        jdbcTemplate.update(updateSql, params);
//        logger.info("updatePopupAdvert success");
//    }

    private class AdvertRowMapper implements RowMapper<Advert> {
        @Override
        public Advert mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Advert advert = Advert.builder().id(rs.getInt("id"))
                    .image(rs.getString("image"))
                    .title(rs.getString("title"))
                    .target(rs.getString("target"))
                    .params(rs.getString("params"))
                    .status(rs.getInt("status"))
                    .catgory(rs.getInt("catgory"))
                    .type(rs.getInt("type"))
                    .index(rs.getInt("index"))
                    .newVersion(rs.getInt("new_version"))
                    .createTime(rs.getTimestamp("create_time"))
                    .appType(rs.getInt("app_type"))
                    .onlineTime(rs.getLong("on_line_time"))
                    .offlineTime(rs.getLong("off_line_time"))
                    .build();
            return advert;

        }
    }
}
