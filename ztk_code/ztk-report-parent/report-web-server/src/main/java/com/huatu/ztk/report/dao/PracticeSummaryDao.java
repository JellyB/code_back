package com.huatu.ztk.report.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.report.bean.PracticeSummary;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * Created by linkang on 10/19/16.
 */
@Repository
public class PracticeSummaryDao {
    private static final Logger logger = LoggerFactory.getLogger(PracticeSummaryDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 获得每月统计id  uid+subject+yyyyMM
     * @param uid
     * @param subject
     * @return
     */
    public String getMonthSummaryId(long uid, int subject) {
        return uid + subject + DateFormatUtils.format(System.currentTimeMillis(), "yyyyMM");
    }

    /**
     * 获得总统计id uid+subject+-1
     * @param uid
     * @param subject
     * @return
     */
    public String getTotalSummaryId(long uid, int subject) {
        return uid + subject + "-1";
    }

    /**
     * 根据id更新练习统计信息
     * @param id
     * @param update
     */
    public void updateSummary(String id,Update update){
        final Query query = Query.query(Criteria.where("_id").is(id));
        mongoTemplate.updateFirst(query, update, PracticeSummary.class);
    }

    /**
     * 根据id查询统计
     * @param summaryId
     * @return
     */
    public PracticeSummary findById(String summaryId) {
        return mongoTemplate.findById(summaryId, PracticeSummary.class);
    }

    /**
     * 新建一条统计记录
     * @param practiceSummary
     */
    public void insert(PracticeSummary practiceSummary) {
        logger.info("insert practiceSummary={}", JsonUtil.toJson(practiceSummary));
        mongoTemplate.insert(practiceSummary);
    }


}
