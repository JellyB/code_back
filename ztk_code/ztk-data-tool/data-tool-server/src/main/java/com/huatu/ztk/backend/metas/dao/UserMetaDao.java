package com.huatu.ztk.backend.metas.dao;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.metas.bean.MatchTimeBean;
import com.huatu.ztk.backend.metas.bean.MatchUserBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

/**
 * Created by huangqp on 2017\11\16 0016.
 */
@Repository
public class UserMetaDao {
    private final static Logger logger = LoggerFactory.getLogger(UserMetaDao.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int BATCHSIZE = 1000;
    public List<MatchUserMeta> getUserMetas(Collection<Long> longSet){
        Criteria criteria = Criteria.where("practiceId").in(longSet);
        List<MatchUserMeta> userMetas = mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
        return userMetas;
    }

    public List<MatchUserMeta> getUserMetasByPaperId(int paperId,long userId,int size){
        long start = System.currentTimeMillis();
        Criteria criteria = Criteria.where("paperId").is(paperId).and("userId").gt(userId);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC,"userId")).limit(size);
        List<MatchUserMeta> userMetas = mongoTemplate.find(query,MatchUserMeta.class);
        long end = System.currentTimeMillis();
        logger.info("find userMeta once use time :{}",end-start);
        return userMetas;
    }


    public void insertUserMatches1(LinkedList<MatchUserBean> matchUserBeans) throws BizException {
        if(CollectionUtils.isEmpty(matchUserBeans)){
            return;
        }
        long start = System.currentTimeMillis();
        Connection con = null;
        PreparedStatement ps = null;
        String sql = "INSERT into v_match_user_log(paperId,areaId,areaName,userId,subjectId,enrolled,looked,practiceId,joined,submitTime,startTime,endTime,score,userName) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try{
            con = jdbcTemplate.getDataSource().getConnection();
            con.setAutoCommit(false);
            ps = con.prepareStatement(sql);
            int i = 0;
            for(MatchUserBean bean:matchUserBeans){
                ps.setInt(1,bean.getPaperId());
                ps.setInt(2,bean.getAreaId());
                ps.setString(3,bean.getAreaName());
                ps.setLong(4,bean.getUserId());
                ps.setInt(5,bean.getSubjectId());
                ps.setInt(6,bean.getEnrolled());
                ps.setInt(7,bean.getLooked());
                ps.setLong(8,bean.getPracticeId());
                ps.setInt(9,bean.getJoined());
                ps.setLong(10,bean.getSubmitTime());
                ps.setLong(11,bean.getStartTime());
                ps.setLong(12,bean.getEndTime());
                ps.setDouble(13,bean.getScore());
                ps.setString(14,bean.getUserName());
                ps.addBatch();
                i++;
                if(i%BATCHSIZE==0||i==matchUserBeans.size()){
                    ps.executeBatch();
                    con.commit();
                    ps.clearBatch();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new BizException(ErrorResult.create(1000002,"修改失败"));
        }finally {
            if(con!=null){
                try {
                    ps.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("insert mysql use time :{}",end-start);
    }

    public void insertUserMatches(LinkedList<MatchUserBean> matchUserBeans) throws BizException {
        if(CollectionUtils.isEmpty(matchUserBeans)){
            return;
        }
        long start = System.currentTimeMillis();
        Connection con = null;
        try {
            con = jdbcTemplate.getDataSource().getConnection();
            con.setAutoCommit(false);
            PreparedStatement ps = null;
            for (int i = 0;i<matchUserBeans.size();i++) {
                MatchUserBean matchUserBean = matchUserBeans.get(i);
                if(ps==null){
                    ps = con.prepareStatement("");
                    ps.addBatch(assertSql(matchUserBean));
                    continue;
                }
                ps.addBatch(assertSql(matchUserBean));
                if((i+1)% BATCHSIZE ==0){
                    ps.executeBatch();
                    con.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            con.commit();
            ps.clearBatch();
            //事务提交
        }catch (Exception e){
            e.printStackTrace();
            throw new BizException(ErrorResult.create(1000002,"修改失败"));
        }finally {
            if(con!=null){
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("insert mysql use time :{}",end-start);

    }
    public void insertUserMatches2(List<MatchUserBean> matchUserBeans) throws BizException {
        if(CollectionUtils.isEmpty(matchUserBeans)){
            return;
        }
        String sql = assertSqls(matchUserBeans);
        insertBatchExecute(sql);
        if(matchUserBeans.size()>BATCHSIZE){
            insertUserMatches2(matchUserBeans.subList(BATCHSIZE,matchUserBeans.size()));
        }
    }

    private void insertBatchExecute(String sql) throws BizException {
        long start = System.currentTimeMillis();
        Connection con = null;
        Statement statement = null;
        try {
            con = jdbcTemplate.getDataSource().getConnection();
            con.setAutoCommit(false);
            statement = con.createStatement();
            statement.execute(sql);
            con.commit();
            //事务提交
        }catch (Exception e){
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            throw new BizException(ErrorResult.create(1000002,"修改失败"));
        }finally {
            if(con!=null){
                try {
                    statement.close();
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("insert mysql use time :{}",end-start);
    }

    private String assertSqls(List<MatchUserBean> beans){
        StringBuilder sql = new StringBuilder("INSERT into v_match_user_log(paperId,paperName,areaId,areaName,userId,subjectId,enrolled,looked,practiceId,joined,submitTime,startTime,endTime,score,userName) values ");
        int i = 0;
        for(MatchUserBean bean:beans ){
            if(i!=0){
                sql.append(",");
            }
            sql.append("(").append(bean.getPaperId()).append(",");
            sql.append("'").append(bean.getPaperName()).append("',");
            sql.append(bean.getAreaId()).append(",");
            sql.append("'").append(bean.getAreaName()).append("',");
            sql.append(bean.getUserId()).append(",");
            sql.append(bean.getSubjectId()).append(",");
            sql.append(bean.getEnrolled()).append(",");
            sql.append(bean.getLooked()).append(",");
            sql.append(bean.getPracticeId()).append(",");
            sql.append(bean.getJoined()).append(",");
            sql.append(bean.getSubmitTime()).append(",");
            sql.append(bean.getStartTime()).append(",");
            sql.append(bean.getEndTime()).append(",");
            sql.append(bean.getScore()).append(",");
            sql.append("'").append(bean.getUserName()).append("')");
            i++;
            if(i==BATCHSIZE){
                break;
            }
        }
        return sql.toString();
    }

    private String assertSql(MatchUserBean bean){
        StringBuilder sql = new StringBuilder("INSERT into v_match_user_log(paperId,areaId,areaName,userId,subjectId,enrolled,looked,practiceId,joined,submitTime,startTime,endTime,score,userName) ");
        sql.append("VALUES (").append(bean.getPaperId()).append(",");
        sql.append(bean.getAreaId()).append(",");
        sql.append("'").append(bean.getAreaName()).append("',");
        sql.append(bean.getUserId()).append(",");
        sql.append(bean.getSubjectId()).append(",");
        sql.append(bean.getEnrolled()).append(",");
        sql.append(bean.getLooked()).append(",");
        sql.append(bean.getPracticeId()).append(",");
        sql.append(bean.getJoined()).append(",");
        sql.append(bean.getSubmitTime()).append(",");
        sql.append(bean.getStartTime()).append(",");
        sql.append(bean.getEndTime()).append(",");
        sql.append(bean.getScore()).append(",");
        sql.append("'").append(bean.getUserName()).append("')");
        return sql.toString();
    }

    public int findLogByPaperId(int paperId) {
        String sql = "select count(1) from v_match_user_log where paperId = ? ";
        Object[] params = {
                paperId
        };
        return jdbcTemplate.queryForObject(sql,params,Integer.class);
    }
    public List<Map<String,Object>> findLogsByIndex1(long index, int size) {
        String sql = "select * from v_match_user_log where id > ? limit ? ";
        Object[] params = {
                index, size
        };
        return jdbcTemplate.query(sql,params,(rs, i) -> {
            Map<String,Object> map = Maps.newHashMap();
            long id = rs.getLong("id");
            int paperId = rs.getInt("paperId");
            String paperName = rs.getString("paperName");
            int areaId = rs.getInt("areaId");
            String areaName = rs.getString("areaName");
            int subjectId = rs.getInt("subjectId");
            long userId = rs.getLong("userId");
            String userName = rs.getString("userName");
            int enrolled = rs.getInt("enrolled");
            int looked = rs.getInt("looked");
            long practiceId = rs.getLong("practiceId");
            int joined = rs.getInt("joined");
            long submitTime = rs.getLong("submitTime");
            long startTime = rs.getLong("startTime");
            long endTime = rs.getLong("endTime");
            double score = rs.getDouble("score");
            map.put("id",id);
            map.put("offsetId",id);
            map.put("paperId",paperId);
            map.put("paperName",paperName);
            map.put("areaId",areaId);
            map.put("areaName",areaName);
            map.put("subjectId",subjectId);
            map.put("userId",userId);
            map.put("userName",userName);
            map.put("enrolled",enrolled);
            map.put("looked",looked);
            map.put("practiceId",practiceId);
            map.put("joined",joined);
            map.put("submitTime",submitTime);
            map.put("startTime",startTime);
            map.put("endTime",endTime);
            map.put("score",score);
            return map;
        });
    }
    public List<MatchUserBean> findLogsByIndex(long index, int size) {
        String sql = "select * from v_match_user_log where id > ? limit ? ";
        Object[] params = {
                index,size
        };
        return jdbcTemplate.query(sql, params, (rs, i) -> {
            MatchUserBean matchUserBean = new MatchUserBean();
            long id = rs.getLong("id");
            int paperId = rs.getInt("paperId");
            String paperName = rs.getString("paperName");
            int areaId = rs.getInt("areaId");
            String areaName = rs.getString("areaName");
            int subjectId = rs.getInt("subjectId");
            long userId = rs.getLong("userId");
            String userName = rs.getString("userName");
            int enrolled = rs.getInt("enrolled");
            int looked = rs.getInt("looked");
            long practiceId = rs.getLong("practiceId");
            int joined = rs.getInt("joined");
            long submitTime = rs.getLong("submitTime");
            long startTime = rs.getLong("startTime");
            long endTime = rs.getLong("endTime");
            double score = rs.getDouble("score");
            matchUserBean.setId(id);
            matchUserBean.setPaperId(paperId);
            matchUserBean.setPaperName(paperName);
            matchUserBean.setUserId(userId);
            matchUserBean.setAreaId(areaId);
            matchUserBean.setAreaName(areaName);
            matchUserBean.setSubjectId(subjectId);
            matchUserBean.setEnrolled(enrolled);
            matchUserBean.setLooked(looked);
            matchUserBean.setPracticeId(practiceId);
            matchUserBean.setJoined(joined);
            matchUserBean.setSubmitTime(submitTime);
            matchUserBean.setStartTime(startTime);
            matchUserBean.setEndTime(endTime);
            matchUserBean.setScore(score);
            matchUserBean.setUserName(userName);
            return matchUserBean;
        });
    }

    public List<Map> findUserNameByIDs(List<Long> ids) {
        Map<String,Object> parameters = new HashMap<String,Object>();
        String sql = "select pukey,uname from v_qbank_user where pukey in (:ids)";
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        return namedJdbcTemplate.query(sql, parameters, (rs, i) -> {
            Map map = Maps.newHashMap();
            map.put(rs.getLong("pukey"),rs.getString("uname"));
            return map;
        });
    }
    public List<Map> findUserInfoByIDs(List<Long> ids) {
        Map<String,Object> parameters = new HashMap<String,Object>();
        String sql = "select pukey,uname,reg_phone,nick from v_qbank_user where pukey in (:ids)";
        parameters.put("ids", ids);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        return namedJdbcTemplate.query(sql, parameters, (rs, i) -> {
            Map map = Maps.newHashMap();
            map.put("uname",rs.getString("uname"));
            map.put("reg_phone",rs.getString("reg_phone"));
            map.put("nick",rs.getString("nick"));
            map.put("userId",rs.getString("pukey"));
            return map;
        });
    }
    public void insertUserMatchTime(List<MatchTimeBean> matchTimeBeans) throws BizException {
        if(CollectionUtils.isEmpty(matchTimeBeans)){
            return;
        }
        String sql = assertTimeSql(matchTimeBeans);
        insertBatchExecute(sql);
        if(matchTimeBeans.size()>BATCHSIZE){
            insertUserMatchTime(matchTimeBeans.subList(BATCHSIZE,matchTimeBeans.size()));
        }
    }

    private String assertTimeSql(List<MatchTimeBean> matchTimeBeans) {
        StringBuilder sql = new StringBuilder("INSERT into v_match_user_time_log(paperId,userId,moduleId,moduleName,questionNum,time) values ");
        int i = 0;
        for(MatchTimeBean bean:matchTimeBeans ){
            if(i!=0){
                sql.append(",");
            }
            sql.append("(").append(bean.getPaperId()).append(",");
            sql.append(bean.getUserId()).append(",");
            sql.append(bean.getModuleId()).append(",");
            sql.append("'").append(bean.getModuleName()).append("',");

            sql.append(bean.getQuestionNum()).append(",");
            sql.append(bean.getTime()).append(")");
            i++;
            if(BATCHSIZE==i){
                break;
            }
        }
        return sql.toString();
    }

    /**
     * 查询之前考试的学员id（flag =1报名学员2参考学员）
     * @param flag
     * @param startTime
     * @return
     */
    public List<Long> findUserIdByStart(int flag, long startTime) {
        StringBuilder sb = new StringBuilder("select DISTINCT userId from v_match_user_log where startTime <  ?");
        if(flag == 2 ){
            sb.append(" and joined = 1");
        }
        Object[] param = {startTime};
        return jdbcTemplate.queryForList(sb.toString(),param,Long.class);

    }
    /**
     * 查询当次考试的学员id（flag =1报名学员2参考学员）
     * @param flag
     * @param paperId
     * @return
     */
    public List<Long> findUserIdByPaperId(int flag, int paperId) {
        StringBuilder sb = new StringBuilder("select DISTINCT userId from v_match_user_log where paperId = ?");
        if(flag == 2 ){
            sb.append(" and joined = 1");
        }
        Object[] param = {paperId};
        return jdbcTemplate.queryForList(sb.toString(),param,Long.class);
    }

    public Integer countByUserId(long userId) {
        long start = System.currentTimeMillis();
        Criteria criteria = Criteria.where("userId").gt(userId);
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query,MatchUserMeta.class);
        long end = System.currentTimeMillis();
        logger.info("find userMeta once use time :{}",end-start);
        return total==null?0:total.intValue();
    }

    public Map<Long,Integer> groupByUserId(List<Long> collect) {
        collect.removeIf(i->i==null);
        Map<Long,Integer> mapData = Maps.newHashMap();
        // 返回的字段
        ProjectionOperation projectionOperation = Aggregation.project("userId", "sum");

        // 条件
        Criteria operator = Criteria.where("userId").in(collect);
        MatchOperation matchOperation = Aggregation.match(operator);

        // 分组操作，并对每个广告的总条数进行统计
        GroupOperation groupOperation = Aggregation.group("userId").count().as("sum");
        // 组合条件
        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);
        // 执行操作
        AggregationResults<Map> aggregationResults = mongoTemplate.aggregate(aggregation, "ztk_match_user_meta", Map.class);
        logger.info("list={}",aggregationResults.getMappedResults());
        aggregationResults.getMappedResults().stream().filter(i->i.get("_id")!=null).forEach(i->{
            mapData.put(Long.parseLong(String.valueOf(i.get("_id"))),Integer.parseInt(i.get("sum").toString()));
        });
        return mapData;
    }
}
