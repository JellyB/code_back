package com.huatu.ztk.monitor;

import com.huatu.ztk.monitor.task.LogReadTask;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * Created by shaojieyue
 * Created time 2017-02-07 11:37
 */


public class WarnTaskTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(WarnTaskTest.class);

    @Autowired
    private LogReadTask logReadTask;

    @Test
    public void aaTest(){
        for (int i = 0; i < 10000; i++) {
            final MessageProperties messageProperties = new MessageProperties();
            messageProperties.setAppId("knowledge-web-server.192.168.100.24");
            messageProperties.getHeaders().put("level","ERROR");
            logReadTask.onMessage(new Message("".getBytes(),messageProperties));
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
