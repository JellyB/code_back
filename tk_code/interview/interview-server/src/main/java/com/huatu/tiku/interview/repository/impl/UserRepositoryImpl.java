package com.huatu.tiku.interview.repository.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 学员管理
 */
@Repository
public class UserRepositoryImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> listForLimit(int page, int pageSize, String content,long classId,long areaId) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT ")
                .append("u.id,u.open_id openId,u.`name` uname,u.id_card idCard,u.key_contact keyContact,u.nation,u.phone,u.pregnancy ,u.sex,u.class_title classTitle," +
                        "temp.class_id cid,temp.`name` cname,u.area_id aid ,a.`name` aname,u.status status,u.biz_status bizStatus ");
        sql.append(getBaseSql(content, classId, areaId));
        sql.append(" limit ").append((page - 1) * pageSize).append(",").append(pageSize);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql.toString());
        return maps;
    }


    public long count(String content,long classId,long areaId) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT count(u.id) AS 'count' ")
                .append(getBaseSql( content, classId, areaId));
        Map<String, Object> map = jdbcTemplate.queryForMap(sql.toString());
        return Long.valueOf(map.get("count").toString());

    }

    private static String getBaseSql(String content,long classId,long areaId) {
        StringBuilder sql = new StringBuilder(256);
        sql.append("FROM t_user u ")
                .append("LEFT JOIN " )
                .append("(SELECT uc.open_id,c.name,uc.class_id,c.start_time,c.end_time FROM t_class_info c LEFT JOIN t_user_class uc ON c.status = 1 AND uc.class_id = c.id AND uc.status = 1) temp " )
                .append( "ON u.open_id = temp.open_id " )
                .append("LEFT JOIN t_area a ON u.area_id = a.`id`")
        .append("where u.status = 1 and u.biz_status = 2");
        if (classId != -1) {
            sql.append(" AND temp.class_id = ").append(classId);
        }
        if (areaId != -1) {
            sql.append(" AND u.area_id = ").append(areaId);
        }
        if (StringUtils.isNotEmpty(content)) {
            sql.append(" AND u.`name` like '%").append(content).append("%' ");
        }
        return sql.toString();
    }
}
