package com.huatu.ztk.user.dao;

import com.huatu.ztk.user.bean.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by shaojieyue
 * Created time 2016-12-20 11:10
 */
@Repository
public class UserConfigDao {
    private static final Logger logger = LoggerFactory.getLogger(UserConfigDao.class);

    /**
     * 存储用户的基础配置
     */
    public static final String collection = "ztk_user_config";
    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 根据uid，查询是否有该用户
     * @return
     */
    public UserConfig findById(String id){
        final UserConfig userConfig = mongoTemplate.findById(id,UserConfig.class);
        return userConfig;
    }

    /**
     * 插入用户基础配置
     * @param config 用户基础配置
     */
    public void save(UserConfig config){
        mongoTemplate.save(config,collection);
    }
}
