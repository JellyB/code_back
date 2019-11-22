package com.huatu.ztk.user.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.user.bean.CourseRecord;
import com.huatu.ztk.user.bean.LearnRecord;
import com.huatu.ztk.user.bean.PractiseRecord;
import com.huatu.ztk.user.common.RecordType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-23  15:02 .
 */
@Service
public class LearnRecordService {
    private static final Logger logger = LoggerFactory.getLogger(LearnRecordService.class);

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 查找用户的学习记录
     * @param uid
     * @return
     */
    public Object findLearnRecord(long uid){
        /*Map<String,String> learnRecords = new HashMap();
        LearnRecord learnRecord = LearnRecord.builder()
                .endTime(1112232342)
                .name("test")
                .type(RecordType.PRACTISE)
                .build();
        String recordStr = JsonUtil.toJson(learnRecord);
        for(int i=0;i<5;i++){
            int num = 100+i;
            learnRecords.put(num+"",recordStr);
        }
        learnRecords.put(200+"",recordStr);
        final HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        try {
            hashOperations.putAll("learnRecord"+uid,learnRecords);
        }catch (Exception e){
            logger.error("ex,sessionInfo={}", JsonUtil.toJson(uid),e);
        }
        redisTemplate.expire("learnRecord"+uid,60, TimeUnit.SECONDS);//1分钟过期
        */

        BoundHashOperations<String, String, Object> boundHashOperations = redisTemplate.boundHashOps("learnRecord"+uid);
        logger.info("uid={},boundHashOperations={}",uid,boundHashOperations);
        Map<String,Object> resultMap = boundHashOperations.entries();

        logger.info("resultMap={}",resultMap);

        List<LearnRecord> resultRecords = new ArrayList<>();


        Iterator iter = resultMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            int key = Integer.parseInt((String) entry.getKey());
            if(key/100==0){
                CourseRecord courseRecord = JsonUtil.toObject((String)entry.getValue(),CourseRecord.class);
                resultRecords.add(courseRecord);
            }else {
                PractiseRecord practiseRecord = JsonUtil.toObject((String)entry.getValue(),PractiseRecord.class);
                resultRecords.add(practiseRecord);
            }
        }
        return resultRecords;
    }



}
