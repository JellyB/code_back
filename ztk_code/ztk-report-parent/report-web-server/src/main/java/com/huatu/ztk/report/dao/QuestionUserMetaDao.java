package com.huatu.ztk.report.dao;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.huatu.ztk.question.bean.QuestionUserMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        switch (level) {
            case 1:
                criteria.and("firstPointId").is(pointId);
                break;
            case 2:
                criteria.and("secondPointId").is(pointId);
                break;
            case 3:
                criteria.and("thirdPointId").is(pointId);
                break;
            default:
                return Lists.newArrayList();
        }
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
     * 批量插入记录
     *
     * @param list
     */
    public void insert(List<QuestionUserMeta> list) {
        Multimap<Long, QuestionUserMeta> data = ArrayListMultimap.create();

        //按用户进行分组
        for (QuestionUserMeta questionRecord : list) {
            //重新设置id
            questionRecord.setId(generId(questionRecord.getUserId(), questionRecord.getQuestionId()));
            //分组放入多值数组
            data.put(questionRecord.getUserId(), questionRecord);
        }

        for (Long uid : data.keySet()) {
            mongoTemplate.insert(data.get(uid), getCollectionName(uid));
        }
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
     * 保存答题卡
     * @param meta
     */
    public void save(QuestionUserMeta meta) {
        mongoTemplate.save(meta, getCollectionName(meta.getUserId()));
    }
}
