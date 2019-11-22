package com.huatu.ztk.backend.paper.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.metas.bean.AnswerCardSub;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 答题卡dao层
 * Created by shaojieyue
 * Created time 2016-05-03 14:09
 */

@Repository
public class AnswerCardDao {
    private static final Logger logger = LoggerFactory.getLogger(AnswerCardDao.class);

    /**
     * 存储试题的集合名字
     */
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据id查询答题卡
     * @return
     */
    public AnswerCard findById(long practiceId) {
        final AnswerCard answerCard = mongoTemplate.findById(practiceId, AnswerCard.class);
        return answerCard;
    }

    public List<AnswerCard> findByIds(List<Long> practiceIds ){
        Criteria criteria = Criteria.where("id").in(practiceIds);
        return mongoTemplate.find(new Query(criteria), AnswerCard.class);
    }

    /**
     * 根据练习id，查询userId
     * @param practiceIds
     * @return
     */
    public Map<Long,Long> findUserIdByIds(List<Long> practiceIds ){
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("_id", new BasicDBObject("$in", practiceIds));
        DBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
        fields.put("_id", 1);
        fields.put("userId", 1);
        DBCursor dbCursor =mongoTemplate.getCollection("ztk_answer_card").find(query1,fields);
        Map<Long,Long> result = Maps.newHashMap();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            result.put(Long.parseLong(object.get("_id").toString()),Long.parseLong(object.get("userId").toString()));
        }
        logger.info("result size ={}",result.size());
        return result;
    }
    public List<AnswerCard> findByIdsV2(List<Long> practiceIds ){
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("_id", new BasicDBObject("$in", practiceIds));
        DBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
        fields.put("_id", 1);
        fields.put("createTime", 1);
        fields.put("score", 1);
        fields.put("answers", 1);
        DBCursor dbCursor =mongoTemplate.getCollection("ztk_answer_card").find(query1,fields);
        List<AnswerCard> list= Lists.newArrayList();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            StandardCard answerCard = new StandardCard();
            answerCard.setId(Long.parseLong(object.get("_id").toString()));
            answerCard.setCreateTime(Long.parseLong(object.get("createTime").toString()));
            answerCard.setScore(Double.parseDouble(object.get("score").toString()));
            BasicDBList basicDBList = (BasicDBList)object.get("answers");
            boolean flag = false;
            for(Object obj :basicDBList){
                if(!obj.toString().equals("0")){
                    flag = true;
                    break;
                }
            }
            if(flag){
                answerCard.setLastIndex(1);
            }else{
                answerCard.setLastIndex(0);
            }
            list.add(answerCard);
        }
        return list;
    }

    /**
     * 查询答题卡答题的情况，比如答题用时/是否正确
     * @param practiceIds
     * @return
     */
    public List<AnswerCardSub> findAllByIds(List<Long> practiceIds ,String name) {

        DBObject queryObject = new BasicDBObject();
        queryObject.put("_id",new BasicDBObject("$in", practiceIds));
        DBObject fieldsObject = new BasicDBObject();
        fieldsObject.put("_id",1);
        fieldsObject.put(name,1);
        DBCursor dbCursor =mongoTemplate.getCollection("ztk_answer_card").find(queryObject,fieldsObject);
        List<AnswerCardSub> list= Lists.newArrayList();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            AnswerCardSub answerCard = new AnswerCardSub();
            answerCard.setId(Long.parseLong(object.get("_id").toString()));
            BasicDBList basicDBList = (BasicDBList)object.get(name);
            answerCard.setArrays(basicDBList.stream().map(i->String.valueOf(i)).toArray());
            list.add(answerCard);
        }
        return list;
    }


}
