package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-05-20 12:00
 */
public class QuestionPointDubboServerImplTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionPointDubboServerImplTest.class);

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private QuestionPointService questionPointService;

    @Test
    public void randomPointTest(){
        for (int i = 0; i < 1000; i++) {
            QuestionPoint questionPoint = questionPointDubboService.randomPoint();
            logger.info(JsonUtil.toJson(questionPoint));
            Assert.assertNotNull(questionPoint);
        }
    }

    @Test
    public void aaTest(){
        final List<QuestionPointTree> questionPointTrees = questionPointDubboService.questionPointSummary(new ArrayList<>(), new int[0], new int[0]);
        System.out.println(questionPointTrees);
    }


    @Test
    public void randomPointATest(){
        int count = 5;
        List<QuestionPoint> questionPoints = null;
        questionPoints = questionPointDubboService.randomPoint(392, count);
        Assert.assertEquals(questionPoints.size(),5);

        questionPoints = questionPointDubboService.randomPoint(393,count);
        Assert.assertEquals(questionPoints.size(),4);

        questionPoints = questionPointDubboService.randomPoint(394,count);
        Assert.assertEquals(questionPoints.size(),1);
    }

    @Test
    public void findTrainPointTest() {
        List<QuestionPoint> points = questionPointDubboService.findDayTrainPoints(239, 1, 5);

        Assert.assertEquals(5, points.size());

        System.out.println(JsonUtil.toJson(points));
    }

}
