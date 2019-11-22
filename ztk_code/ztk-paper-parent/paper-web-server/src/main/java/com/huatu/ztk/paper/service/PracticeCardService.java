package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.enums.CustomizeEnum;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 练习服务层
 * Created by shaojieyue
 * Created time 2016-05-03 11:26
 */

@Service
public class PracticeCardService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeCardService.class);

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private PracticeCardDubboService practiceCardDubboService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    /**
     * 创建练习试卷
     *
     * @param practicePaper
     * @param terminal
     * @return
     */
    public PracticeCard create(PracticePaper practicePaper, int terminal, int type, long userId) throws BizException {
        return practiceCardDubboService.create(practicePaper, terminal, type, userId);
    }

    /**
     * 保存答题卡
     *
     * @param answerCard
     */
    public void save(AnswerCard answerCard) {
        answerCardDao.save(answerCard);
    }

    /**
     * 保存答题卡
     */
    @Deprecated
    public void save(List<AnswerCard> answerCards) {
        AnswerCard[] array = new AnswerCard[answerCards.size()];
        for (int i = 0; i < answerCards.size(); i++) {
            array[i] = answerCards.get(i);
        }
        answerCardDao.insert(array);
    }

    /**
     * 专项练习添加未完成的练习id
     *
     * @param pointId
     * @param practiceCard
     */
    public void addCustomizesUnfinishedId(int pointId, PracticeCard practiceCard) {
        long userId = practiceCard.getUserId();
        long practiceId = practiceCard.getId();
        int subject = practiceCard.getSubject();

        String key = RedisKnowledgeKeys.getUnfinishedPointListKey(userId, subject);

        //知识点id_练习id
        String value = pointId + "_" + practiceId;
        ListOperations<String, String> opsForList = redisTemplate.opsForList();

        //最新的未完成的练习放在首位
        opsForList.leftPush(key, value);
    }
    
    /**
     * 专项练习添加未完成的练习id 区别做题模式和背题模式
     * @param pointId
     * @param practiceCard
     * @param cardType
     */
	public void addCustomizesUnfinishedIdV2(int pointId, PracticeCard practiceCard, CustomizeEnum.ModeEnum modeEnum) {
		long userId = practiceCard.getUserId();
		long practiceId = practiceCard.getId();
		int subject = practiceCard.getSubject();
		String key = RedisKnowledgeKeys.getUnfinishedPointListKey(userId, subject);
		if (modeEnum == CustomizeEnum.ModeEnum.Look) {
			key = RedisKnowledgeKeys.getUnfinishedPointListKeyV2(userId, subject, modeEnum.getKey());
		}
		// 知识点id_练习id
		String value = pointId + "_" + practiceId;
		ListOperations<String, String> opsForList = redisTemplate.opsForList();

		// 最新的未完成的练习放在首位
		opsForList.leftPush(key, value);
	}
    
    /**
     * 更新未完成的专项练习知识点列表
     *
     * @param answerCard
     */
    public void updateUnfinishedPointList(AnswerCard answerCard, CustomizeEnum.ModeEnum modeEnum) {
        if (answerCard instanceof StandardCard) {
            return;
        }
        long userId = answerCard.getUserId();
        long answerCardId = answerCard.getId();
        int subject = answerCard.getSubject();

        ListOperations<String, String> opsForList = redisTemplate.opsForList();

        String unfinishedPointList = RedisKnowledgeKeys.getUnfinishedPointListKey(userId, subject);
		if (modeEnum == CustomizeEnum.ModeEnum.Look) {
			unfinishedPointList = RedisKnowledgeKeys.getUnfinishedPointListKeyV2(userId, subject,
					CustomizeEnum.ModeEnum.Look.getKey());
		}

        //最新的未完成的专项练习放在第一位,知识点id_练习id
        List<String> idList = opsForList.range(unfinishedPointList, 0, 0);
        if (CollectionUtils.isNotEmpty(idList)) {
            //未完成的练习id
            long lastPracticeId = Long.valueOf(idList.get(0).split("_")[1]);
            if (lastPracticeId == answerCardId) {
                //弹出
                opsForList.leftPop(unfinishedPointList);
            }
        }
    }
    
   

    /**
     * 未完成答题卡查询
     *
     * @param userId
     * @param subject
     * @return
     */
    public Map<Integer, Long> getUnfinishedPointMap(long userId, int subject) {
        ListOperations<String, String> opsForList = redisTemplate.opsForList();
        String unfinishedPointKey = RedisKnowledgeKeys.getUnfinishedPointListKey(userId, subject);

        List<String> unfinishedList = opsForList.range(unfinishedPointKey, 0, 0);
        Map<Integer, Long> unfinishedPointMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(unfinishedList)) {
            //知识点id_练习id
            String last = unfinishedList.get(0);
            int unfinishedPointId = Integer.valueOf(last.split("_")[0]);
            long practiceId = Long.valueOf(last.split("_")[1]);

            unfinishedPointMap.put(unfinishedPointId, practiceId);

            QuestionPoint tmpPoint = questionPointDubboService.findById(unfinishedPointId);
            logger.info("userId是:{},未完成的知识点ID是:{},知识点信息是:{}", userId, unfinishedPointId, tmpPoint);
            //找出上一级知识点,直到顶级
            if (null != tmpPoint) {
                while (tmpPoint.getParent() != 0) {
                    tmpPoint = questionPointDubboService.findById(tmpPoint.getParent());
                    unfinishedPointMap.put(tmpPoint.getId(), practiceId);
                }
            }
        }
        return unfinishedPointMap;
    }
    
    /**
     * 未完成答题卡查询 区别背题模式
     * @param userId
     * @param subject
     * @param modeEnum
     * @return
     */
    public Map<Integer, Long> getUnfinishedPointMapV2(long userId, int subject,CustomizeEnum.ModeEnum modeEnum) {
        ListOperations<String, String> opsForList = redisTemplate.opsForList();
        //如果是做题模式用之前记录
		String unfinishedPointKey = RedisKnowledgeKeys.getUnfinishedPointListKey(userId, subject);
		if (modeEnum == CustomizeEnum.ModeEnum.Look) {
			unfinishedPointKey = RedisKnowledgeKeys.getUnfinishedPointListKeyV2(userId, subject, modeEnum.getKey());
		}

        List<String> unfinishedList = opsForList.range(unfinishedPointKey, 0, 0);
        Map<Integer, Long> unfinishedPointMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(unfinishedList)) {
            //知识点id_练习id
            String last = unfinishedList.get(0);
            int unfinishedPointId = Integer.valueOf(last.split("_")[0]);
            long practiceId = Long.valueOf(last.split("_")[1]);

            unfinishedPointMap.put(unfinishedPointId, practiceId);

            QuestionPoint tmpPoint = questionPointDubboService.findById(unfinishedPointId);
            logger.info("userId是:{},未完成的知识点ID是:{},知识点信息是:{}", userId, unfinishedPointId, tmpPoint);
            //找出上一级知识点,直到顶级
            if (null != tmpPoint) {
                while (tmpPoint.getParent() != 0) {
                    tmpPoint = questionPointDubboService.findById(tmpPoint.getParent());
                    unfinishedPointMap.put(tmpPoint.getId(), practiceId);
                }
            }
        }
        return unfinishedPointMap;
    }
    
}
