package com.huatu.tiku.teacher.service.impl.activity;

import com.huatu.tiku.teacher.service.activity.PaperAnswerCardService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/6
 * @描述
 */

@Slf4j
@Service
public class PaperAnswerCardServiceImpl implements PaperAnswerCardService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * 根据试卷ID获取参加考试的所有答题卡信息
     * @param paperId
     * @return
     */
    public List<AnswerCard> getUserAnswerCardByPaperId(Long paperId) {
        String paperPracticeIdSoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId.intValue());
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Set<byte[]> practiceIds = connection.zRange(paperPracticeIdSoreKey.getBytes(), 0, -1);
        if (CollectionUtils.isNotEmpty(practiceIds)) {
            //获取学员答题卡ID
            List<Long> paperPracticeIds = practiceIds.stream().map(practiceId -> {
                String id = new String(practiceId);
                return Long.valueOf(id);
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(paperPracticeIds)) {
                List<AnswerCard> answerCards = getAnswerCardByPracticeId(paperPracticeIds);
                return answerCards;
            }
        }
        return null;
    }

    /**
     * 通过答题卡ID批量获取答题卡全部信息
     *
     * @param paperPracticeIds
     * @return
     */
    public List<AnswerCard> getAnswerCardByPracticeId(List<Long> paperPracticeIds) {
        List<AnswerCard> answerCards = new ArrayList<>();
        //批量获取答题卡信息
        if (CollectionUtils.isNotEmpty(paperPracticeIds)) {
            Criteria criteria = Criteria.where("id").in(paperPracticeIds);
            Query query = new Query(criteria);
            answerCards = mongoTemplate.find(query, AnswerCard.class);
        }
        return answerCards;
    }


}