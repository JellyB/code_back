package com.huatu.ztk.knowledge.task;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeysV2;
import com.huatu.ztk.knowledge.dao.AnswerCardDao;
import com.huatu.ztk.knowledge.service.v1.QuestionErrorServiceV1;
import com.huatu.ztk.knowledge.service.v1.QuestionFinishServiceV1;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserQuestionPointTask implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(UserQuestionPointTask.class);
    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    @Qualifier("questionErrorServiceImplV2")
    private QuestionErrorServiceV1 questionErrorServiceV2;

    @Autowired
    @Qualifier("questionFinishServiceImplV2")
    private QuestionFinishServiceV1 questionFinishServiceV2;

    @Override
    public void onMessage(Message message) {
        String content = new String(message.getBody());
        logger.info("receive message={}", content);
        Map data = new HashMap();
        try {
            data = JsonUtil.toMap(content);

            Long id = null;
            if (data.containsKey("id")) {
                id = Longs.tryParse(data.get("id").toString());
            }
            if (id == null) {
                logger.error("message not contain key id,skip it. data={}", content);
                return;
            }

            AnswerCard answerCard = answerCardDao.findById(id);
            if (answerCard == null || answerCard.getStatus() != AnswerCardStatus.FINISH) {
                return;
            }

            int[] corrects = answerCard.getCorrects();
            if (null == corrects || corrects.length == 0) {
                return;
            }

            Function<AnswerCard, List<Integer>> getQuestionIds = (card -> {
                try {
                    if (card instanceof StandardCard) {
                        return ((StandardCard) card).getPaper().getQuestions();
                    } else if (card instanceof PracticeCard) {
                        return ((PracticeCard) card).getPaper().getQuestions();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Lists.newArrayList();
            });
            List<Integer> questionIds = getQuestionIds.apply(answerCard);
            if (CollectionUtils.isEmpty(questionIds)) {
                return;
            }
            long userId = answerCard.getUserId();
            Map<Integer, Integer> questionCorrect = IntStream.range(0, corrects.length).boxed().filter(i -> corrects[i] != 0)
                    .collect(Collectors.toMap(i -> questionIds.get(i), i -> corrects[i]));
            for (Map.Entry<Integer, Integer> entry : questionCorrect.entrySet()) {
                Question question = questionDubboService.findById(entry.getKey());
                if (question instanceof GenericQuestion) {
                    updateUserQuestionCorrect(userId, (GenericQuestion) question, entry.getValue());
                    updateUserQuestionFinish(userId, (GenericQuestion) question);
                }
            }
        } catch (Exception e) {
            logger.error("ex", e);
        }
    }

    private void updateUserQuestionFinish(long userId, GenericQuestion question) {
//        String finishCountKey = RedisKnowledgeKeysV2.getFinishCountKey(userId);
//        List<Integer> points = question.getPoints();
////        questionFinishServiceV2.countByPoints(points, userId);
////        points.stream().forEach(i -> questionFinishServiceV2.getQuestionIds(userId, i));
//        if(!QuestionPointPoolUtil.isPoolFlag(question)){
//            return;
//        }
//        //是否是单题批改类型
//        for (Integer point : points) {
//            String finishedSetKey = RedisKnowledgeKeysV2.getFinishedSetKey(userId, point);
//////            System.out.println("finishedSetKey = " + finishedSetKey);
////            redisTemplate.opsForSet().add(finishedSetKey, question.getId() + "");
////            Long size = redisTemplate.opsForSet().size(finishedSetKey);
////            redisTemplate.opsForHash().put(finishCountKey, point + "", Math.max(size-1,0) + "");
//            Long expire = redisTemplate.getExpire(finishedSetKey);
//            if(expire.intValue() != -2){        //存在缓存，则修改缓存数据
//                expire.
//            }
//        }
    }

    public void updateUserQuestionCorrect(long userId, GenericQuestion question, int correct) {
//        String wrongCountKey = RedisKnowledgeKeysV2.getWrongCountKey(userId);
////        System.out.println("wrongCountKey = " + wrongCountKey);
//        List<Integer> points = question.getPoints();
//        //确保redis数据存在
//        points.stream().forEach(i -> questionErrorServiceV2.getQuestionIds(i, userId));
//        questionErrorServiceV2.countAll(userId);
//        for (Integer point : points) {
//            String wrongSetKey = RedisKnowledgeKeysV2.getWrongSetKey(userId, point);
//            String wrongCursor = RedisKnowledgeKeysV2.getWrongCursor(userId, point);
//            long currentTimeMillis = System.currentTimeMillis();
//            if (correct != 1) {
//                redisTemplate.opsForZSet().add(wrongSetKey, question.getId() + "", currentTimeMillis);
//                redisTemplate.opsForZSet().add(wrongCursor, question.getId() + "", currentTimeMillis);
//            } else {
//                redisTemplate.opsForZSet().remove(wrongSetKey, question.getId() + "");
//                redisTemplate.opsForZSet().remove(wrongCursor, question.getId() + "");
//            }
////            System.out.println("wrongSetKey = " + wrongSetKey + "|" + (correct==1));
////            System.out.println("wrongCursor = " + wrongCursor + "|" + (correct == 1 ));
//            Long size = redisTemplate.opsForZSet().size(wrongSetKey);
//            redisTemplate.opsForHash().put(wrongCountKey, point + "", size + "");
//        }
    }
}
