package com.huatu.ztk.knowledge.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.CompositeQuestion;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * Created by shaojieyue
 * Created time 2016-05-18 19:37
 */


public class QuestionStrategyDubboServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionStrategyDubboServiceTest.class);

    @Autowired
    private QuestionStrategyDubboService questionStrategyDubboService;

    @Autowired
    private QuestionDubboService questionDubboService;

    private long uid = 233358929;
    private int subject = 1;

//
//    @Test
//    public void filterFinishedTest(){
//        Set<Integer> finishedSet = Sets.newSet(55286,55424,55976,56024,56165,56483,56507,57599,57649,59743,61300,61419);
//        //,56483,56507,57599,57649,59743,61300,61419
//        for (int i = 0; i < 100; i++) {
//            final int count = 13;
//            final QuestionStrategy questionStrategy = questionStrategyDubboService.randomStrategy(uid, subject, 625, count);
//            Assert.assertEquals(CollectionUtils.retainAll(questionStrategy.getQuestions(),finishedSet).size(),0);
//            Assert.assertEquals(questionStrategy.getQuestions().size(), count);
//        }
//
//    }



    @Test
    public void randomStrategyTest(){
        final int pointId = 393;
        final int size = 15;
        QuestionStrategy questionStrategy = questionStrategyDubboService.randomStrategy(uid, subject, pointId, size);
//        Assert.assertEquals(30,questionStrategy.getQuestions().size());
        logger.info("--->list="+ JsonUtil.toJson(questionStrategy));


    }

//    @Test
//    public void randomStrategyNoUserTest(){
//        final int pointId = 30;
//        final int size = 50;
//        List<Integer> list = null;
//        QuestionStrategy questionStrategy = null;
//        for (int i = 0; i < 10; i++) {
//            questionStrategy = questionStrategyDubboService.randomStrategyNoUser(subject, 642, size);
//            Assert.assertEquals(30,questionStrategy.getQuestions().size());
//            logger.info("--->list="+ JsonUtil.toJson(questionStrategy));
//        }
//    }
//
//    @Test
//    public void randomErrorStrategyTest(){
//        long uid = 12252065;
//        final int size = 30;
//        QuestionStrategy questionStrategy = questionStrategyDubboService.randomErrorStrategy(uid, -1, 1, size);
//        questionStrategy = questionStrategyDubboService.randomErrorStrategy(uid, 435, 1, size);
//        Assert.assertEquals(questionStrategy.getModules().get(0).getQcount() ,questionStrategy.getQuestions().size());
//        Assert.assertTrue(questionStrategy.getQuestions().size()>0);
//        Assert.assertTrue(questionStrategy.getQuestions().size()== size);
//
//        questionStrategy = questionStrategyDubboService.randomErrorStrategy(uid, 439, 1, size);
//        Assert.assertEquals(questionStrategy.getModules().get(0).getQcount() ,questionStrategy.getQuestions().size());
////        Assert.assertTrue(questionStrategy.getQuestions().size()== 2);
////        List<Integer> list = Lists.newArrayList(30901,30904);
////        CollectionUtils.isEqualCollection(questionStrategy.getQuestions(),list);
//
//    }
//
//    @Test
//    public void ziliaoTest(){
//        final QuestionStrategy questionStrategy = questionStrategyDubboService.randomStrategy(uid, subject, 780, 13);
//        final List<Integer> questions = questionStrategy.getQuestions();
//        System.out.println(questions);
//        for (int index = 0; index < questions.size(); ) {
//            final Question question = questionDubboService.findById(questions.get(index));
//            GenericQuestion genericQuestion = (GenericQuestion)question;
//            if (genericQuestion.getParent() > 0) {//大于0则是资料分析
//                final CompositeQuestion parent = (CompositeQuestion)questionDubboService.findById(genericQuestion.getParent());
//                int count = Math.min(index+parent.getQuestions().size(),questions.size());
//                System.out.println("--->index="+index);
//                for (int i = index,j=0; i <count ; i++,index ++,j++) {
//                    Assert.assertEquals(questions.get(i),parent.getQuestions().get(j));
//                }
//            }else {
//                index++;
//            }
//        }
//    }
}
