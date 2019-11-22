package com.huatu.ztk.pc.dao;

import com.huatu.ztk.pc.bean.Share;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by shaojieyue
 * Created time 2016-09-19 20:39
 */

@Repository
public class ShareDao {
    private static final Logger logger = LoggerFactory.getLogger(ShareDao.class);
    @Autowired
    private MongoTemplate mongoTemplate;


    public void insert(Share share) {
        mongoTemplate.insert(share);
    }

    public Share findById(String id){
        return mongoTemplate.findById(id,Share.class);
    }
}
