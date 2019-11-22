package com.huatu.ztk.backend.question.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.question.bean.RefuseInfo;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import com.huatu.ztk.question.common.QuestionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-06-15  17:15 .
 */
@Repository
public class QuestionScatteredDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionScatteredDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final String collection = "ztk_question_new";
    public static final String collectionExtend = "question_extend";
    public static final int QUESTION_LIST_LIMIT = 200;

    /**
     * 根据各种搜索条件查找信息
     * @param subject
     * @param stem
     * @param pointsId
     * @param startTime
     * @param endTime
     * @param uid
     * @param status
     * @param questionIds
     * @return
     */
    public List<Question> findByDetail(int subject,String stem,List<Integer> pointsId,long startTime,long endTime,long uid,int status,List<Integer> questionIds){
        List<Question> questions = Lists.newArrayList();
        //Criteria criteria = new Criteria().and("channel").is(3).orOperator(Criteria.where("parent").is("0"),Criteria.where("parent").exists(false));//只查询散题，且是父题
        Criteria criteria = Criteria.where("channel").is(3);

        if(subject!=-1){
            criteria.and("subject").is(subject);
        }
        if(stem!=""&&stem!=null&&stem.length()>0){
            criteria.and("stem").regex(".*"+stem+".*");
        }
        if(pointsId.size()!=0){
            criteria.and("points").is(pointsId);
        }
        if(startTime!=-1&&endTime!=-1){
            criteria.and("createTime").gte(startTime).lte(endTime);
        }else if(startTime==-1&&endTime!=-1){
            criteria.and("createTime").lte(endTime);
        }else if(startTime!=-1&&endTime==-1){
            criteria.and("createTime").gte(startTime);
        }
        if(uid==-1){
            criteria.and("status").is(QuestionStatus.CREATED);
        }else{
            criteria.and("createBy").is(uid);
            if(status!=-1){
                criteria.and("status").is(status);
            }else{
                criteria.and("status").ne(QuestionStatus.DELETED);
            }
        }
        if(CollectionUtils.isNotEmpty(questionIds)){
            criteria.and("id").in(questionIds);
        }

        criteria.orOperator(Criteria.where("parent").is(0),Criteria.where("parent").exists(false));

        Query query = new Query(criteria);
        if(uid!=-1){
            query.limit(QUESTION_LIST_LIMIT);
        }
        query.with(new Sort(Sort.Direction.DESC, "createTime"));//按年份排序
        questions = mongoTemplate.find(query,Question.class,collection);
        logger.info("共查找到的散题数量={}",questions.size());
        return questions;
    }

    /**
     * 根据questionId或者module，查询该题的扩展部分
     * @param ids
     * @return
     */
    public List<QuestionExtend> findExtendByIds(List<Integer> ids,int module){
        Criteria criteria = new Criteria();
        if(CollectionUtils.isNotEmpty(ids)){
            criteria.and("qid").in(ids);
        }
        if(module!=-1){
            criteria.and("moduleId").is(module);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "qid"));
        return mongoTemplate.find(query,QuestionExtend.class,collectionExtend);
    }

    public void editStatus(int id,int status){
        Question question = findById(id);
        question.setStatus(status);
        try {
            mongoTemplate.save(question, collection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Question findById(int id){
        return mongoTemplate.findById(id,Question.class,collection);
    }


    public void insertRefuseInfo(int id,String description){
        String sql = "INSERT INTO v_c_new_sys_scattered_question_review(qid,description) VALUES (?,?)";
        Object[] param = {id,description};
        jdbcTemplate.update(sql,param);
    }

    public void deleteRefuseInfo(int id){
        String sql = "DELETE FROM v_c_new_sys_scattered_question_review WHERE qid=?";
        Object[] param = {id};
        jdbcTemplate.update(sql,param);
    }

    public RefuseInfo findRefuseInfo(int id){
        String sql = "SELECT * FROM v_c_new_sys_scattered_question_review WHERE qid=?";
        Object[] param = {id};
        List<RefuseInfo> refuseInfos = jdbcTemplate.query(sql,param,new RefuseInfoRowMapper());
        RefuseInfo refuseInfo = new RefuseInfo();
        if (CollectionUtils.isNotEmpty(refuseInfos)) {
            refuseInfo = refuseInfos.get(0);
        }
        return refuseInfo;
    }


    class RefuseInfoRowMapper implements RowMapper<RefuseInfo> {
        public RefuseInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            final RefuseInfo fi = RefuseInfo.builder()
                    .id(rs.getInt("qid"))
                    .description(rs.getString("description"))       //反馈名称
                    .build();
            return fi;
        }
    }
}
