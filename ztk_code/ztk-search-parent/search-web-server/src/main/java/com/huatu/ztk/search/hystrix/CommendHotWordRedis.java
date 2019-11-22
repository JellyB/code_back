package com.huatu.ztk.search.hystrix;

import com.google.common.collect.Lists;
import com.huatu.ztk.search.util.SpringTool;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.huatu.ztk.search.bean.KeyWordSearchBeanNew.HOT_WORD_REDIS_KEY;

/**
 * @author zhengyi
 * @date 2019-03-07 15:22
 **/
public class CommendHotWordRedis extends HystrixCommand<List<String>> {

    private static final Logger log = LoggerFactory.getLogger(CommendHotWordRedis.class);

    private long userId;
    private int catgory;

    CommendHotWordRedis(long userId, int catgory) {
        super(HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("jbzm-nb-redis"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutEnabled(true)
                        .withExecutionTimeoutInMilliseconds(100)));
        this.userId = userId;
        this.catgory = catgory;
    }


    @Override
    protected List<String> run() {
        RedisTemplate redisTemplate = SpringTool.getApplicationContext().getBean(RedisTemplate.class);
        String keySource = (String) redisTemplate.opsForValue().get(HOT_WORD_REDIS_KEY + userId + catgory);
        return Lists.newArrayList(keySource.split(","));
    }

    @Override
    protected List<String> getFallback() {
        return new ArrayList<>();
    }
}