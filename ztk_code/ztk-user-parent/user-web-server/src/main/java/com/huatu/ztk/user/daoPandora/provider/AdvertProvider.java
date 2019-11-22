package com.huatu.ztk.user.daoPandora.provider;

import com.huatu.tiku.common.AdvertEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 广告管理
 */

@Slf4j
public class AdvertProvider {

    /**
     * 多条件查询广告图
     */
    public String findAdvert(int category, int type, int newVersion, int appType) {
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT * from advert");
        sql.append(" WHERE status = 1 AND a = 1");
        if (type > 0) {
            sql.append(" AND type = ").append(type);
        }

        if (category > 0) {
            sql.append(" AND catgory = ").append(category);
        }

        if (newVersion >= 0) {
            sql.append(" AND new_version = ").append(newVersion);
        }

        if (appType > 0) {
            sql.append(" AND app_type in (0,").append(appType).append(")");
        }

        /**
         * 组装时间筛选条件
         */
        long nowDate = new Date().getTime();
        sql.append(" AND on_line_time <= '").append(nowDate).append("' ");
        sql.append(" AND off_line_time >= '").append(nowDate).append("' ");

        sql.append(" ORDER BY `index` DESC,id DESC ");
        log.info(" 广告 sql = {}, category:{}, type:{}, newVersion:{}, appType:{}", sql, category, type, newVersion, appType);
        return sql.toString();
    }


    /**
     * 查询 m 站广告轮播图
     */
    public String findMAdvert() {
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT * from advert");
        sql.append(" WHERE status = 1 AND m = 1");

        long nowDate = new Date().getTime();
        sql.append(" AND on_line_time <= '").append(nowDate).append("' ");
        sql.append(" AND off_line_time >= '").append(nowDate).append("' ");

        sql.append(" ORDER BY `index` DESC,id DESC ");
        log.info("m list.sql:{}", sql.toString());
        return sql.toString();
    }


    /**
     * 根据公告Id批量查询广告图
     */
    public String findByIds(@Param("ids") String ids) {
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT * from advert");
        sql.append(" WHERE id in (");
        sql.append(ids).append(") ");
        sql.append(" ORDER BY `index` DESC,id DESC ");
        //log.info(" 广告 sql = {}",sql);
        return sql.toString();
    }
}
