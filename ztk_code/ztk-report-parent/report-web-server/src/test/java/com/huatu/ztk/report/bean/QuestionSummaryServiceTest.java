package com.huatu.ztk.report.bean;

import com.huatu.ztk.report.BaseTest;
import com.huatu.ztk.report.service.QuestionSummaryService;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Created by shaojieyue
 * Created time 2016-05-29 08:20
 */
public class QuestionSummaryServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionSummaryServiceTest.class);

    @Autowired
    private QuestionSummaryService questionSummaryService;



    private long uid = 12252065;
    private int subject = 1;

    @Test
    public void findByUserIdTest(){
        final QuestionSummary questionSummary = questionSummaryService.findByUserId(uid, subject);
        Assert.assertNotNull(questionSummary);
    }

    @Test
    public void updateTest(){
        for (int i = 0; i < 100; i++) {
            final QuestionSummary old = questionSummaryService.findByUserId(uid, subject);
            Random random = new Random();
            int wcount = random.nextInt(100);
            int rcount = random.nextInt(100);
            int times = random.nextInt(10000);
            final int count = old.getRsum() + old.getWsum() + wcount + rcount;
            //重新计算正确率,保留一位小数
            double accuracy = 100*(rcount+old.getRsum())/count;
            final QuestionSummary questionSummary = questionSummaryService.update(uid, subject, wcount, rcount,times, -9);
            Assert.assertEquals(questionSummary.getWsum(),wcount+old.getWsum());
            Assert.assertEquals(questionSummary.getRsum(),rcount+old.getRsum());
            Assert.assertEquals(questionSummary.getAsum(),count);
            Assert.assertEquals(questionSummary.getTimes(),times+old.getTimes());
            Assert.assertEquals(questionSummary.getSpeed(),(times+old.getTimes())/ count);
            Assert.assertEquals(questionSummary.getAccuracy(),accuracy,0.001);
        }


    }
}
