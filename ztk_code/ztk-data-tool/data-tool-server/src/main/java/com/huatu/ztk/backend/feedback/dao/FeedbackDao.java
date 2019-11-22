package com.huatu.ztk.backend.feedback.dao;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.feedback.feedback.Feedback;
import com.huatu.ztk.backend.system.bean.NsTextMsg;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.backend.util.ZlibCompressUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ht on 2016/12/1.
 */
@Repository
public class FeedbackDao {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取反馈列表
     * @param catgory
     * @return
     */
    public List<Feedback> query(int catgory){
        String sql = "SELECT * FROM v_user_feedback WHERE 1=1  ";
        if(catgory>0){
            sql+=" and catgory= "+catgory;
        }
        sql+=" order by BB103 desc limit 0,100 ";
        final List<Feedback> feedbackList = jdbcTemplate.query(sql, new FeedBackRowMapper());
        if (CollectionUtils.isEmpty(feedbackList)) {
            return  new ArrayList<>();
        }
        return feedbackList;
    }

    /**
     * 获取反馈详情
     * @param id
     * @return
     */
    public Feedback find(long id){
        String sql = "SELECT * FROM v_user_feedback WHERE pukey=? ";
        Object[] param={id};
        final List<Feedback> feedbackList = jdbcTemplate.query(sql,param, new FeedBackRowMapper());
        if (CollectionUtils.isEmpty(feedbackList)) {
            return null;
        }
        Feedback feedback=feedbackList.get(0);
        return feedback;
    }

    class FeedBackRowMapper implements RowMapper<Feedback> {
        public Feedback mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String,String > resultMap=getUserFeedVersion(rs.getString("FB1Z2"));
            final Feedback feedback = Feedback.builder()
                    .id(rs.getInt("pukey"))
                    .uid(rs.getInt("uid"))
                    .title(rs.getString("title"))
                    .content(rs.getString("content"))
                    .catgory(rs.getInt("catgory"))
                    .contact(rs.getString("contact_mail"))
                    .appVersion(resultMap.get("appVersion"))
                    .device(resultMap.get("device"))
                    .system(resultMap.get("system"))
                    .createTime(rs.getLong("BB103")*1000)
                    .type(rs.getInt("type"))
                    .build();
            return feedback;
        }
    }

    private Map<String,String> getUserFeedVersion(String result){
        Map<String,String> resultMap=Maps.newHashMap();
        if(result==null){
            return resultMap;
        }
        result=ZlibCompressUtils.uncompress(result);//解码
        Map<String,Object> map=JsonUtil.toMap(result);
        if(map.containsKey("App版本")){ //版本号
            resultMap.put("appVersion",(String )map.get("App版本"));
        }
        if(map.containsKey("App代号")){  //设备
            resultMap.put("device",(String )map.get("App代号"));
        }
        if(map.containsKey("App版本号")){ //版本号
            resultMap.put("appVersion",(String )map.get("App版本号"));
        }
        if(map.containsKey("操作设备")){  //设备
            resultMap.put("device",(String )map.get("操作设备"));
        }
        if(map.containsKey("系统版本号")){  //操作系统
            resultMap.put("system",(String )map.get("系统版本号"));
        }
       return resultMap;

    }
}
