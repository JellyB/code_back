package com.huatu.ztk.knowledge.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.bean.PointSummary;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-06-16 09:50
 */

@Repository
public class PointSummaryDao {
    private static final Logger logger = LoggerFactory.getLogger(PointSummaryDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据id查询知识点
     * @param id
     * @return
     */
    public PointSummary findById(String id) {
        return mongoTemplate.findById(id, PointSummary.class);
    }

    /**
     * 插入知识点汇总
     * @param pointSummary
     */
    public void insert(PointSummary pointSummary) {
        logger.info("insert PointSummary, data={}",JsonUtil.toJson(pointSummary));
        if (StringUtils.isBlank(pointSummary.getId())) {
            throw new IllegalStateException("PointSummary id is null,insert fail. data=" +JsonUtil.toJson(pointSummary));
        }
        mongoTemplate.insert(pointSummary);
    }

    /**
     * 更新知识点统计
     * @param pointSummary
     */
    public void save(PointSummary pointSummary) {
        logger.info("update PointSummary,data={}",JsonUtil.toJson(pointSummary));
        mongoTemplate.save(pointSummary);
    }

    /**
     * 查询用户指定科目的知识点
     * @param uid
     * @param subject
     * @return
     */
    public List<PointSummary> findUserPointSummary(long uid, int subject) {
        final Criteria criteria = Criteria.where("uid").is(uid)
                    .and("subject").is(subject);
        Query query = new Query(criteria);
        List<PointSummary> pointSummaryList = mongoTemplate.find(query, PointSummary.class);
        if (pointSummaryList == null) {
            pointSummaryList = new ArrayList<PointSummary>();
        }
        return pointSummaryList;
    }
}
