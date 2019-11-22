//package com.huatu.ztk.knowledge.task;
//
//import com.google.common.primitives.Longs;
//import com.huatu.ztk.commons.JsonUtil;
//import com.huatu.ztk.knowledge.BaseTest;
//import com.huatu.ztk.knowledge.api.PointSummaryDubboService;
//import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
//import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
//import com.huatu.ztk.knowledge.bean.QuestionPoint;
//import com.huatu.ztk.knowledge.bean.QuestionStrategy;
//import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
//import com.huatu.ztk.paper.bean.Answer;
//import com.huatu.ztk.paper.bean.UserAnswers;
//import com.huatu.ztk.question.api.QuestionDubboService;
//import com.huatu.ztk.question.bean.GenericQuestion;
//import com.huatu.ztk.question.common.QuestionCorrectType;
//import org.apache.commons.lang3.RandomUtils;
//import org.junit.Assert;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.Message;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ZSetOperations;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Created by shaojieyue
// * Created time 2016-06-13 15:16
// */
//public class UserAnswersTaskTest extends BaseTest {
//    private static final Logger logger = LoggerFactory.getLogger(UserAnswersTaskTest.class);
//
//    @Autowired
//    private UserAnswersTask userAnswersTask;
//
//    @Autowired
//    private QuestionStrategyDubboService questionStrategyDubboService;
//
//    @Autowired
//    private PointSummaryDubboService pointSummaryDubboService;
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Autowired
//    private QuestionPointDubboService questionPointDubboService;
//
//    @Autowired
//    private SsdbClient ssdbClient;
//
//    @Autowired
//    private QuestionDubboService questionDubboService;
//
//    @Test
//    public void aa(){
//        ssdbClient.zset("finish_1_769",12+"",12);
//    }
//
//    @Test
//    public void onMessageTest(){
//        final int oneLevelPointId = 435;
//        final int twoLevelpointId = 436;
//        final int pointId = 443;
//        final int uid = 12252066;
//        final int subject = 1;
//        final QuestionStrategy questionStrategy = questionStrategyDubboService.randomStrategy(pointId, 3);
//        List<Answer> answerList = new ArrayList<>();
//        Random random = new Random();
//        Set<Integer> errors = new HashSet<Integer>();
//        Set<Integer> rights = new HashSet<Integer>();
//        int allTimes = 0;
//        int errorCount = 0;
//        int rightCount = 0;
//        for (int i = 0; i < questionStrategy.getQuestions().size(); i++) {
//            Answer answer = new Answer();
//            answer.setTime(RandomUtils.nextInt(10,100));
//            answer.setAnswer(12);
//            int correct = random.nextBoolean()? QuestionCorrectType.RIGHT:QuestionCorrectType.WRONG;
//            answer.setCorrect(correct);
//            answer.setQuestionId(questionStrategy.getQuestions().get(i));
//            answerList.add(answer);
//            if (QuestionCorrectType.RIGHT != answer.getCorrect()) {
//                errors.add(questionStrategy.getQuestions().get(i));
//                errorCount ++;
//            }else {
//                rightCount ++;
//                rights.add(questionStrategy.getQuestions().get(i));
//            }
//
//            allTimes = allTimes + answer.getTime();
//        }
//
//
//        final UserAnswers userAnswers = UserAnswers.builder()
//                .subject(subject)
//                .uid(uid)
//                .submitTime(System.currentTimeMillis())
//                .answers(answerList)
//                .build();
//        final String json = JsonUtil.toJson(userAnswers);
//
//        String ss = "{\"uid\":13041527,\"subject\":1,\"area\":21,\"submitTime\":1469674531522,\"answers\":[{\"questionId\":89861,\"answer\":3,\"time\":87,\"correct\":2},{\"questionId\":89853,\"answer\":3,\"time\":2,\"correct\":1},{\"questionId\":89659,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89716,\"answer\":1,\"time\":1,\"correct\":2},{\"questionId\":89736,\"answer\":2,\"time\":1,\"correct\":1},{\"questionId\":89881,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89693,\"answer\":2,\"time\":1,\"correct\":1},{\"questionId\":89776,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89799,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89683,\"answer\":2,\"time\":1,\"correct\":1},{\"questionId\":89733,\"answer\":1,\"time\":4,\"correct\":2},{\"questionId\":89816,\"answer\":1,\"time\":5,\"correct\":2},{\"questionId\":89737,\"answer\":1,\"time\":1,\"correct\":2},{\"questionId\":89801,\"answer\":1,\"time\":2,\"correct\":2},{\"questionId\":89673,\"answer\":2,\"time\":2,\"correct\":2},{\"questionId\":89800,\"answer\":2,\"time\":59,\"correct\":2},{\"questionId\":90451,\"answer\":2,\"time\":8,\"correct\":2},{\"questionId\":89650,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89806,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89684,\"answer\":1,\"time\":2,\"correct\":2},{\"questionId\":89705,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89700,\"answer\":2,\"time\":3,\"correct\":1},{\"questionId\":89744,\"answer\":2,\"time\":9,\"correct\":2},{\"questionId\":89851,\"answer\":2,\"time\":2,\"correct\":1},{\"questionId\":89745,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89710,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89860,\"answer\":3,\"time\":21,\"correct\":2},{\"questionId\":89666,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89749,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89885,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89898,\"answer\":1,\"time\":11,\"correct\":2},{\"questionId\":89687,\"answer\":2,\"time\":1,\"correct\":1},{\"questionId\":89769,\"answer\":2,\"time\":2,\"correct\":2},{\"questionId\":89656,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89778,\"answer\":4,\"time\":1,\"correct\":2},{\"questionId\":89782,\"answer\":3,\"time\":59,\"correct\":1},{\"questionId\":89715,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89755,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89899,\"answer\":3,\"time\":10,\"correct\":2},{\"questionId\":89857,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89751,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89649,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89804,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89813,\"answer\":1,\"time\":2,\"correct\":2},{\"questionId\":89775,\"answer\":2,\"time\":2,\"correct\":2},{\"questionId\":89689,\"answer\":2,\"time\":2,\"correct\":2},{\"questionId\":89727,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89698,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89658,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89681,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89832,\"answer\":1,\"time\":2,\"correct\":2},{\"questionId\":89655,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89730,\"answer\":3,\"time\":10,\"correct\":2},{\"questionId\":90448,\"answer\":3,\"time\":9,\"correct\":1},{\"questionId\":89844,\"answer\":2,\"time\":3,\"correct\":2},{\"questionId\":89679,\"answer\":1,\"time\":1,\"correct\":2},{\"questionId\":89836,\"answer\":2,\"time\":2,\"correct\":1},{\"questionId\":89757,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89859,\"answer\":4,\"time\":21,\"correct\":2},{\"questionId\":89665,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89906,\"answer\":1,\"time\":10,\"correct\":2},{\"questionId\":89880,\"answer\":1,\"time\":28,\"correct\":2},{\"questionId\":89843,\"answer\":2,\"time\":2,\"correct\":2},{\"questionId\":89904,\"answer\":1,\"time\":22,\"correct\":1},{\"questionId\":89712,\"answer\":1,\"time\":1,\"correct\":1},{\"questionId\":89704,\"answer\":1,\"time\":1,\"correct\":2},{\"questionId\":89667,\"answer\":1,\"time\":4,\"correct\":2},{\"questionId\":89874,\"answer\":3,\"time\":3,\"correct\":2},{\"questionId\":89879,\"answer\":1,\"time\":28,\"correct\":2},{\"questionId\":89699,\"answer\":1,\"time\":1,\"correct\":2},{\"questionId\":89722,\"answer\":2,\"time\":4,\"correct\":2},{\"questionId\":89723,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89661,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89773,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89771,\"answer\":1,\"time\":2,\"correct\":2},{\"questionId\":89805,\"answer\":1,\"time\":2,\"correct\":1},{\"questionId\":89814,\"answer\":2,\"time\":2,\"correct\":2},{\"questionId\":89875,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89815,\"answer\":1,\"time\":1,\"correct\":1},{\"questionId\":89765,\"answer\":4,\"time\":2,\"correct\":2},{\"questionId\":89862,\"answer\":2,\"time\":73,\"correct\":2},{\"questionId\":89652,\"answer\":4,\"time\":1,\"correct\":2},{\"questionId\":89676,\"answer\":4,\"time\":3,\"correct\":2},{\"questionId\":90447,\"answer\":2,\"time\":10,\"correct\":2},{\"questionId\":89820,\"answer\":2,\"time\":3,\"correct\":2},{\"questionId\":89827,\"answer\":2,\"time\":3,\"correct\":2},{\"questionId\":89809,\"answer\":2,\"time\":2,\"correct\":2},{\"questionId\":89766,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89907,\"answer\":4,\"time\":11,\"correct\":2},{\"questionId\":89719,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89660,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89903,\"answer\":3,\"time\":20,\"correct\":2},{\"questionId\":89825,\"answer\":1,\"time\":3,\"correct\":2},{\"questionId\":89671,\"answer\":3,\"time\":5,\"correct\":2},{\"questionId\":89728,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89654,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89877,\"answer\":2,\"time\":5,\"correct\":2},{\"questionId\":89708,\"answer\":1,\"time\":2,\"correct\":2},{\"questionId\":90450,\"answer\":2,\"time\":11,\"correct\":1},{\"questionId\":89724,\"answer\":3,\"time\":3,\"correct\":2},{\"questionId\":89905,\"answer\":1,\"time\":12,\"correct\":2},{\"questionId\":89878,\"answer\":1,\"time\":1,\"correct\":2},{\"questionId\":89781,\"answer\":2,\"time\":1,\"correct\":1},{\"questionId\":89653,\"answer\":4,\"time\":1,\"correct\":1},{\"questionId\":89732,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89882,\"answer\":2,\"time\":2,\"correct\":1},{\"questionId\":89758,\"answer\":3,\"time\":2,\"correct\":2},{\"questionId\":89697,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89657,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89803,\"answer\":2,\"time\":1,\"correct\":2},{\"questionId\":89669,\"answer\":3,\"time\":1,\"correct\":1},{\"questionId\":89651,\"answer\":2,\"time\":2,\"correct\":2},{\"questionId\":89739,\"answer\":1,\"time\":1,\"correct\":2},{\"questionId\":90449,\"answer\":3,\"time\":10,\"correct\":2},{\"questionId\":89858,\"answer\":4,\"time\":45,\"correct\":2},{\"questionId\":89675,\"answer\":3,\"time\":1,\"correct\":2},{\"questionId\":89753,\"answer\":2,\"time\":10,\"correct\":1}]}";
//        Message message = new Message(ss.getBytes(),null);
//        userAnswersTask.onMessage(message);
//        try {
//            //异步处理,保证消息处理完成
//            Thread.sleep(50000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(errors);
//        System.out.println(rights);
//        for (Object error : errors) {
//            final GenericQuestion question = (GenericQuestion)questionDubboService.findById(Integer.valueOf(error.toString()));
//            final String finishedSetKey = RedisKnowledgeKeys.getFinishedSetKey(uid, subject, question.getPoints().get(2));
//
//            final String oneLevelWrongSetKey = RedisKnowledgeKeys.getWrongSetKey(uid, subject, question.getPoints().get(0));
//            final String twoLevelWrongSetKey = RedisKnowledgeKeys.getWrongSetKey(uid, subject, question.getPoints().get(1));
//            final String wrongSetKey = RedisKnowledgeKeys.getWrongSetKey(uid, subject, question.getPoints().get(2));
//            Double score = redisTemplate.opsForZSet().score(oneLevelWrongSetKey, error.toString());
//            Assert.assertTrue(score>0);
//
//            score = redisTemplate.opsForZSet().score(twoLevelWrongSetKey, error.toString());
//            Assert.assertTrue(score>0);
//
//            score = redisTemplate.opsForZSet().score(wrongSetKey, error.toString());
//            Assert.assertTrue(score>0);
//
//            Assert.assertTrue(ssdbClient.zget(finishedSetKey,error.toString())>0);
//        }
//
//
//        for (Object right : rights) {
//            final GenericQuestion question = (GenericQuestion)questionDubboService.findById(Integer.valueOf(right.toString()));
//            Assert.assertTrue(ssdbClient.zget(RedisKnowledgeKeys.getFinishedSetKey(uid,subject,question.getPoints().get(2)),right.toString())>0);
//        }
//
//        final String oneFinishedCountKey = RedisKnowledgeKeys.getFinishedCountKey(uid, subject);
//        List<QuestionPoint> children = questionPointDubboService.findChildren(oneLevelPointId);
//        List<String> childPoints = children.stream().map(child -> child.getId()+"").collect(Collectors.toList());
//        final String oneLevelCount = ssdbClient.hget(oneFinishedCountKey, oneLevelPointId + "");
//        int sum = ssdbClient.multiHget(oneFinishedCountKey, childPoints)
//                .stream()
//                .filter(count -> count != null)
//                .map(count -> count.getValue())
//                .mapToInt(Integer::valueOf).sum();
//        Assert.assertEquals(Integer.valueOf(oneLevelCount).intValue(),sum);
//
//        for (String childPoint : childPoints) {
//            children = questionPointDubboService.findChildren(oneLevelPointId);
//            childPoints = children.stream().map(child -> child.getId()+"").collect(Collectors.toList());
//            sum = ssdbClient.multiHget(oneFinishedCountKey, childPoints)
//                    .stream()
//                    .filter(count -> count != null)
//                    .map(count -> count.getValue())
//                    .mapToInt(Integer::valueOf).sum();
//            Assert.assertEquals(Integer.valueOf(oneLevelCount).intValue(),sum);
//        }
//
//
//        final Set<Integer> userPoints = questionPointDubboService.findUserPoints(uid, subject);
//        Assert.assertTrue(userPoints.contains(pointId));
//        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
//        final HashOperations hashOperations = redisTemplate.opsForHash();
//        final String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(uid, subject);
//        System.out.println("----------"+zSetOperations.size(RedisKnowledgeKeys.getWrongSetKey(uid,subject,pointId)));
//        Assert.assertEquals(zSetOperations.size(RedisKnowledgeKeys.getWrongSetKey(uid,subject,oneLevelPointId)), Longs.tryParse(hashOperations.get(wrongCountKey,oneLevelPointId+"").toString()));
//        Assert.assertEquals(zSetOperations.size(RedisKnowledgeKeys.getWrongSetKey(uid,subject,twoLevelpointId)),Longs.tryParse(hashOperations.get(wrongCountKey,twoLevelpointId+"").toString()));
//        Assert.assertEquals(zSetOperations.size(RedisKnowledgeKeys.getWrongSetKey(uid,subject,pointId)),Longs.tryParse(hashOperations.get(wrongCountKey,pointId+"").toString()));
//
//    }
//}
