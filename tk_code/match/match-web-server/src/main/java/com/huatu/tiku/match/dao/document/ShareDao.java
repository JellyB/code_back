package com.huatu.tiku.match.dao.document;

import com.huatu.ztk.pc.bean.Share;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by shaojieyue
 * Created time 2016-09-19 20:39
 */
@Slf4j
@Repository
public class ShareDao {
    @Autowired
    private MongoTemplate mongoTemplate;


    public void save(Share share) {
        mongoTemplate.save(share,"ztk_share");
    }

    public Share findById(String id){
        return mongoTemplate.findById(id,Share.class);
    }
}
