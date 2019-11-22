package com.huatu.ztk.backend.mysql.dao;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\4\3 0003.
 */
@Repository
public class PaperSqlDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Integer> findAllPaper(){
        String sql = "select pukey from v_pastpaper_info where bb102 = 1";
        return jdbcTemplate.queryForList(sql,Integer.class);
    }

    public List<Map> findAllName(){
        String sql = "SELECT\n" +
                "\tpukey as id,\n" +
                "\tpastpaper_name as name\n" +
                "FROM\n" +
                "\tv_pastpaper_info\n" +
                "WHERE\n" +
                "\tbb102 = 1\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT\n" +
                "\t2000000+PUKEY as id,\n" +
                "\t`name` as name\n" +
                "FROM\n" +
                "\tv_testpaper_info\n" +
                "WHERE\n" +
                "\tbb102 = 1";
        return jdbcTemplate.query(sql,(rs,i)->{
            HashMap<Object, Object> map = Maps.newHashMap();
            map.put("id",rs.getInt("id"));
            map.put("name",rs.getString("name"));
            return map;
        });
    }

}
