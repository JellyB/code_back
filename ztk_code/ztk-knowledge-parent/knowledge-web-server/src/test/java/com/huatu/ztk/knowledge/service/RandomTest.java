package com.huatu.ztk.knowledge.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by linkang on 11/15/16.
 */
public class RandomTest extends BaseTest{
    @Autowired
    private QuestionStrategyDubboServiceImpl questionStrategyDubboService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void dddddd() {
        HashOperations<String,String,String> hashOperations = redisTemplate.opsForHash();
        String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(233906356);
        ArrayList<String> strings = Lists.newArrayList("2", "3", "1", "4");
        List<String> strings1 = hashOperations.multiGet(wrongCountKey, strings);
        System.out.println(strings1);
        System.out.println(strings1.stream().map(String::valueOf).collect(Collectors.joining(",")));
        System.out.println(strings1.stream().map(i->{
            if(StringUtils.isBlank(i)){
                return 0;
            }else{
                return Integer.parseInt(i);
            }
        }).collect(Collectors.toList()));


    }
}
