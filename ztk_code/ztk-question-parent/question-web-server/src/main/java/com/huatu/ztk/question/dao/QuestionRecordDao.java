package com.huatu.ztk.question.dao;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionRecord;
import com.mongodb.WriteResult;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-09-08 14:07
 */

@Repository
public class QuestionRecordDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionRecordDao.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 通过id批量查询
     * @return
     */
    public List<QuestionRecord> findBath(long uid,int[] qids) {

        //转换为id
        final String[] ids = Arrays.stream(qids).mapToObj(qid -> generId(uid, qid)).toArray(String[]::new);
        //通过id批量获取
        Query query = new Query(Criteria.where("_id").in(ids));
        final List<QuestionRecord> records = mongoTemplate.find(query, QuestionRecord.class,getCollectionName(uid));
        return records;
    }

    /**
     * 更新用户该题的做题记录
     * @param answer
     * @param time
     * @return
     */
    public boolean update(long uid,int qid,int answer,int time){
        Update update = new Update().push("answers",answer).push("times",time);
        Query query = new Query(Criteria.where("_id").is(generId(uid, qid)));
        WriteResult writeResult = mongoTemplate.updateFirst(query,update,QuestionRecord.class,getCollectionName(uid));
        return writeResult.getN()>0;
    }

    /**
     * 批量插入记录
     * @param list
     */
    public void insert(List<QuestionRecord> list){
        Multimap<Long,QuestionRecord> data = ArrayListMultimap.create();

        //按用户进行分组
        for (QuestionRecord questionRecord : list) {
            //重新设置id
            questionRecord.setId(generId(questionRecord.getUid(),questionRecord.getQid()));
            //分组放入多值数组
            data.put(questionRecord.getUid(),questionRecord);
        }

        for (Long uid : data.keySet()) {
            mongoTemplate.insert(data.get(uid),getCollectionName(uid));
        }
    }

    /**
     * 组装id
     * @param uid
     * @param qid
     * @return
     */
    private String generId(long uid,int qid){
        return new StringBuilder(uid+"").append("_").append(qid).toString();
    }

    /**
     * 用用户id进行分表,分为32个
     * @param uid
     * @return
     */
    private String getCollectionName(long uid){
        return "ztk_question_record_"+(uid%32);
    }

}
