package com.huatu.ztk.paper.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.paper.bean.DayTrainSettings;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 每日特训设置服务层
 * Created by shaojieyue
 * Created time 2016-05-20 17:22
 */

@Repository
public class DayTrainSettingsDao {
    private static final Logger logger = LoggerFactory.getLogger(DayTrainSettingsDao.class);
    /**
     * 存储每日特训设置的集合名字
     */
    public static final String collection = "user_day_train_settings";

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据用户id查询
     * @param userId
     * @return
     */
    public DayTrainSettings findByUserId(long userId,int subject) {
        final Criteria criteria = Criteria.where("userId").is(userId);

        if (subject == SubjectType.GWY_XINGCE) {
            //特殊处理,之前的数据没有subject字段
            criteria.orOperator(Criteria.where("subject").is(subject), Criteria.where("subject").exists(false));
        } else {
            criteria.and("subject").is(subject);
        }

        Query query = new Query(criteria);
        final List<DayTrainSettings> trainSettingses = mongoTemplate.find(query, DayTrainSettings.class);
        DayTrainSettings dayTrainSettings = null;
        if (CollectionUtils.isNotEmpty(trainSettingses)) {
            dayTrainSettings = trainSettingses.get(0);
        }
        return dayTrainSettings;
    }

    /**
     * 整体更新每日训练设置
     * @param dayTrainSettings
     */
    public void update(DayTrainSettings dayTrainSettings) {
        logger.info("update day train settings: data={}", JsonUtil.toJson(dayTrainSettings));
        mongoTemplate.save(dayTrainSettings);
    }

    /**
     * 插入每日训练设置
     * @param dayTrainSettings
     */
    public void insert(DayTrainSettings dayTrainSettings) {
        logger.info("insert day train settings: data={}", JsonUtil.toJson(dayTrainSettings));
        mongoTemplate.insert(dayTrainSettings);
    }

    public DayTrainSettings findById(long id) {
        return mongoTemplate.findById(id,DayTrainSettings.class);
    }
}
