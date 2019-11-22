package com.huatu.tiku.course.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.course.util.CourseCacheKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-04-19 3:42 PM
 **/
@Slf4j
public class SyllaBusInfoServiceTest extends BaseWebTest {


    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test(){
        String key = CourseCacheKey.getProcessLogSyllabusDealList();
        ZSetOperations<String, String> dealList = redisTemplate.opsForZSet();
        long max = System.currentTimeMillis();
        Set<Long> syllabusIds = dealList.rangeByScore(key, 0, max).stream().map(Long::valueOf).collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(syllabusIds)){
            return;
        }
        dealList.remove(key, String.valueOf(0L));
    }
}
