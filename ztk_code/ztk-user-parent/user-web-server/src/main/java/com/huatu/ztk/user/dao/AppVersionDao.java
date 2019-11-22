package com.huatu.ztk.user.dao;

import com.huatu.ztk.user.bean.AppVersion;
import com.huatu.ztk.user.common.VersionRedisKey;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * app 版本管理
 * Created by shaojieyue
 * Created time 2016-11-21 16:14
 */

@Repository
public class AppVersionDao {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionDao.class);


    @Resource(name = "coreRedisTemplate")
    private ValueOperations valueOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询终端下的最新版本
     *
     * @param client
     * @param category
     * @return
     */
    public AppVersion findLatestVersion(int client, int category) {
        String sql = "SELECT * FROM v_app_version WHERE client=? AND catgory=? ORDER BY version_Count DESC limit 1";
        final List<AppVersion> appVersions = jdbcTemplate.query(sql, new Object[]{client, category}, new AppVersionRowMapper());

        AppVersion appVersion = null;
        if (CollectionUtils.isNotEmpty(appVersions)) {
            appVersion = appVersions.get(0);
        }

        return appVersion;
    }


    /**
     * 查询指定版本信息
     *
     * @param client
     * @param versionCount
     * @return
     */
    public AppVersion findVersion(int client, int versionCount, int category) {
        StringBuilder sb = new StringBuilder(VersionRedisKey.CURRENT_VERSION_OBJ_PREFIX).append(client).append("_").append(versionCount).append("_").append(category);
      //  logger.info("version redis key:{}", sb.toString());
        Object object = valueOperations.get(sb.toString());
        if (object != null) {
            return (AppVersion) object;
        }

        String sql = "SELECT * FROM v_app_version WHERE client=? and version_Count=? and catgory=? ";
        final List<AppVersion> appVersions = jdbcTemplate.query(sql, new Object[]{client, versionCount, category}, new AppVersionRowMapper());

        AppVersion appVersion = null;
        if (CollectionUtils.isNotEmpty(appVersions)) {
            appVersion = appVersions.get(0);
            valueOperations.set(sb.toString(), appVersion, 10, TimeUnit.MINUTES);
        }
        return appVersion;
    }

    class AppVersionRowMapper implements RowMapper<AppVersion> {

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
                    .build();
            return appVersion;
        }
    }
}
