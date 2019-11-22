package com.huatu.ztk.knowledge.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.QuestionMetaFlag;
import com.huatu.ztk.question.bean.QuestionUserMeta;
import com.huatu.ztk.question.bean.ReflectQuestion;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng
 * Created time 2019-06-03
 */

@Repository
public class QuestionUserMetaDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionUserMetaDao.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 通过id批量查询
     *
     * @return
     */
    public List<QuestionUserMeta> findBath(long uid, int pointId, int level) {
        Criteria criteria = Criteria.where("userId").is(uid);
        criteria.and(getQueryKey(level)).is(pointId);

        //通过id批量获取
        Query query = new Query(criteria);
        final List<QuestionUserMeta> records = mongoTemplate.find(query, QuestionUserMeta.class, getCollectionName(uid));
        return records;
    }

    /**
     * 更新用户该题的做题记录
     *
     * @return
     */
    public QuestionUserMeta findById(long uid, int qid) {
        return mongoTemplate.findById(generId(uid, qid), QuestionUserMeta.class, getCollectionName(uid));
    }

    /**
     * 组装id
     *
     * @param uid
     * @param qid
     * @return
     */
    public String generId(long uid, int qid) {
        return new StringBuilder(uid + "").append("_").append(qid).toString();
    }

    /**
     * 用用户id进行分表,分为32个
     *
     * @param uid
     * @return
     */
    private String getCollectionName(long uid) {
        return "ztk_question_user_meta_" + (uid % 32);
    }

    /**
     * 按照做错次数倒叙查询所有用户特定知识点下做过的试题
     *
     * @param uid      用户ID
     * @param pointIds 三级知识点
     * @param size     limit数量
     * @return
     */
    public List<QuestionUserMeta> findBathForErrorCount(long uid, List<Integer> pointIds, int size) {
        Criteria criteria = Criteria.where("userId").is(uid).and("status").is(2);
        criteria.and("thirdPointId").in(pointIds);
        //通过id批量获取
        Query query = new Query(criteria).with(new Sort(Sort.Direction.DESC, "errorFlag", "errorCount")).limit(size);
        final List<QuestionUserMeta> records = mongoTemplate.find(query, QuestionUserMeta.class, getCollectionName(uid));
        return records;
    }

    /**
     * 查询所有错题本试题及对应的三级知识点ID
     *
     * @param userId
     * @return
     */
    public Map<Integer, Integer> countErrorQuestion(long userId) {
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("userId", userId);
        query1.put("errorFlag", 1);
        query1.put("status", new BasicDBObject("$in", Lists.newArrayList(1,2)));
        return countQuestion(userId, query1);
    }

    /**
     * 查询所有做过的抽题池试题及对应的三级知识点ID
     *
     * @param userId
     * @return
     */
    public Map<Integer, Integer> countFinishQuestion(long userId) {
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("userId", userId);
        query1.put("poolFlag", 1);
        query1.put("status", 2);
        return countQuestion(userId, query1);
    }

    public Map<Integer, Integer> countQuestion(long uid, DBObject query) {
        DBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
        fields.put("_id", 1);
        fields.put("pointsList", 1);
        DBCursor dbCursor = mongoTemplate.getCollection(getCollectionName(uid)).find(query, fields);
        Map<Integer, Integer> result = Maps.newHashMap();

        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            BasicDBList pointsList = (BasicDBList) object.get("pointsList");
            for (Object o : pointsList) {
                int point = Integer.parseInt(o.toString());
                Integer value = result.getOrDefault(point, 0);
                value++; //添加到
                result.put(point, value);
            }

        }
        logger.info("countQuestion result size ={},table={},query={}", result.size(),getCollectionName(uid),query);
        return result;
    }


//    public Map<String, Set<String>> getQuestionIds(long userId) {
//
//        Criteria criteria = Criteria.where("userId").is(userId)
//                .and("errorFlag").is(1)
//                .and("status").is(2);
//        Query query = new Query(criteria);
//
//        return knowQuestionIds(userId, query);
//    }

//    /**
//     * 查询知识下的试题ID
//     *
//     * @param uid
//     * @param query
//     * @return
//     */
//    public Map<String, Set<String>> knowQuestionIds(long uid, Query query) {
//
//        List<QuestionUserMeta> questionUserMetas = mongoTemplate.find(query, QuestionUserMeta.class, getCollectionName(uid));
//        Map<Integer, List<QuestionUserMeta>> allMap = Maps.newHashMap();
//
//        //知识点ID,对所有的进行分组
//        Map<Integer, List<QuestionUserMeta>> firstKnowQuestionIds = questionUserMetas.stream()
//                .collect(Collectors.groupingBy(QuestionUserMeta::getFirstPointId));
//        allMap.putAll(firstKnowQuestionIds);
//        logger.info("一级知识点ID:{}", JsonUtil.toJson(firstKnowQuestionIds));
//
//        Map<Integer, List<QuestionUserMeta>> secondKnowQuestionIds = questionUserMetas.stream()
//                .collect(Collectors.groupingBy(QuestionUserMeta::getSecondPointId));
//        allMap.putAll(secondKnowQuestionIds);
//
//        Map<Integer, List<QuestionUserMeta>> thirdKnowQuestionIds = questionUserMetas.stream()
//                .collect(Collectors.groupingBy(QuestionUserMeta::getThirdPointId));
//        allMap.putAll(thirdKnowQuestionIds);
//
//
//        Map<String, Set<String>> resultMap = Maps.newHashMap();
//        for (Map.Entry entry : allMap.entrySet()) {
//            List<QuestionUserMeta> allQuestion = (List<QuestionUserMeta>) entry.getValue();
//            Set<String> questionIds = allQuestion.stream().map(questionUserMeta -> String.valueOf(questionUserMeta.getQuestionId()))
//                    .collect(Collectors.toSet());
//            Map knowMap = Maps.newHashMap();
//            knowMap.put(String.valueOf(entry.getKey().toString()), questionIds);
//            resultMap.putAll(knowMap);
//        }
//
//        logger.info("结果是:{}", JsonUtil.toJson(resultMap));
//        return resultMap;
//    }


    /**
     * 查询所有错题本试题及对应的三级知识点ID
     *
     * @param userId
     * @return
     */
    public Set<Integer> findErrorQuestion(long userId, QuestionPoint questionPoint) {
        if (null == questionPoint) {
            return Sets.newHashSet();
        }
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("userId", userId);
        query1.put("errorFlag", 1);
        query1.put("status", new BasicDBObject("$in", Lists.newArrayList(1,2)));
        query1.put(getQueryKey(questionPoint.getLevel()), questionPoint.getId());
        return findQuestion(userId, query1);
    }

    /**
     * 查询所有做过的抽题池试题及对应的三级知识点ID
     *
     * @param userId
     * @return
     */
    public Set<Integer> findFinishQuestion(long userId, QuestionPoint questionPoint) {
        if (null == questionPoint) {
            return Sets.newHashSet();
        }
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("userId", userId);
        query1.put("poolFlag", 1);
        query1.put("status", 2);
        query1.put(getQueryKey(questionPoint.getLevel()), questionPoint.getId());
        return findQuestion(userId, query1);
    }

    private String getQueryKey(int level) {
        switch (level) {
            case 0:
                return "firstPointId";
            case 1:
                return "secondPointId";
            case 2:
                return "thirdPointId";
            default:
                return "_id";
        }
    }

    public Set<Integer> findQuestion(long uid, DBObject query) {
        DBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
        fields.put("_id", 1);
        fields.put("questionId", 1);
        DBCursor dbCursor = mongoTemplate.getCollection(getCollectionName(uid)).find(query, fields);
        Set<Integer> result = Sets.newHashSet();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            result.add(Integer.parseInt(object.get("questionId").toString()));
        }
        logger.info("findQuestion result size ={},table={},query={}", result.size(),getCollectionName(uid),query);
        return result;
    }

    /**
     * 保存答题卡
     *
     * @param meta
     */
    public void save(QuestionUserMeta meta) {
        mongoTemplate.save(meta, getCollectionName(meta.getUserId()));
    }

    /**
     * 用户删除错题
     */
    public void clearErrorQuestions(long uid, int questionId) {
        Criteria criteria;
        if (questionId > 0) {
            criteria = Criteria.where("_id").is(generId(uid,questionId));
        }else {
            criteria = Criteria.where("userId").is(uid);
        }
        criteria.and("errorFlag").is(QuestionMetaFlag.erroFlag.ERROR.getKey()).and("status").is(2);
        Query query = new Query(criteria);
        Update update = new Update().set("errorFlag", QuestionMetaFlag.erroFlag.right.getKey());
        mongoTemplate.updateMulti(query, update, QuestionUserMeta.class, getCollectionName(uid));
    }

    /**
     * 批量添加用户错题
     */
    public void addErrorQuestions(long uid, List<Integer> questionIds) {
        Criteria criteria;
        if (CollectionUtils.isNotEmpty(questionIds)) {
            criteria = Criteria.where("_id").in(questionIds.stream().map(i->generId(uid,i)).collect(Collectors.toList()));
        }else {
            criteria = Criteria.where("userId").is(uid);
        }
        Query query = new Query(criteria);
        Update update = new Update().set("errorFlag", QuestionMetaFlag.erroFlag.ERROR.getKey());
        mongoTemplate.updateMulti(query, update, QuestionUserMeta.class, getCollectionName(uid));
    }
}
