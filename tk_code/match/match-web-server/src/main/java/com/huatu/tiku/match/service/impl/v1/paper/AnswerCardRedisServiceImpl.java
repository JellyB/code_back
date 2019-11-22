package com.huatu.tiku.match.service.impl.v1.paper;

import com.google.common.collect.Lists;
import com.huatu.tiku.match.dao.document.AnswerCardDao;
import com.huatu.tiku.match.service.v1.paper.AnswerCardDBService;
import com.huatu.ztk.paper.bean.AnswerCard;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/1/15
 */
@Service
public class AnswerCardRedisServiceImpl implements AnswerCardDBService {

    /**
     * 默认超时时间 3 小时 - 此时间理论需要大于模考时长
     */
    private final static int DEFAULT_TIMEOUT = 3;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, AnswerCard> valueOperations;

    @Override
    public AnswerCard findById(Long practiceId) {
        final String key = buildKey(practiceId);
        try {
            AnswerCard answerCard = valueOperations.get(key);
            if (null != answerCard) {
                answerCard.setAnswers(Arrays.stream(answerCard.getAnswers()).map(i-> StringUtils.isBlank(i)?"0":i).toArray(String[]::new));
                return answerCard;
            }
            AnswerCard answerCardDaoById = answerCardDao.findById(practiceId);
            valueOperations.set(key, answerCardDaoById, DEFAULT_TIMEOUT, TimeUnit.HOURS);
            return valueOperations.get(key);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AnswerCard> findById(final List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Lists.newArrayList();
        }
        List<AnswerCard> cacheAnswerCardList = idList.stream()
                .map(answerCardId -> valueOperations.get(buildKey(answerCardId)))
                .filter(answerCard -> null != answerCard)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cacheAnswerCardList) || cacheAnswerCardList.size() < idList.size()) {
            final Set<Long> cacheIdSet = cacheAnswerCardList.stream()
                    .map(AnswerCard::getId)
                    .collect(Collectors.toSet());
            final List<Long> notExistCacheIdList = idList.stream()
                    .filter(id -> !cacheIdSet.contains(id))
                    .collect(Collectors.toList());
            final List<AnswerCard> dbAnswerCardList = answerCardDao.findById(notExistCacheIdList);
            dbAnswerCardList.forEach(answerCard -> {
                final String key = buildKey(answerCard.getId());
                valueOperations.set(key, answerCard, DEFAULT_TIMEOUT, TimeUnit.HOURS);
            });
            cacheAnswerCardList.addAll(dbAnswerCardList);
        }
        cacheAnswerCardList.sort(Comparator.comparing(answerCard -> idList.indexOf(answerCard.getId())));
        return cacheAnswerCardList;

    }


    @Override
    public void save(AnswerCard answerCard) {
        if (null != answerCard) {
            String key = buildKey(answerCard.getId());
            valueOperations.set(key, answerCard, DEFAULT_TIMEOUT, TimeUnit.HOURS);
        }
    }

    @Override
    public void saveToDB(AnswerCard answerCard) {
        if (null != answerCard) {
            String key = buildKey(answerCard.getId());
            valueOperations.set(key, answerCard, DEFAULT_TIMEOUT, TimeUnit.HOURS);
            answerCardDao.save(answerCard);
        }
    }

    /**
     * 答题卡ID
     * 格式化之后的 key为 match-web-server.key
     */
    private static String buildKey(Long practiceId) {
        StringBuilder key = new StringBuilder();
        return key.append("answerCard:id:").append(practiceId).toString();
    }
}
