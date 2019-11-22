package com.huatu.tiku.teacher.service;

import com.google.common.collect.Maps;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/9/12.
 */
@Slf4j
public class InnerServeiceT extends TikuBaseTest {

    @Autowired
    InnerServiceImpl innerService;
    @Autowired
    RedisTemplate redisTemplate;
    @Test
    public void test(){
        int subject =  1;
        int knowledgeId = 396;
        int flag = 1;
        String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(knowledgeId);
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Set<byte[]> bytes = null;
        try{
            bytes = connection.sMembers(pointQuesionIdsKey.getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            connection.close();
        }
        Map<String, Object> attrMap = Maps.newHashMap();
        attrMap.put("status", 2);
        attrMap.put("subject", subject);
        //选填
        if (1 == subject) {
            attrMap.put("year", 1);
            attrMap.put("mode", 1);
            attrMap.put("type", 1);
        }
        //试题知识点查询
        String collectionName = flag == 1 ?"ztk_question_new":"ztk_question";
        Map<Integer, List<Integer>> map = innerService.findByAttrs(attrMap, collectionName);
        HashSet<Integer> sets = Sets.newHashSet();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            boolean contains = entry.getValue().contains(knowledgeId);
            if(contains){
                sets.add(entry.getKey());
            }
        }
        if(CollectionUtils.isNotEmpty(bytes)){
            List<Integer> collect = bytes.stream().map(String::new).map(Integer::parseInt).collect(Collectors.toList());
            collect.removeAll(sets);
            System.out.println("多出的试题ID: "+ StringUtils.join(collect,","));
        }
    }
}
