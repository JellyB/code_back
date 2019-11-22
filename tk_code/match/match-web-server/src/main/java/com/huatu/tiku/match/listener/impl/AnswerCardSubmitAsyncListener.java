package com.huatu.tiku.match.listener.impl;

import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.dto.paper.AnswerDTO;
import com.huatu.tiku.match.listener.ListenerService;
import com.huatu.tiku.match.service.v1.paper.AnswerCardService;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author huangqingpeng 2019/05/31
 */
@Slf4j
@Component
public class AnswerCardSubmitAsyncListener implements ListenerService {

    @Autowired
    AnswerCardService answerCardService;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void onMessage(Map message) {
        String answerCardKey = MatchInfoRedisKeys.getMatchSubmitAnswerCardIdSetKey();
        Long practiceId = -1L;
        try {
            log.info("message start={}", message);
            Integer userId = MapUtils.getInteger(message, "userId");
            practiceId = MapUtils.getLong(message, "practiceId", -1L);
            String answers = MapUtils.getString(message, "answerList");
            List<AnswerDTO> answerList = JsonUtil.toList(answers, AnswerDTO.class);
            answerCardService.submit(userId, practiceId, answerList);
        } catch (Exception e) {
            log.error("MatchUserMetaSyncListener handler message error,message={}", message);
            e.printStackTrace();
        } finally {
            redisTemplate.opsForSet().remove(answerCardKey, practiceId.toString());
        }
    }
}
