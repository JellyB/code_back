//package com.huatu.ztk.report.bean;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//import com.huatu.ztk.knowledge.bean.PointSummary;
//import com.huatu.ztk.paper.bean.Answer;
//import com.huatu.ztk.paper.bean.UserAnswers;
//import com.huatu.ztk.question.api.QuestionDubboService;
//import com.huatu.ztk.question.bean.GenericQuestion;
//import com.huatu.ztk.question.bean.Question;
//import com.huatu.ztk.question.common.QuestionCorrectType;
//import com.huatu.ztk.report.BaseTest;
//import com.huatu.ztk.report.service.PointSummaryService;
//import com.huatu.ztk.report.task.QuestionAnswerMessageListener;
//import org.junit.AfterClass;
//import org.junit.Assert;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.*;
//
///**
// * Created by shaojieyue
// * Created time 2016-06-16 11:26
// */
//public class QuestionAnswerMessageListenerTest extends BaseTest{
//    private static final Logger logger = LoggerFactory.getLogger(QuestionAnswerMessageListenerTest.class);
//    @Autowired
//    private QuestionAnswerMessageListener questionAnswerMessageListener;
//
//    @Autowired
//    private QuestionDubboService questionDubboService;
//
//    @Autowired
//    private PointSummaryService pointSummaryService;
//
//    @Test
//    public void proccessMessageTest(){
//        Set<Integer> points = Sets.newHashSet();
//        Map<Integer,PointSummary> oldMap = Maps.newHashMap();
//        Map<Integer,Integer> wrongMap = Maps.newHashMap();
//        Map<Integer,Integer> rightMap = Maps.newHashMap();
//        Map<Integer,Integer> timeMap = Maps.newHashMap();
//        Map<Integer,Integer> allMap = Maps.newHashMap();
//        long uid = 12252065;
//        int subject = 1;
//        final List<Answer> answers = Lists.newArrayList();
//        Random random = new Random();
//        for (int i = 0; i < 100; i++) {
//            Answer answer = new Answer();
//            final int quesionId = random.nextInt(30000) + 30878;
//            answer.setQuestionId(quesionId);
//            answer.setTime(random.nextInt(2000));
//            answer.setCorrect(random.nextInt(2)+1);
//            answer.setAnswer(random.nextInt(4)+1);
//            answers.add(answer);
//            final Question question = questionDubboService.findById(quesionId);
//            if (question != null && question instanceof GenericQuestion) {
//                GenericQuestion genericQuestion = (GenericQuestion)question;
//                for (Integer point : genericQuestion.getPoints()) {
//                    final Integer time = timeMap.get(point)==null?0:timeMap.get(point);
//                    final Integer wrong = wrongMap.get(point)==null?0:wrongMap.get(point);
//                    final Integer right = rightMap.get(point)==null?0:rightMap.get(point);
//                    final Integer all = allMap.get(point)==null?0:allMap.get(point);
//                    points.add(point);
//                    if (answer.getCorrect() == QuestionCorrectType.RIGHT) {
//                        rightMap.put(point, right +1);
//                    }else if(answer.getCorrect() == QuestionCorrectType.WRONG){
//                        wrongMap.put(point,wrong+1);
//                    }else {//传入非法的数据,也视为答错
//                        wrongMap.put(point,wrong+1);
//                    }
//                    allMap.put(point,all+1);
//                    timeMap.put(point, time +answer.getTime());
//                }
//            }
//
//        }
//
//        //老数据
//        for (Integer point : points) {
//            oldMap.put(point,pointSummaryService.find(uid,subject,point));
//        }
//        final UserAnswers userAnswers = UserAnswers.builder()
//                .subject(subject)
//                .uid(uid)
//                .answers(answers)
//                .build();
//        questionAnswerMessageListener.proccessMessage(userAnswers);
//
//        Map<Integer,PointSummary> newData = Maps.newHashMap();
//        for (Integer point : points) {
//            newData.put(point,pointSummaryService.find(uid,subject,point));
//        }
//
//        for (Integer point : newData.keySet()) {
//            final PointSummary newpointSummary = newData.get(point);
//            final PointSummary oldpointSummary = oldMap.get(point);
//            Assert.assertEquals(oldpointSummary.getAcount()+allMap.get(point),newpointSummary.getAcount());
//            if (rightMap.containsKey(point)) {
//                Assert.assertEquals(oldpointSummary.getRcount()+rightMap.get(point),newpointSummary.getRcount());
//            }
//            if (wrongMap.containsKey(point)) {
//                Assert.assertEquals(oldpointSummary.getWcount()+wrongMap.get(point),newpointSummary.getWcount());
//            }
//
//            if (timeMap.containsKey(point)) {
//                Assert.assertEquals((oldpointSummary.getTimes()+timeMap.get(point))/(newpointSummary.getAcount()),newpointSummary.getSpeed());
//            }
//        }
//    }
//}
