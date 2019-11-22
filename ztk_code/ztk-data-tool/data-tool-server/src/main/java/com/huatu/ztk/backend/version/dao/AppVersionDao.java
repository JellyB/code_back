package com.huatu.ztk.backend.version.dao;

import com.huatu.ztk.backend.version.bean.AppVersion;
import com.huatu.ztk.commons.JsonUtil;
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

;

/**
 * app 版本管理
 * Created by shaojieyue
 * Created time 2016-11-21 16:14
 */

@Repository
public class AppVersionDao {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询终端下的最新版本，备用
     *
     * @param client
     * @return
     */
    public AppVersion findLatestVersion(int client) {
        String sql = "SELECT * FROM v_app_version WHERE client=? ORDER BY version_Count DESC limit 1";
        final List<AppVersion> appVersions = jdbcTemplate.query(sql, new Object[]{client}, new AppVersionRowMapper());

        AppVersion appVersion = null;
        if (CollectionUtils.isNotEmpty(appVersions)) {
            appVersion = appVersions.get(0);
        }

        return appVersion;
    }

    /**
     * 查询所有版本
     *
     * @return
     */
    public List<AppVersion> findAll() {
        String sql = "SELECT * FROM v_app_version ORDER BY catgory ASC ,client ASC , version_Count DESC ";
        List<AppVersion> versions = jdbcTemplate.query(sql, new AppVersionRowMapper());
        return versions;
    }

    /**
     * 删除版本
     *
     * @param id
     */
    public void deleteById(int id) {
        String sql = "DELETE FROM v_app_version WHERE id=" + id;
        jdbcTemplate.update(sql);
    }

    /**
     * 新建一个版本
     *
     * @param appVersion
     */
    public void insert(AppVersion appVersion) {
        logger.info("insert obj={}", JsonUtil.toJson(appVersion));
        String sql = "INSERT v_app_version (version,message,full,bulk,bulk_Md5,create_time,client,level,version_Count,update_mode,catgory) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        Object[] params = {
                appVersion.getVersion(),
                appVersion.getMessage(),
                appVersion.getFull(),
                appVersion.getBulk(),
                appVersion.getBulkMd5(),
                appVersion.getCreateTime(),
                appVersion.getClient(),
                appVersion.getLevel(),
                appVersion.getVersionCount(),
                appVersion.getUpdateMode(),
                appVersion.getCatgory()
        };
        jdbcTemplate.update(sql, params);
    }


    /**
     * 更新版本
     *
     * @param appVersion
     */
    public void update(AppVersion appVersion) {
        String sql = "update v_app_version set version=?,message = ?,full=?,bulk=?," +
                "bulk_Md5=?,create_time=?,client=?,level=?,version_Count=?,update_mode=?,catgory=? WHERE id=?";
        Object[] params = {
                appVersion.getVersion(),
                appVersion.getMessage(),
                appVersion.getFull(),
                appVersion.getBulk(),
                appVersion.getBulkMd5(),
                appVersion.getCreateTime(),
                appVersion.getClient(),
                appVersion.getLevel(),
                appVersion.getVersionCount(),
                appVersion.getUpdateMode(),
                appVersion.getCatgory(),
                appVersion.getId()
        };
        jdbcTemplate.update(sql, params);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    public AppVersion findById(int id) {
        AppVersion appVersion = null;
        String sql = "SELECT * FROM v_app_version WHERE id=?";

        List<AppVersion> list = jdbcTemplate.query(sql, new Object[]{id}, new AppVersionRowMapper());
        if (CollectionUtils.isNotEmpty(list)) {
            appVersion = list.get(0);
        }
        return appVersion;
    }

    class AppVersionRowMapper implements RowMapper<AppVersion> {

        @Override
        public AppVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
            final AppVersion appVersion = AppVersion.builder()
                    .bulk(rs.getString("bulk"))
                    .bulkMd5(rs.getString("bulk_Md5"))
                    .full(rs.getString("full"))
                    .level(rs.getInt("level"))
                    .message(rs.getString("message"))
                    .version(rs.getString("version"))
                    .versionCount(rs.getInt("version_Count"))
                    .updateMode(rs.getInt("update_mode"))
                    .id(rs.getInt("id"))
                    .client(rs.getInt("client"))
                    .createTime(rs.getTimestamp("create_time"))
                    .catgory(rs.getInt("catgory"))
                    .build();
            return appVersion;
        }
    }
}
