package com.huatu.tiku.match.initTest;

import com.huatu.common.test.BaseWebTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-17 下午4:56
 **/
@Slf4j
public class RewardTest extends BaseWebTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Test
    public void test(){
        Map data = new HashMap<>(4);
        data.put("id", 362322357889146405L);
        data.put("type", 7);
        //发送提交试卷的事件
        rabbitTemplate.convertAndSend("submit_practice_exchange", "", data);

        int dayOfYear = Calendar.getInstance().get(6);
        log.error(">>>>>>>> ：{}", dayOfYear);
    }
}
