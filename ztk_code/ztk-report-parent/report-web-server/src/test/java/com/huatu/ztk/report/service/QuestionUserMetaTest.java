package com.huatu.ztk.report.service;

import com.google.common.collect.Maps;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.report.BaseTest;
import com.huatu.ztk.report.dao.AnswerCardDao;
import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class QuestionUserMetaTest extends BaseTest {

    @Autowired
    private QuestionUserMetaService questionUserMetaService;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String SYNC_USER_META_CURSOR = "syncUserMetaCursor";

    @Test
    public void test(){
        Object o = redisTemplate.opsForValue().get(SYNC_USER_META_CURSOR);
        long i = 0;
        try {
            if(null != o){
               i = Long.parseLong(o.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("cone:"+o);
        answerCardDao.findAndHandlerAnswerCard(handlerAnswerCard,i);
    }

    Consumer<List<AnswerCard>> handlerAnswerCard = (answerCards -> {
        long maxId = 0;
        long l = System.currentTimeMillis();
        for (AnswerCard answerCard : answerCards) {
            maxId = Math.max(answerCard.getId(),maxId);
            Map map = Maps.newHashMap();
            map.put("id",answerCard.getId());
            rabbitTemplate.convertAndSend("","init_question_user_meta",map);
        }
        System.out.println(maxId+"|"+answerCards.size()+"|"+(System.currentTimeMillis()-l));
//        redisTemplate.opsForValue().set(SYNC_USER_META_CURSOR,maxId+"");
    });
}
