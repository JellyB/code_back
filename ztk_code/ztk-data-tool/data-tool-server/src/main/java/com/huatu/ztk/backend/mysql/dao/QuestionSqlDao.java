package com.huatu.ztk.backend.mysql.dao;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\4\5 0005.
 */
@Repository
public class QuestionSqlDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public void updateChildQuestion(Map<Integer, Integer> changeChildMap) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = jdbcTemplate.getDataSource().getConnection();
            con.setAutoCommit(false);
            ps = con.prepareStatement("");
            for(Map.Entry<Integer,Integer> entry:changeChildMap.entrySet()){
                String sql = "UPDATE v_obj_question SET multi_id = "+ entry.getValue() +",EB103 = 'multi20180405' where pukey = " +entry.getKey();
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

    public List<Map<String,Integer>> findByMulti(int multiId) {
        String sql = "select * from v_obj_question where multi_id = ?";
        Object[] param = {multiId};
        return jdbcTemplate.query(sql,param,(rs,i)->{
            Map<String,Integer> map = Maps.newHashMap();
            map.put("id",rs.getInt("pukey"));
            map.put("flag",rs.getString("eb103").equals("multi20180405")?1:0);
            map.put("is_answer_true",rs.getInt("is_ture_answer"));
            map.put("status",rs.getInt("bb102"));
            return map;
        });
    }
    public List<Integer> findIdByMulti(int multiId) {
        String sql = "select pukey from v_obj_question where multi_id = ?";
        Object[] param = {multiId};
        return jdbcTemplate.queryForList(sql,param,Integer.class);
    }
    public List<Map<Integer,Integer>> findUpdate() {
        String sql = "select * from v_obj_question where eb103 = 'multi20180405'";
        return jdbcTemplate.query(sql,(rs,i)->{
            Map<Integer,Integer> map = Maps.newHashMap();
            map.put(rs.getInt("pukey"),rs.getInt("multi_id"));
            return map;
        });
    }

    public List<Map<Integer,Integer>> findCount() {
        String sql =" select multi_id,count(1) msize from v_obj_question group by multi_id";
        return jdbcTemplate.query(sql,(rs,i)->{
            Map<Integer,Integer> map = Maps.newHashMap();
            map.put(rs.getInt("multi_id"),rs.getInt("msize"));
            return map;
        });
    }
}
