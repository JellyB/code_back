package com.huatu.ztk.paper.service.v4.impl;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PeriodTestRedisKey;
import com.huatu.ztk.paper.common.SmallRedisKey;
import com.huatu.ztk.paper.dto.ScoreRankDto;
import com.huatu.ztk.paper.enums.ScoreSortEnum;
import com.huatu.ztk.paper.service.v4.HandlerMetaService;
import com.huatu.ztk.paper.service.v4.ScoreSortService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/2/20.
 */
@Service
public class HandlerMetaServiceImpl implements HandlerMetaService {

    private final static Logger logger = LoggerFactory.getLogger(HandlerMetaService.class);
    private final static int DEFAULT_JOIN_COUNT = 0;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ScoreSortService scoreSortService;

    @Override
    public int getJoinCount(int paperId) {
        String createAnswerCardCount = SmallRedisKey.getCreateAnswerCardCount(paperId);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(createAnswerCardCount);
        if (null == value) {
            return DEFAULT_JOIN_COUNT;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    /**
     * 自增1
     *
     * @param paperId
     */
    @Override
    public void incrementJoinCount(int paperId) {
        String createAnswerCardCount = SmallRedisKey.getCreateAnswerCardCount(paperId);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.increment(createAnswerCardCount, 1);
    }

    /**
     * 补充报告数据
     *
     * @param answerCard
     */
    @Override
    public void fillSmallEstimateReportInfo(AnswerCard answerCard) {
        int answerCardType = answerCard.getType();
        if (answerCardType == AnswerCardType.SMALL_ESTIMATE) {
            scoreSortService.reSort(answerCard, ScoreSortEnum.SCORE_SUBMIT_SORT);
        }
        AnswerCardUtil.transSmallEstimateReport((StandardCard) answerCard);
    }

    /**
     * 获取答题卡的统计信息(排名,平均分)
     *
     * @param standardCard
     * @return
     */
    public CardUserMeta getCardUserMeta(final StandardCard standardCard) {
        //修改真题演练,往期模考,专项模考,精准估分
        int type = standardCard.getType();
        switch (type) {
            case AnswerCardType.TRUE_PAPER:
            case AnswerCardType.SIMULATE:
            case AnswerCardType.ESTIMATE:
            case AnswerCardType.MATCH_AFTER:
            case AnswerCardType.APPLETS_PAPER:
                return getCardUserMetaForTruePaper(standardCard);
            case AnswerCardType.SMALL_ESTIMATE:
                CardUserMeta commonMeta = getCardUserMetaForCommon(standardCard);
                scoreSortService.reSort(standardCard, ScoreSortEnum.SCORE_SUBMIT_SORT);
                fillCardUserMeta(commonMeta, standardCard);
                return commonMeta;
            case AnswerCardType.FORMATIVE_TEST_ESTIMATE:
                CardUserMeta cardUserMeta = getCardUserMetaForTruePaper(standardCard);
                //fillCardUserMeta(cardUserMeta, standardCard);
                return cardUserMeta;
            default:
                return getCardUserMetaForCommon(standardCard);
        }

    }

    @Override
    public void handlerSubmitInfo(AnswerCard answerCard) {
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            int paperId = paper.getId();
            int answerCardType = answerCard.getType();
            String paperSubmitTimeKey = PaperRedisKeys.getPaperSubmitTimeKey(paperId, answerCardType);
            final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
            zSetOperations.add(paperSubmitTimeKey, answerCard.getId() + "", System.currentTimeMillis());
        }
    }

    /**
     * 缓存报告一分钟
     *
     * @param answerCard
     */
    @Override
    public void putCache(StandardCard answerCard) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String answerInfoKey = SmallRedisKey.getAnswerInfoKey(answerCard.getId());
        try {
            valueOperations.set(answerInfoKey, JsonUtil.toJson(answerCard), 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询缓存报告
     *
     * @param practiceId
     * @return
     */
    @Override
    public StandardCard getReportCache(Long practiceId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String answerInfoKey = SmallRedisKey.getAnswerInfoKey(practiceId);
        StandardCard standardCard = null;
        try {

            String value = valueOperations.get(answerInfoKey);
            logger.info("getReportCache:{};value={}", practiceId, value);
            if (null != value) {
                standardCard = JsonUtil.toObject(value, StandardCard.class);
                return standardCard;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return standardCard;
    }

    /**
     * 补充用户做对题及交卷行为排序等统计数据
     *
     * @param cardUserMeta
     * @param standardCard
     */
    private void fillCardUserMeta(CardUserMeta cardUserMeta, StandardCard standardCard) {
        Paper paper = standardCard.getPaper();
        int totalScore = paper.getScore();
        if (totalScore == 0) {
            logger.error("试卷分数为空，paper={}", paper.getId());
        }
        int qcount = paper.getQcount();
        double max = cardUserMeta.getMax();
        if (totalScore < max) {           //这种试卷分数是按100总分计算的
            totalScore = 100;
        }
        double average = cardUserMeta.getAverage();
        double qdm = qcount * max / totalScore;  //按比例计算答对题数
        int mq = getInt(qdm);
        double qda = qcount * average / totalScore;  //按比例计算答对题数
        int aq = getInt(qda);
        String paperSubmitTimeKey = PaperRedisKeys.getPaperSubmitTimeKey(paper.getId(), standardCard.getType());
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Long total = ScoreSortUtil.getTotal(zSetOperations, paperSubmitTimeKey);
        Long rank = ScoreSortUtil.getRank(zSetOperations, standardCard.getId() + "", paperSubmitTimeKey, total);
        cardUserMeta.setRNumAverage(aq);
        cardUserMeta.setRNumMax(mq);
        cardUserMeta.setSubmitCount(total.intValue());
        cardUserMeta.setSubmitRank(total.intValue() - rank.intValue() + 1);
        long reportTime = System.currentTimeMillis();
        if (paper instanceof EstimatePaper) {
            long endTime = ((EstimatePaper) paper).getEndTime();
            reportTime = Math.min(endTime, reportTime);
        }
        cardUserMeta.setReportTime(reportTime);
        standardCard.setCardUserMeta(cardUserMeta);
    }

    public static int getInt(double number) {
        try {
            BigDecimal bd = new BigDecimal(number).setScale(0, BigDecimal.ROUND_HALF_UP);
            return Integer.parseInt(bd.toString());
        } catch (Exception e) {
            logger.error("getInt error,number = {}", number);
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 正常运算规则
     *
     * @param standardCard
     * @return
     */
    private CardUserMeta getCardUserMetaForCommon(StandardCard standardCard) {
        final int paperId = standardCard.getPaper().getId();
        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        String paperScoreSum = PaperRedisKeys.getPaperScoreSum(paperId);
        if (standardCard.getType() == AnswerCardType.MATCH_AFTER) {
            paperPracticeIdSore = PaperRedisKeys.getKeyWithType(paperPracticeIdSore, AnswerCardType.MATCH_AFTER);
            paperScoreSum = PaperRedisKeys.getKeyWithType(paperScoreSum, AnswerCardType.MATCH_AFTER);
        }
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Long total = ScoreSortUtil.getTotal(zSetOperations, paperPracticeIdSore);
        Long rank = ScoreSortUtil.getRank(zSetOperations, standardCard.getId() + "", paperPracticeIdSore, total);
        final String scoreStr = valueOperations.get(paperScoreSum);//该试卷总得分
        Double average = 60D;//默认值

        if (scoreStr != null && total > 0) {
            double allScore = Double.parseDouble(scoreStr);//答题卡所有分数和
            average = allScore / total;//平均分
        }
        final int beatRate = (int) ((total - rank) * 100 / total);

        Double max = 0d;
        if (total != 0) {
            Set<ZSetOperations.TypedTuple<String>> withScores =
                    zSetOperations.reverseRangeWithScores(paperPracticeIdSore, 0, 0);

            if (CollectionUtils.isNotEmpty(withScores)) {
                max = new ArrayList<>(withScores).get(0).getScore();
            }
        }
        BigDecimal bigDecimal = new BigDecimal(average);
        double dAverage = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        BigDecimal maxDecimal = new BigDecimal(max);
        double dMax = maxDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        CardUserMeta cardUserMeta = CardUserMeta.builder()
                .average(dAverage)
                .beatRate(beatRate)
                .rank(rank.intValue())
                .total(total.intValue())
                .max(dMax)
                .build();
        AnswerCardUtil.reBuildCardMeta(cardUserMeta, rank, total);
        return cardUserMeta;
    }

    /**
     * 修改真题演练,专项训练平均分（击败比例）计算规则
     */
    public CardUserMeta getCardUserMetaForTruePaper(final StandardCard standardCard) {
        //可配置
        int standScore = 20;       //分数下限
        int scoreUpLine = 200;  //分数上限
        double totalScore = 0D;
        int size = 0;
        Double allScore = 0D;       //累加和
        double max = 0D;

        int paperId = standardCard.getPaper().getId();

        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        if (standardCard.getType() == AnswerCardType.FORMATIVE_TEST_ESTIMATE) {
            paperPracticeIdSore = paperPracticeIdSore + "_" + standardCard.getSyllabusId();
        }
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        //往期模考，需要添加类型
        if (standardCard.getType() == AnswerCardType.MATCH_AFTER) {
            paperPracticeIdSore = PaperRedisKeys.getKeyWithType(paperPracticeIdSore, AnswerCardType.MATCH_AFTER);
        }
        Set<ZSetOperations.TypedTuple<String>> allStudentScoreInfo = zSetOperations.rangeWithScores(paperPracticeIdSore, 0, -1);
        List<Double> allStudentScore = allStudentScoreInfo.stream().map(score -> score.getScore())
                .collect(Collectors.toList());
        Long rank = ScoreSortUtil.getRank(zSetOperations, standardCard.getId() + "", paperPracticeIdSore, Long.valueOf(String.valueOf(size)));
        if (CollectionUtils.isNotEmpty(allStudentScore)) {
            max = allStudentScore.stream().max(Double::compareTo).get();
            //只要有一个大于20分
            List<Double> scores = allStudentScore.stream().filter(score -> Double.valueOf(score) >= standScore)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(scores)) {
                for (Double score : allStudentScore) {
                    BigDecimal bigDecimalFirstScore = new BigDecimal(score.toString());
                    BigDecimal bigDecimalSecondScore = new BigDecimal(allScore.toString());
                    allScore = bigDecimalSecondScore.add(bigDecimalFirstScore).doubleValue();
                }
                totalScore = allScore;
                size = allStudentScore.size();
            } else {
                Set<ZSetOperations.TypedTuple<String>> scoresSet = zSetOperations.rangeByScoreWithScores(paperPracticeIdSore, standScore, scoreUpLine);
                if (CollectionUtils.isNotEmpty(scoresSet)) {
                    totalScore = scoresSet.stream().mapToDouble(u -> u.getScore()).sum();
                }
                size = scoresSet.size();
            }
        }
        if (size == 0) {
            size = 1;//纠正
        }

        logger.info("答题卡类型是:{},答题卡ID是:{}", standardCard.getType(), standardCard.getId());
        Double average = 60D;//默认值
        average = totalScore / size;
        int beatRate = 0;
        if (standardCard.getScore() >= standScore) {
            beatRate = (int) ((size - rank) * 100 / size);
        }

        logger.info("大于20以上的分数是:{},数量是:{},平均分是:{},排名是:{},击败比例是:{}", totalScore, size, average, rank, beatRate);
        BigDecimal bigDecimal = new BigDecimal(average);
        Double dAverage = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

        logger.info("最终平均分是:{}", dAverage);


        return CardUserMeta.builder()
                .average(dAverage)
                .beatRate(beatRate)
                .rank(rank.intValue())
                .total(allStudentScore.size())
                .max(max)
                .build();
    }

    /**
     * 缓存阶段测试报告一分钟
     *
     * @param scoreRankDtoList
     */
    @Override
    public void putPeriodReportCache(List<ScoreRankDto> scoreRankDtoList, StandardCard standardCard) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String periodReportKey = PeriodTestRedisKey.getPeriodReportKey() + "_" + standardCard.getPaper().getId() + "_" + standardCard.getSyllabusId();
        valueOperations.set(periodReportKey, JsonUtil.toJson(scoreRankDtoList), 1, TimeUnit.MINUTES);

    }

    /**
     * 查询阶段测试缓存报告
     *
     * @return
     */
    @Override
    public List<ScoreRankDto> getPeriodReportCache(StandardCard standardCard) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String periodReportKey = PeriodTestRedisKey.getPeriodReportKey() + "_" + standardCard.getPaper().getId() + "_" + standardCard.getSyllabusId();
        List<ScoreRankDto> scoreRankDtoList = Lists.newArrayList();
        try {
            String ScoreRankDtoListStr = valueOperations.get(periodReportKey);
            if (null != ScoreRankDtoListStr) {
                scoreRankDtoList = JsonUtil.toList(ScoreRankDtoListStr, ScoreRankDto.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scoreRankDtoList;
    }

    /**
     * 获取参加考试的人数
     */
    public long getCardUserMetaNum(final StandardCard standardCard) {
        //可配置
        int size = 0;

        int paperId = standardCard.getPaper().getId();

        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        if (standardCard.getType() == AnswerCardType.FORMATIVE_TEST_ESTIMATE) {
            paperPracticeIdSore = paperPracticeIdSore + "_" + standardCard.getSyllabusId();
        }
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        //往期模考，需要添加类型
        if (standardCard.getType() == AnswerCardType.MATCH_AFTER) {
            paperPracticeIdSore = PaperRedisKeys.getKeyWithType(paperPracticeIdSore, AnswerCardType.MATCH_AFTER);
        }
        Set<ZSetOperations.TypedTuple<String>> allStudentScoreInfo = zSetOperations.rangeWithScores(paperPracticeIdSore, 0, -1);
        List<Double> allStudentScore = allStudentScoreInfo.stream().map(score -> score.getScore())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(allStudentScore)) {
            return allStudentScore.size();
        }

        if (size == 0) {
            size = 1;//纠正
        }
        return size;

    }

}
