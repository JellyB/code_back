package com.huatu.ztk.backend.question.dao;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.question.bean.QuestionModify;
import com.huatu.ztk.backend.question.bean.QuestionTemp;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import com.huatu.ztk.question.common.QuestionStatus;
<<<<<<< HEAD
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
=======
>>>>>>> master
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-22  20:45 .
 */
@Repository
public class QuestionDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionDao.class);
    @Autowired
    private QuestionDubboService questionDubboService;
    /**
     * 题库
     */
    public static final String collection = "ztk_question_new";
    public static final String collectionCopy = "ztk_question_copy";
    public static final String collectionExtend = "question_extend";
    public static final String collectionExtendCopy = "question_extend_copy";
    public static final String collectionId = "question_temp";//存放试题id的表
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final int ID_BASE = 21000000;//id基数，防止跟以前的id冲突
    public static final int QUESTION_LIST_LIMIT = 2000;
    public static final int QUESTIONEXTEND_LIST_LIMIT = 20000;

    /**
     * 根据questionId，查询该行测题
     * @param questionId
     * @return
     */
    public List<GenericQuestion> findById(int questionId){
        Criteria criteria = Criteria.where("id").is(questionId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query,GenericQuestion.class,collection);
    }

    /**
     * 根据questionId，返回试题（支持各种类型）
     * @param questionId
     * @return
     */
    public Question findAllTypeById(int questionId){
        return mongoTemplate.findById(questionId,Question.class,collection);
    }

    /**
     * 根据试题id列表，返回所有试题（支持各种类型）
     * @param ids
     * @return
     */
    public List<Question> findAllTypeByIds(List<Integer> ids){
        Criteria criteria = Criteria.where("id").in(ids);
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Question.class,collection);
    }

    /**
     * 根据questionId，查询该题的扩展部分
     * @param questionId
     * @return
     */
    public QuestionExtend findExtendById(int questionId){
        return mongoTemplate.findById(questionId,QuestionExtend.class,collectionExtend);
    }

    /**
     * 根据questionId，查询该题的扩展部分
     * @param ids
     * @return
     */
    public List<QuestionExtend> findExtendByIds(List<Integer> ids){
        Criteria criteria = Criteria.where("qid").in(ids);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "qid"));
        return mongoTemplate.find(query,QuestionExtend.class,collectionExtend);
    }

    /**
     * 根据模块id查找该模块下试题扩展对象集合
     *
     * @param moduleId
     * @return
     */
    public List<QuestionExtend> findExtendByModule(int moduleId) {
        Criteria criteria = Criteria.where("moduleId").is(moduleId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, QuestionExtend.class, collectionExtend);
    }

    /**
     * 根据questionId，删除该题，即修改该题的状态为删除状态
    */
    public void deleteById(int questionId){
        GenericQuestion question = findById(questionId).get(0);
        question.setStatus(QuestionStatus.DELETED);
        try {
            mongoTemplate.save(question, collection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据搜索条件，返回一个列表
     * @param subject
     * @param areas
     * @param year
     * @param mode
     * @param types
     * @param stem
     * @param questionIds
     * @param parent
     * @return
     */
    public List<Question> findByDetail(int subject,List<Integer> areas,int year,int mode,List<Integer> types,String stem,List<Integer> questionIds,int parent){
        List<Question> questions = Lists.newArrayList();
        Criteria criteria = new Criteria().and("status").is(QuestionStatus.AUDIT_SUCCESS);//只查询审核通过的题
        int sign = 0;//为0表示所有搜索条件都无效，1存在有些搜索条件
        if(subject!=-1){
            criteria.and("subject").is(subject);
            sign = 1;
        }
        if(year!=-1){
            criteria.and("year").is(year);
            sign = 1;
        }
        if(mode!=-1){
            criteria.and("mode").is(mode);
            sign = 1;
        }
        if(stem!=""&&stem!=null&&stem.length()>0){
            criteria.and("stem").regex(".*"+stem+".*");
            sign = 1;
        }
        if(parent!=-1){
            if(parent==0){
                criteria.orOperator(Criteria.where("parent").is(0),Criteria.where("parent").exists(false));
            }else{
                criteria.and("parent").is(parent);
            }
            sign = 1;
        }
        if(CollectionUtils.isNotEmpty(areas)){
            criteria.and("area").in(areas);
            sign = 1;
        }
        if(CollectionUtils.isNotEmpty(types)){
            criteria.and("type").in(types);
            sign = 1;
        }
        if(CollectionUtils.isNotEmpty(questionIds)){
            criteria.and("id").in(questionIds);
            sign = 1;
        }
        if(sign==0){//返回空表
            return questions;
        }else{
            Query query = new Query(criteria).limit(QUESTION_LIST_LIMIT);
            //按年份排序
            query.with(new Sort(Sort.Direction.DESC, "createTime"));
            questions = mongoTemplate.find(query,Question.class,collection);
            return questions;
        }
    }

    /**
     * 插入试题
     * @param question
     */
    public int insert(Question question){
        long startInsert = System.currentTimeMillis();
        int id = -1;
        if (question.getId()<1) {//没有设置id，则生成id此处主要是数据初始化时用到
            //long questionId = mongoTemplate.count(new Query(), collection)+ID_BASE;
            Criteria criteria = Criteria.where("id").is(1);
            Query query = new Query(criteria);
            Update update = new Update().inc("questionId",1);
            long questionId = 0;
            QuestionTemp questionTemp = mongoTemplate.findAndModify(query,update,QuestionTemp.class,collectionId);
            questionId = questionTemp.getQuestionId();
            logger.info("获取到的id={}",questionId);
            question.setId((int)questionId);//设置id
        }
        long midInsert = System.currentTimeMillis();
        logger.info("试题id={}，计算id用时={}",id,midInsert-startInsert);
        //此处循环是未来保证key冲突的情况下也能插入到mongo里
        for (int i = 0; i < 5; i++)
            try {
                mongoTemplate.save(question, collection);
                id = question.getId();//获取id
                break;//插入成功，则跳出循环
            } catch (Exception e) {
                e.printStackTrace();
            }
        long endInsert = System.currentTimeMillis();
        logger.info("试题id={}，插入试题用时={}",id,endInsert-startInsert);
        return id;
    }

    /**
     * 根据试题id列表，修改所有列表的状态
     * @param ids
     * @param status
     */
    public void editQuestionsStatus(List<Integer> ids,int status){
        Query query = new Query(Criteria.where("id").in(ids));
        Update update = new Update().set("status",status);
        mongoTemplate.updateMulti(query, update,Question.class,collection);
    }


    /**
     * 插入试题扩展部分
     * @param questionExtend
     */
    public void insertExtend(QuestionExtend questionExtend){
        try {
            mongoTemplate.save(questionExtend,collectionExtend);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 更改试题信息
     * @param question
     */
    public void updateQuestion(Question question){
        mongoTemplate.save(question,collection);
    }



    /**
     * 插入编辑题记录
     * @param applyName
     * @param applyId
     */
    public void editApply(QuestionModify questionModify,String applyName,int applyId){
        String sql = "INSERT INTO v_question_new_check_log(question_id,content,applier_name,applier_id,apply_time,type,subject,subSign,paperId,module) VALUES (?,?,?,?,?,?,?,?,?,?)";
        int applyTime = (int) (System.currentTimeMillis()/1000);
        Object[] param = {questionModify.getQid(),questionModify.getContent(),applyName,applyId,applyTime,questionModify.getType(),questionModify.getSubject(),questionModify.getSubSign(),questionModify.getPaperId(),questionModify.getModule()};
        jdbcTemplate.update(sql,param);
    }


    /**
     * 根据条件，查找符合的题编辑记录
     */
    public List<QuestionModify> findEditLogByDetail(int subject,int module,int type,long startTime,long endTime,int questionId,long id,int status){
        String sql = "SELECT * FROM v_question_new_check_log WHERE 1=1 and subject="+subject;
        System.out.println(subject+"  "+module+"  "+type+"  "+startTime+"  "+endTime);
        List<Object> paramList = new ArrayList<>();
        if(questionId>0){//subject 有效
            sql +=" AND question_id=?";
            paramList.add(questionId);
        }
        if(module>0){//module 有效
            sql +=" AND module=?";
            paramList.add(module);
        }
        if(type>0){//type 有效
            if(type==2){
                sql +=" AND type=105";
            }else if(type==3){
                sql +=" AND type=106";
            }else if(type==4){
                sql +=" AND type=107";
            }else{
                sql +=" AND type in (99,100,101,109)";
            }
        }
        if(startTime>0){//startTime 有效
            sql +=" AND apply_time>?";
            paramList.add(startTime);
        }
        if(endTime>0){//type 有效
            sql +=" AND apply_time<?";
            paramList.add(endTime);
        }
        if(id>=0){
            sql +=" AND applier_id=?";
            paramList.add(id);
            if(status>-2){
                sql +=" AND review_mark=?";
                paramList.add(status);
            }
        }else{
            sql +=" AND review_mark=-1";
        }
        sql+=" order by apply_time desc ";
        logger.info("sql={}",sql);
        Object[] param = paramList.toArray();
        final List<QuestionModify> questionModifyList = jdbcTemplate.query(sql,param,new QuestionModifyRowMapper());
        return questionModifyList;
    }

    /**
     *根据试题id，查找该题的编辑记录
     * @param questionId
     * @return
     */
    public List<QuestionModify> findEditByQuestionId(int questionId){
        String sql = "SELECT * FROM v_question_new_check_log WHERE question_id=? AND review_mark=-1";
        Object[] param = {questionId};
        final List<QuestionModify> questionModifyList = jdbcTemplate.query(sql,param,new QuestionModifyRowMapper());
        return questionModifyList;
    }
    public List<QuestionModify> findByQuestionId(int questionId){
        String sql = "SELECT * FROM v_question_new_check_log WHERE question_id=?";
        Object[] param = {questionId};
        final List<QuestionModify> questionModifyList = jdbcTemplate.query(sql,param,new QuestionModifyRowMapper());
        return questionModifyList;
    }

    /**
     * 根据试题id，状态status，修改该题的编辑状态
     * @param questionId
     * @param status
     * @reurn
     */
    public void modifyEditLogStatus(int questionId,int status,String reason){
        String sql = "UPDATE v_question_new_check_log SET review_mark=?,review_content=? WHERE question_id=?";
        Object[] param = {status,reason,questionId};
        jdbcTemplate.update(sql,param);
    }

    /**
     * 获取试题信息
     * @param id
     * @return
     */
    public Question findQuestionById(int id){
        final Question question = mongoTemplate.findById(id, Question.class, collection);
         return question;
    }
    /**
     * 获取某个id区间的question集合
     * @param startId
     * @param endId
     * @return
     */
    public List<Question> findQuestionsByRange(int startId, int endId){
        Criteria criteria = Criteria.where("id").gte(startId).lte(endId).orOperator(Criteria.where("parent").is(0),Criteria.where("parent").exists(false));
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Question.class,collection);
    }


    /**
     * 批量插入试题
     */
    public List<Question> filterNull(){
        Criteria criteria = Criteria.where("parent").ne(0).and("subject").gte(1000).lte(1020).and("material").regex(Pattern.compile("(.*?)null</p>(.*?)</p>"));
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Question.class,collection);
    }

    public List<Question> filterNoPoint(){
        Criteria criteria = Criteria.where("points").size(0).and("subject").gte(1000).lte(1020);
        logger.info("表示已经来了={}","wo");
        //Criteria criteria = Criteria.where("type").is(99);
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Question.class,collectionCopy);
    }
    public QuestionExtend filterNoPointExtend(int questionId){
        return mongoTemplate.findById(questionId,QuestionExtend.class,collectionExtendCopy);
    }

    public void editMaterial(int qid,String material){
        Query query = new Query(Criteria.where("id").is(qid));
        Update update = new Update().set("material",material);
        mongoTemplate.updateFirst(query, update, GenericQuestion.class,collection);
    }

    /**
     * 分页查询
     * @param cursor
     * @param size
     * @param subject
     * @return
     */
    public List<Question> findQuestionsForPage(int cursor,int size,int subject){
        Query query = new Query(Criteria.where("id").gt(cursor).and("subject").is(subject));
        query.with(new Sort(Sort.Direction.ASC,"id")).limit(size);
        return mongoTemplate.find(query,Question.class,collection);
    }
    /**
     * 分页查询
     * @param cursor
     * @param size
     * @param subject
     * @return
     */
    public List<Question> findQuestionsByPointForPage(int cursor,int size,int subject,int pointId){
        Query query = new Query(Criteria.where("id").gt(cursor).and("subject").is(subject).and("points").all(Lists.newArrayList(pointId)));
        query.with(new Sort(Sort.Direction.ASC,"id")).limit(size);
        return mongoTemplate.find(query,Question.class,collection);
    }
    public Long countBySubject(int subject) {
        Query query = new Query(Criteria.where("subject").is(subject));
        return mongoTemplate.count(query,Question.class,collection);
    }


    /**
     * 查询试卷导入时试卷中的试题id
     * @param paperId
     * @return
     */
    public List<Integer> findExportQuestion(Integer paperId) {
        String sql = "select DISTINCT questionId from v_export_question where status = 1 and paperId = ? ";
        Object[] param = {paperId};
        return jdbcTemplate.queryForList(sql,param,Integer.class);
    }

    /**
     * 查询已经导入的有归属的试题
     * @return
     */
    public List<Integer> findAllExportQuestion() {
        String sql = "select DISTINCT questionId from v_export_question where status = 1 ";
        return jdbcTemplate.queryForList(sql,Integer.class);
    }

    /**
     * 批量添加记录（试卷下试题导入记录）
     * @param paperId
     * @param questions
     */
    public void insertExportQuestion(Integer paperId, List<Integer> questions) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into v_export_question(paperId, questionId) VALUES ");
        for(Integer id:questions){
            sb.append("("+paperId+","+id+")").append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        logger.info("添加试卷试题记录的sql={}",sb.toString());
        jdbcTemplate.update(sb.toString());
    }

    /**
     * 删除试卷下的导入记录
     * @param paperId
     * @param questions
     */
    public void deleteExportQuestion(Integer paperId, List<Integer> questions) {
        StringBuilder sb = new StringBuilder();
        sb.append("update v_export_question set status = -1 where questionId in (");
        for(Integer id:questions){
            sb.append(id).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(") and paperId = ");
        sb.append(paperId);
        logger.info("删除试卷试题记录的sql={}",sb.toString());
        jdbcTemplate.update(sb.toString());
    }

    public List<Integer> findByModuleId(Integer moduleId) {
        String sql ="select PUKEY from v_obj_question where is_ture_answer = 1 and bb102 = 1 and bl_ep_part = ? and multi_id = 0 ORDER  by BB103 DESC ";
        Object[] param = {moduleId};
        return jdbcTemplate.queryForList(sql,param,Integer.class);
    }

    public List<Integer> findByModuleId1(Integer moduleId) {
        String sql ="select multi_id from (\n" +
                "select multi_id,count(1) as c from v_obj_question where is_ture_answer = 1 and bb102 = 1 and bl_ep_part =? group by multi_id having  c = 5 ) as b";
        Object[] param = {moduleId};
        return jdbcTemplate.queryForList(sql,param, Integer.class);
    }

    public List<Integer> findByMultiIds(List<Integer> multiIds) {
        String sql = "select pukey from v_obj_question where is_ture_answer = 1 and bb102 = 1 and bl_ep_part = 19 and multi_id in (:ids) order by multi_id,seq_in_pastpaper;";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", multiIds);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedJdbcTemplate.queryForList(sql,parameters,Integer.class);
    }

    public String findMoudleName(Integer moduleId) {
        String sql ="select name from v_exampaper_part where pukey = ?";
        Object[] param = {moduleId};
        return jdbcTemplate.queryForList(sql,param, String.class).get(0);
    }

    public List<Integer> sortMultiIds(List<Integer> multiIds) {
        String sql = "select pukey from v_multi_question ORDER by BB103 DESC ";
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("ids", multiIds);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return namedJdbcTemplate.queryForList(sql,parameters,Integer.class);
    }

    public void group(List<String> questions, String collectionName, StringBuilder stringBuilder) {
        Map<String,Integer> countMap = Maps.newHashMap();
        List<Integer> ids = questions.stream().map(Integer::parseInt).collect(Collectors.toList());
        DBObject queryObject = new BasicDBObject();
        queryObject.put("_id",new BasicDBObject("$in", ids));
        DBObject fieldsObject = new BasicDBObject();
        fieldsObject.put("_id",1);
        fieldsObject.put("subject",1);
        fieldsObject.put("status",1);
        fieldsObject.put("type",1);
        DBCursor dbCursor =mongoTemplate.getCollection(collectionName).find(queryObject,fieldsObject);
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            String subject = String.valueOf(object.get("subject"));
            String status = String.valueOf(object.get("status"));
            String type = String.valueOf(object.get("type"));
            String  key = subject + "_" + status + "_" + type;
            Integer count = countMap.getOrDefault(key, 0);
            count++;
            countMap.put(key,count);
        }
        List<Map.Entry<String, Integer>> tempList = countMap.entrySet().stream().collect(Collectors.toList());
        tempList.sort(Map.Entry.comparingByValue());
        stringBuilder.append("表名\t\t\t").append("总数\t").append("科目\t").append("状态\t").append("题型\r\n");
        for (Map.Entry<String, Integer> entry : tempList) {
            String[] split = entry.getKey().split("_");
            logger.info("collection={},total={},subject={},status={},type={}",collectionName,entry.getValue(),split[0],split[1],split[2]);
            stringBuilder.append(collectionName+"\t").append(entry.getValue()+"\t\t").append(split[0]+"\t\t").append(split[1]+"\t\t").append(split[2]+"\r\n");
        }
    }

    class QuestionModifyRowMapper implements RowMapper<QuestionModify> {
        @Override
        public QuestionModify mapRow(ResultSet rs, int rowNum) throws SQLException {
            final QuestionModify qm = QuestionModify.builder()
                    .id(rs.getInt("pukey"))
                    .qid(rs.getInt("question_id"))      //试题id
                    .uid(rs.getInt("applier_id"))            //反馈人id
                    .uname(rs.getString("applier_name"))       //反馈名称
                    .reviewerId(rs.getInt("reviewer_id"))//审核人id
                    .reviewerName(rs.getString("reviewer_name"))//审核人name
                    .content(rs.getString("content"))  //纠错内容
                    .createTime(rs.getLong("apply_time")*1000)  //创建时间
                    .reviewTime(rs.getLong("apply_time")*1000)//审核时间
                    .subject(rs.getInt("subject"))//类目
                    .type(rs.getInt("type"))//题型
                    .module(rs.getInt("module"))//模块
                    .status(rs.getInt("review_mark"))
                    .subSign(rs.getString("subSign"))
                    .paperId(rs.getInt("paperId"))
                    .reviewContent(rs.getString("review_content"))
                    .build();
            return qm;
        }
    }

    /**
     * 所有试题ID统计
     * @param subjects
     * @param collectionName
     * @return
     */
    public List<String> findAllQuestion(List<Integer> subjects,String collectionName) {

        DBObject queryObject = new BasicDBObject();
//        queryObject.put("status",2);
        if(CollectionUtils.isNotEmpty(subjects)){
            queryObject.put("subject",new BasicDBObject("$in", subjects));
        }
        DBObject fieldsObject = new BasicDBObject();
        fieldsObject.put("_id",1);
        DBCursor dbCursor =mongoTemplate.getCollection(collectionName).find(queryObject,fieldsObject);
        List<String> list= Lists.newArrayList();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            list.add(String.valueOf(object.get("_id")));
        }
        return list;
    }

    public void groupBy(List<String> questions,String collectionName) {
        String groupStr = "{$group:{_id:{subject:\"$subject\",\"type\":\"$type\",\"status\":\"$status\"},total : {$sum : 1}}}";
        DBObject group = (DBObject) JSON.parse(groupStr);

        String matchStr = String.format("{$match:{_id:{$in:[%s]}}}", StringUtils.join(questions,","));
        logger.info("matchStr={}",matchStr);
        String sortStr = "{$sort:{total:-1}}";
        DBObject sort = (DBObject) JSON.parse(sortStr);
        DBObject match = (DBObject) JSON.parse(matchStr);

        List<DBObject> pipeline = new ArrayList<>();
        pipeline.add(group);
        pipeline.add(match);
        pipeline.add(sort);

        AggregationOutput output = mongoTemplate.getCollection(collectionName).aggregate(pipeline);
        Iterator<DBObject> iterator = output.results().iterator();
        while(iterator.hasNext()) {
            BasicDBObject dbo = (BasicDBObject)iterator.next();
            String id = dbo.getString("_id");
            int total = dbo.getInt("total");
            logger.info("collection={},groupBy={}, total={}",collectionName,id,total);
        }
    }


    public List<Question> findByIdGtAndLimit(int startIndex, int offset) {
        Criteria criteria = Criteria.where("id").gt(startIndex);
        Query query = new Query(criteria);
        query.limit(offset);
        return mongoTemplate.find(query, Question.class, collection);
    }
}
