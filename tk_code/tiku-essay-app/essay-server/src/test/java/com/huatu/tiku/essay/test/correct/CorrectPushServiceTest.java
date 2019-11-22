package com.huatu.tiku.essay.test.correct;

import com.huatu.tiku.essay.service.CorrectPushService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-03 6:59 PM
 **/

@Slf4j
public class CorrectPushServiceTest extends TikuBaseTest {

    @Autowired
    private CorrectPushService correctPushService;


    @Test
    public void report(){

        long answerCardID = 625;

        int answerCardType = 0;

        correctPushService.correctReport4Push(answerCardID, answerCardType);
    }
}
