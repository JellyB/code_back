package com.huatu.ztk.user.daoPandora.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;


/**
 * 描述：返回appVersion最新版信息
 *
 * @author biguodong
 * Create time 2018-09-06 下午5:42
 **/
@Slf4j
public class AppVersionProvider {

    public String findLastedVersion(@Param("terminal") int terminal, @Param("appName") int appName){
        StringBuilder sql = new StringBuilder(512);
        sql
                .append("SELECT id, app_name as appName, terminal, app_version as appVersion, " +
                        "version_count as versionCount, update_type as updateType, " +
                        "update_channel as updateChannel, update_mode as updateMode, " +
                        "message, file_or_url as fileOrUrl, file_md5 as fileMd5, release_type as releaseType ")
                .append(" from app_version ")
                .append(" where terminal = ")
                .append(terminal)
                .append(" AND app_name = ")
                .append(appName)
                .append(" AND `status` = 1 ")
                .append(" ORDER BY version_count, create_time DESC ")
                .append(" LIMIT 1");
        //log.info(sql.toString());
        return sql.toString();
    }
}
