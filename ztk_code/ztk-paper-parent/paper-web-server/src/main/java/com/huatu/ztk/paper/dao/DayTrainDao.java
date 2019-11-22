package com.huatu.ztk.paper.dao;

import com.huatu.ztk.paper.bean.DayTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by shaojieyue
 * Created time 2016-05-20 22:20
 */

@Repository
public class DayTrainDao {
    private static final Logger logger = LoggerFactory.getLogger(DayTrainDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    public DayTrain findById(String id) {
        return mongoTemplate.findById(id,DayTrain.class);
    }

    public void update(DayTrain dayTrain){
        mongoTemplate.save(dayTrain);
    }

    public void insert(DayTrain dayTrain) {
        mongoTemplate.insert(dayTrain);
    }
}
