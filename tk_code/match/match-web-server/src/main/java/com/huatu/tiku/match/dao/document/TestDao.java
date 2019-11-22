package com.huatu.tiku.match.dao.document;

import com.huatu.tiku.match.bean.document.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lijun on 2018/10/11
 */
@Repository
public class TestDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<BaseTest> listAll(){
        return mongoTemplate.findAll(BaseTest.class);
    }
}
