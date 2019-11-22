package com.huatu.ztk.backend.metas.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.dao.AnswerCardDao;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.RedisKeyConstant;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/11/18.
 */
@Service
public class MatchCacheService {

    @Autowired
    MatchDao matchDao;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    AnswerCardDao answerCardDao;

    /**
     * 回复报名数据
     *
     * @param paperId
     */
    public void replyMatchEnroll(int paperId) {
        Match match = matchDao.findById(paperId);
        List<MatchUserMeta> userMetas = matchDao.findAllMatchUserMeta("_" + paperId);
        if (CollectionUtils.isEmpty(userMetas)) {
            return;
        }
//        List<Long> practiceIds = userMetas.stream().map(MatchUserMeta::getPracticeId).filter(i -> i > 0).collect(Collectors.toList());
//        List<AnswerCard> answerCards = findAnswerCard(practiceIds);
//        Map<Long, Double> cardMap = answerCards.stream().collect(Collectors.toMap(i -> i.getId(), i -> i.getScore()));
        String totalEnrollCountKey = MatchRedisKeys.getTotalEnrollCountKey(paperId);
        redisTemplate.opsForValue().setIfAbsent(totalEnrollCountKey, userMetas.size() + "");
        System.out.println("总报名数据 = " + userMetas.size());

        Map<Integer, List<MatchUserMeta>> collect = userMetas.stream().collect(Collectors.groupingBy(MatchUserMeta::getPositionId));
        for (Map.Entry<Integer, List<MatchUserMeta>> entry : collect.entrySet()) {
            Integer position = entry.getKey();
            List<MatchUserMeta> value = entry.getValue();
            String positionEnrollSetKey = MatchRedisKeys.getPositionEnrollSetKey(paperId, position);
            redisTemplate.opsForSet().add(positionEnrollSetKey,
                    value.stream()
                            .map(MatchUserMeta::getUserId)
                            .map(String::valueOf)
                            .collect(Collectors.toList()));
//            replyPositionMatch(paperId, position, value, cardMap);
            System.out.println("行测报名地区：paperId = " + paperId + "position = " + position);
            if (match.getEssayPaperId() > 0) {
                long essayPaperId = match.getEssayPaperId();
                System.out.println("申论报名地区：essayPaperId = " + essayPaperId + "position = " + position);
                SetOperations opsForSet = redisTemplate.opsForSet();
                String positionEssayEnrollSetKey = RedisKeyConstant.getPositionEnrollSetKey(essayPaperId, position);
                opsForSet.add(positionEssayEnrollSetKey,
                        value.stream()
                                .map(MatchUserMeta::getUserId)
                                .map(String::valueOf)
                                .collect(Collectors.toList()));
            }
        }
//        if (match.getEssayPaperId() > 0) {
//            long essayPaperId = match.getEssayPaperId();
//            HashOperations hashOperations = redisTemplate.opsForHash();
//            String countEnrollKey = RedisKeyConstant.getTotalEnrollCountKey(essayPaperId);
//            System.out.println("申论报名总数countEnrollKey = " + countEnrollKey);
//            redisTemplate.opsForValue().set(countEnrollKey, userMetas.size() + "");
//            for (MatchUserMeta userMeta : userMetas) {
//                String essayPaperKey = RedisKeyConstant.getMockUserAreaPrefix(essayPaperId);
//                hashOperations.put(essayPaperKey, userMeta.getUserId() + "", userMeta.getPositionId() + "");
//                System.out.println("申论报名地区》》》essayPaperKey = " + essayPaperKey);
//                String userPracticeScoreKey = RedisKeyConstant.getUserPracticeScoreKey(essayPaperId);
//                Double score = cardMap.get(userMeta.getPracticeId());
//                if (null != score && userMeta.getPracticeId() > 0) {
//                    redisTemplate.opsForZSet().add(userPracticeScoreKey, userMeta.getUserId(), cardMap.get(userMeta.getPracticeId()));
//                }
//            }
//
//        }
//        Long schoolId = userMetas.get(0).getSchoolId();
//        if (null != schoolId && schoolId > 0) {
//            Map<Long, List<MatchUserMeta>> schoolMap = userMetas.stream().collect(Collectors.groupingBy(MatchUserMeta::getSchoolId));
//            for (Map.Entry<Long, List<MatchUserMeta>> entry : schoolMap.entrySet()) {
//                Long school = entry.getKey();
//                List<MatchUserMeta> value = entry.getValue();
//                String schoolEnrollSetKey = MatchRedisKeys.getSchoolEnrollSetKey(paperId, school);
//                redisTemplate.opsForSet().add(schoolEnrollSetKey,
//                        value.stream()
//                                .map(MatchUserMeta::getSchoolId)
//                                .map(String::valueOf)
//                                .collect(Collectors.toList()));
//                replySchoolMatch(paperId, school, value, cardMap);
//                System.out.println("行测报名学校paperId = " + paperId + "schoolId = " + schoolId);
//            }
//        }
//        replySubmitMatch(paperId, cardMap);
    }

    private void replySchoolMatch(int paperId, Long school, List<MatchUserMeta> values, Map<Long, Double> cardMap) {
        String positionPracticeIdSore = MatchRedisKeys.getSchoolPracticeIdSore(paperId, school);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        for (MatchUserMeta value : values) {
            long practiceId = value.getPracticeId();
            zSetOperations.add(positionPracticeIdSore, String.valueOf(practiceId), cardMap.get(practiceId));
        }
    }

    private void replyPositionMatch(int paperId, Integer position, List<MatchUserMeta> values, Map<Long, Double> cardMap) {
        String positionPracticeIdSore = MatchRedisKeys.getPositionPracticeIdSore(paperId, position);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        for (MatchUserMeta value : values) {
            long practiceId = value.getPracticeId();
            zSetOperations.add(positionPracticeIdSore, String.valueOf(practiceId), cardMap.get(practiceId));
        }
    }

    private List<AnswerCard> findAnswerCard(List<Long> practiceIds) {
        int size = 100;
        int index = 0;
        ArrayList<AnswerCard> answerCardList = Lists.newArrayList();
        while (true) {
            if (index == practiceIds.size()) {
                break;
            }
            int end = index + size > practiceIds.size() ? practiceIds.size() : index + size;
            List<Long> ids = practiceIds.subList(index, end);
            List<AnswerCard> answerCards = answerCardDao.findByIds(ids);
            answerCardList.addAll(answerCards);
            index += end;
        }
        return answerCardList;
    }

    public void replySubmitMatch(int paperId, Map<Long, Double> cardMap) {
        SetOperations setOperations = redisTemplate.opsForSet();
        String matchSubmitPracticeIdSetKey = MatchRedisKeys.getMatchSubmitPracticeIdSetKey(paperId);
        int size = 100;
        int index = 0;
        List<Long> practiceIds = cardMap.entrySet().stream().map(i -> i.getKey()).collect(Collectors.toList());
        while (true) {
            if (index >= practiceIds.size()) {
                break;
            }
            int end = index + size > practiceIds.size() ? practiceIds.size() : index + size;
            setOperations.add(matchSubmitPracticeIdSetKey, practiceIds.subList(index, end).stream().map(String::valueOf).collect(Collectors.toList()));
            index = end;
        }
        for (Long practiceId : practiceIds) {
            String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
            redisTemplate.opsForZSet().add(paperPracticeIdSore, practiceId + "", cardMap.get(practiceId));
            String paperScoreSum = PaperRedisKeys.getPaperScoreSum(paperId);
            redisTemplate.opsForValue().increment(paperScoreSum, cardMap.get(practiceId));
        }
    }
}
