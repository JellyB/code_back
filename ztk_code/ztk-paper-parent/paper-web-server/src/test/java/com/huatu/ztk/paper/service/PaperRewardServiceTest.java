package com.huatu.ztk.paper.service;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.AnswerCardType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

/**
 * Created by linkang on 2017/10/13 上午10:19
 */
public class PaperRewardServiceTest extends BaseTest{
    @Autowired
    private PaperRewardService paperRewardService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void sendSubmitPracticeMsg() throws Exception {
        AnswerCard answerCard = new StandardCard();
        answerCard.setId(45464);
        answerCard.setSubject(1);
        answerCard.setType(AnswerCardType.WRONG_PAPER);

        paperRewardService.sendSubmitPracticeMsg(1L, "hehe", answerCard);
    }

    @Test
    public void sendEnrollMsg() throws Exception {

        paperRewardService.sendEnrollMsg(1L, "hehe", 111);
    }

    @Test
    public void clearRedisKeys() {
        Set<String> keys = redisTemplate.keys("reward_*");
        redisTemplate.delete(keys);

    }
}