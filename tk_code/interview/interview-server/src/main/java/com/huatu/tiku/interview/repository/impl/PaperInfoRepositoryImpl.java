package com.huatu.tiku.interview.repository.impl;

import com.huatu.tiku.interview.constant.WXStatusEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by junli on 2018/4/13.
 */
@Repository
public class PaperInfoRepositoryImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> listForLimit(int page, int pageSize, int type, String paperName) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT ")
                .append(" p.id AS 'id',p.type AS 'type',p.paper_name AS 'paperName',p.biz_status AS 'bizStatus', ")
                .append(" c.`name` AS 'className',a.username AS 'userName' ");
        sql.append(getBaseSql(type, paperName));
        sql.append(" limit ").append((page - 1) * pageSize).append(",").append(pageSize);
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql.toString());
        return maps;
    }

    public long count(int type, String paperName) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" SELECT count(p.id) AS 'count' ")
                .append(getBaseSql(type, paperName));
        Map<String, Object> map = jdbcTemplate.queryForMap(sql.toString());
        return Long.valueOf(map.get("count").toString());

    }

    private static String getBaseSql(int type, String paperName) {
        StringBuilder sql = new StringBuilder(256);
        sql.append(" FROM ")
                .append(" t_paper_info p ")
                .append(" LEFT JOIN t_class_info c ON p.class_id = c.id AND p.exam_type = 1 AND c.`status` = ").append(WXStatusEnum.Status.NORMAL.getStatus())
                .append(" LEFT JOIN t_admin a ON p.creator = a.id AND a.`status` = ").append(WXStatusEnum.Status.NORMAL.getStatus())
                .append(" WHERE p.`status` = 1 ")
                .append(" AND p.`exam_type` = 1 ");
        if (type != 0) {
            sql.append(" AND p.`type` = ").append(type);
        }
        if (StringUtils.isNotBlank(paperName)) {
            sql.append(" AND p.paper_name like '%").append(paperName).append("%' ");
        }
        sql.append(" order by p.`id` DESC ");
        return sql.toString();
    }
}
