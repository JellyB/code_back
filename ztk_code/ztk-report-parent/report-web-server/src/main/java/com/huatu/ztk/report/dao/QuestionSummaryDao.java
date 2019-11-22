package com.huatu.ztk.report.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.report.bean.QuestionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * 试题总结
 * Created by shaojieyue
 * Created time 2016-05-28 20:50
 */

@Repository
public class QuestionSummaryDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionSummaryDao.class);


    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 通过用户和考试科目查询统计信息
     * @param userId 用户id
     * @param sujectId 科目id
     * @return
     */
    public QuestionSummary findByUserId(long userId,int sujectId){
        String id = getId(userId,sujectId);
        return mongoTemplate.findById(id,QuestionSummary.class);
    }

    public void update(QuestionSummary questionSummary){
        logger.info("update QuestionSummary,data={}", JsonUtil.toJson(questionSummary));
        mongoTemplate.save(questionSummary);
    }

    /**
     * 插入
     * @param questionSummary
     */
    public void insert(QuestionSummary questionSummary){
        final int subject = questionSummary.getSubject();
        final long uid = questionSummary.getUid();
        final String id = getId(uid, subject);
        questionSummary.setId(id);
        logger.info("insert QuestionSummary,data={}", JsonUtil.toJson(questionSummary));
        mongoTemplate.insert(questionSummary);
    }



    /**
     * 组装id
     * @param userId
     * @param sujectId
     * @return
     */
    private String getId(long userId, int sujectId) {
        final String id = userId+"_"+sujectId;
        return id;
    }
}
