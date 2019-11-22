package com.huatu.ztk.backend.mysql.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangqp on 2018\4\3 0003.
 */
@Repository
public class MultiSqlDao {
    private final static Logger logger = LoggerFactory.getLogger(MultiSqlDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<Integer,List<Integer>>> findByIds(List<Integer> ids){
        if(CollectionUtils.isEmpty(ids)){
            return Lists.newArrayList();
        }
        String sql = "select * from v_multi_question where pukey in (:ids)";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return  namedJdbcTemplate.query(sql, parameters,(rs,i)->{
            Map<Integer,List<Integer>> map = Maps.newHashMap();
            int multiId = rs.getInt("pukey");
            List<Integer> list = Lists.newArrayList();
            for(int j= 1;j<=10;j++ ){
                int id = rs.getInt("item_"+j);
                if(id==0){
                    break;
                }
                list.add(id);
            }
            map.put(multiId,list);
            return map;
        });

    }

    public List<Map<Integer,List<Integer>>> findAll(){
        String sql = "select * from v_multi_question where eb103 = 'multi20180405'";
//        String sql = "select * from v_multi_question ";
        return  jdbcTemplate.query(sql, (rs,i)->{
            Map<Integer,List<Integer>> map = Maps.newHashMap();
            int multiId = rs.getInt("pukey");
            List<Integer> list = Lists.newArrayList();
            for(int j= 1;j<=10;j++ ){
                int id = rs.getInt("item_"+j);
                if(id==0){
                    break;
                }
                list.add(id);
            }
            map.put(multiId,list);
            return map;
        });
    }

     public List<Map<Integer,Integer>> findCount(){
        String sql = "select * from v_multi_question where eb103 = 'multi20180405'";
        return  jdbcTemplate.query(sql, (ResultSet rs, int i) ->{
            Map<Integer,Integer> map = Maps.newHashMap();
            int multiId = rs.getInt("pukey");
//            List<Integer> list = Lists.newArrayList();
            Set<Integer> set = Sets.newHashSet();
            for(int j= 1;j<=10;j++ ){
                int id = rs.getInt("item_"+j);
                if(id==0){
                    break;
                }
                set.add(id);
            }
            map.put(multiId,set.size());
            return map;
        });
    }
    public void updateChildIds(Map<Integer, List<Integer>> changeMultiMap) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = jdbcTemplate.getDataSource().getConnection();
            con.setAutoCommit(false);
            ps = con.prepareStatement("");
            for(Map.Entry<Integer,List<Integer>> entry: changeMultiMap.entrySet()){
                String sql = assertUpdateMultiStatement(entry);
                ps.addBatch(sql);
            }
            ps.executeBatch();
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(ps!=null){
                try {
                    ps.clearBatch();
                    ps.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String assertUpdateMultiStatement(Map.Entry<Integer, List<Integer>> entry) {
        StringBuilder sb = new StringBuilder("update v_multi_question set EB103 = 'multi20180405'");
        int i = 1;
        for(int value:entry.getValue()){
            sb.append(",").append("item_").append(i).append(" = ").append(value);
            i++;
        }
        sb.append(" where pukey =").append(entry.getKey());
        logger.info("sql = {}",sb.toString());
        return sb.toString();
    }

    public List<Map<Integer,Integer>> findParentByIdIn(List<Integer> ids,int i) {
        if(CollectionUtils.isEmpty(ids)){
            return Lists.newArrayList();
        }
        String sql = "select * from v_multi_question where item_"+i+"  in (:ids)";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return  namedJdbcTemplate.query(sql, parameters,(rs,k)->{
            Map<Integer,Integer> map = Maps.newHashMap();
            int multiId = rs.getInt("pukey");
            int questionId = rs.getInt("item_"+i);
            map.put(0,multiId);
            map.put(1,questionId);
            return map;
        });

    }
}
