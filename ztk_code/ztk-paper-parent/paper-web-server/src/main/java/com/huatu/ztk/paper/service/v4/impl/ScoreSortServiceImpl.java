package com.huatu.ztk.paper.service.v4.impl;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.enums.ScoreSortEnum;
import com.huatu.ztk.paper.service.v4.ScoreSortService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

/**
 * Created by huangqingpeng on 2019/3/7.
 */
@Service
public class ScoreSortServiceImpl implements ScoreSortService {
    private final static Logger logger = LoggerFactory.getLogger(ScoreSortServiceImpl.class);

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void addScoreSort(StandardCard answerCard, ScoreSortEnum scoreSubmitSort) {
        String idScores = getSortKey(answerCard, scoreSubmitSort);
        double scoreSortValue = getScoreSortValue(answerCard, scoreSubmitSort);
        redisTemplate.opsForZSet().add(idScores, answerCard.getId() + "", scoreSortValue);
    }

    private String getSortKey(StandardCard answerCard, ScoreSortEnum scoreSubmitSort) {
        Paper paper = answerCard.getPaper();
        if (null == paper) {
            return "";
        }
        String idScores = scoreSubmitSort.getScoreSortCacheKey(paper.getId());
        return idScores;
    }

    /**
     * 按照排名逻辑重新计算排名
     *
     * @param answerCard      答题卡ID
     * @param scoreSubmitSort 排序方式
     */
    @Override
    public void reSort(AnswerCard answerCard, ScoreSortEnum scoreSubmitSort) {
        if (!(answerCard instanceof StandardCard)) {
            return;
        }
        StandardCard standardCard = (StandardCard) answerCard;
        String idScores = getSortKey(standardCard, scoreSubmitSort);
        ZSetOperations<String,String> zSetOperations = redisTemplate.opsForZSet();
        Long total = ScoreSortUtil.getTotal(zSetOperations, idScores);
        if(total <= 1){     //大于两个人，再做重排
            return;
        }
        Long rank = ScoreSortUtil.getRank(zSetOperations, standardCard.getId() + "", idScores, total);
        AnswerCardUtil.reBuildCardMeta(standardCard.getCardUserMeta(),rank,total);
    }


    /**
     * 获取排序用到的值
     *
     * @param answerCard
     * @param scoreSubmitSort
     * @return
     */
    private double getScoreSortValue(StandardCard answerCard, ScoreSortEnum scoreSubmitSort) {
        double score = answerCard.getScore();
        switch (scoreSubmitSort) {
            case SCORE_SORT:
                return score;
            case SCORE_SUBMIT_SORT:
                long createTime = answerCard.getCreateTime() / 1000;     //换算成秒时间戳
                return score * Math.pow(10, 12) + Math.pow(10, 12) - createTime;
            case SCORE_EXPEND_SORT:
                int expendTime = answerCard.getExpendTime();        //耗时多少秒
                return score * Math.pow(10, 4) + Math.pow(10, 4) - expendTime;
        }
        return -1;
    }
}
