package com.huatu.ztk.backend.mysql.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\4\3 0003.
 */
@Repository
public class PaperQuestionSqlDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findBindings(int id){
        String sql = "select display_order,question_id,bb103,bb106 from v_pastpaper_question_r where pastpaper_id = ?";
        Object[] param  = {id};
        return jdbcTemplate.query(sql, param, (rs, i) -> {
            Map map = Maps.newHashMap();
            map.put("order",rs.getInt("display_order"));
            map.put("question_id",rs.getInt("question_id"));
            map.put("bb103",rs.getInt("bb103"));
            map.put("bb106",rs.getInt("bb106"));
            return map;
        });
    }

    public List<Map> findChangedBindings(){
//        String sql = "select pastpaper_id,display_order,count(1) as binding_count from v_pastpaper_question_r where question_type = 'o' and\n" +
//                "pastpaper_id in (select pukey from v_pastpaper_info where bb102 =1 ) group by pastpaper_id,display_order having count(1)>1";
        String sql = "select r.pastpaper_id,r.display_order,count(1) as binding_count from v_pastpaper_question_r r,v_obj_question o,v_pastpaper_info i where r.question_id = o.pukey and r.pastpaper_id = i.PUKEY\n" +
                "and o.is_multi_part = 1 and r.bb102 = -1 and i.BB102 = 1 group by r.pastpaper_id,r.display_order ";
        return jdbcTemplate.query(sql, (rs, i) -> {
            Map map = Maps.newHashMap();
            int paperId = rs.getInt("pastpaper_id");
            int order = rs.getInt("display_order");
//            map.put("location",paperId+"_"+order);
            map.put("paperId",paperId);
            map.put("order",order);
            map.put("count",rs.getInt("binding_count"));
            return map;
        });
    }

    public List<Map> findQuestionByLocation(int paperId, int order) {
        String sql = "select r.BB102,r.question_id,o.stem,o.multi_id from v_pastpaper_question_r r ,v_obj_question o where r.pastpaper_id = ?  and r.display_order = ? and r.question_id = o.PUKEY and r.question_type = 'o'";
        Object[] params = {paperId,order};
        return jdbcTemplate.query(sql,params, (rs, i) -> {
            Map map = Maps.newHashMap();
            int status = rs.getInt("BB102");
            int questionId = rs.getInt("question_id");
            String stem = rs.getString("stem");
            int multiId = rs.getInt("multi_id");
            map.put("location",paperId+"_"+order);
            map.put("status",status);
            map.put("questionId",questionId);
            map.put("stem",stem);
            map.put("multiId",multiId);
            return map;
        });
    }

    public List<Map> findSubObjectById(int id) {
        String sql = "select r.pastpaper_id,r.display_order,r.question_id,o.multi_id,r.BB102,r.bb106,r.bb103 from v_pastpaper_question_r r, v_obj_question o where o.pukey = r.question_id and question_type = 'o' and o.is_multi_part = 1 and r.pastpaper_id = ?";
        Object[] param = {id};
        return jdbcTemplate.query(sql,param,(rs,i)->{
            Map map = Maps.newHashMap();
            int paperId = rs.getInt("pastpaper_id");
            int order = rs.getInt("display_order");
            int status = rs.getInt("BB102");
            int questionId = rs.getInt("question_id");
//            String stem = rs.getString("stem");
            int multiId = rs.getInt("multi_id");
            map.put("location",paperId+"_"+order);
            map.put("status",status);
            map.put("order",order);
            map.put("questionId",questionId);
            map.put("multiId",multiId);
            map.put("time",Integer.max(rs.getInt("bb103"),rs.getInt("bb103")));
            return map;
        });
    }

    public void updateStatus(List<Integer> bigIds, int id, int i) {
        if(CollectionUtils.isEmpty(bigIds)){
            return;
        }
        String sql = "update v_pastpaper_question_r set bb102 = "+i+" where pastpaper_id = "+id+" and question_id in (:ids)";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", bigIds);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedJdbcTemplate.update(sql,parameters);
    }

    public List<Map<String,Integer>> findByIdIn(List<Integer> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return Lists.newArrayList();
        }
        String sql = "select * from v_pastpaper_question_r  where  question_id in (:ids) order BY pastpaper_id,display_order";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedJdbcTemplate.query(sql,parameters,(rs,i)->{
            Map<String,Integer> map = Maps.newHashMap();
            map.put("questionId",rs.getInt("question_id"));
            map.put("paperId",rs.getInt("pastpaper_id"));
            map.put("order",rs.getInt("display_order"));
            return map;
        });
    }

    public List<Map> groupByCount() {
        String sql = "SELECT\n" +
                "\t2000000 + testpaper_id as id,\n" +
                "\tcount(1) as total\n" +
                "FROM\n" +
                "\tv_testpaper_question_r\n" +
                "WHERE\n" +
                "\tbb102 = 1\n" +
                "GROUP BY\n" +
                "\ttestpaper_id\n" +
                "UNION\n" +
                "SELECT\n" +
                "\tpastpaper_id as id,\n" +
                "\tcount(1) as total\n" +
                "FROM\n" +
                "\tv_pastpaper_question_r\n" +
                "WHERE\n" +
                "\tbb102 = 1\n" +
                "GROUP BY\n" +
                "\tpastpaper_id";
        return jdbcTemplate.query(sql,(rs,i)->{
            Map map = Maps.newHashMap();
            map.put("id",rs.getInt("id"));
            map.put("total",rs.getInt("total"));
            return map;
        });
    }
}
