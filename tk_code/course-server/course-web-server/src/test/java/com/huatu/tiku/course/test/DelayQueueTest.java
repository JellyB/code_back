package com.huatu.tiku.course.test;

import com.alibaba.fastjson.JSONObject;
import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.course.spring.conf.queue.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-02-26 下午3:29
 **/
@Slf4j
public class DelayQueueTest extends BaseWebTest {

    @Autowired
    private RedisDelayQueue redisDelayQueue;



    @Test
    public void testCreate() throws InterruptedException {
        Message message = new Message();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Payload payload = Payload.builder().syllabusId(i).userName( "test" + i).build();
            message.setId("messageId" + i);
            message.setPayload(payload);
            message.setPriority(0);
            message.setTimeout(random.nextInt(100) * 1000);
            redisDelayQueue.push(message);
            log.info("message info :{}", JSONObject.toJSONString(message));
        }
        redisDelayQueue.listen();
    }
}
