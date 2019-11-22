package com.ht.galaxy.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.ht.base.annotation.log.LogToolNB;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author zhengyi
 * @date 2018/7/31 2:25 PM
 **/
@EnableApolloConfig("tiku.mongo-cluster")
@Configuration
public class MongoConfig {
}