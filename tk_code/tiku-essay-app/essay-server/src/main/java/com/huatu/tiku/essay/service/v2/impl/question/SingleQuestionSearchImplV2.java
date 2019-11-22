package com.huatu.tiku.essay.service.v2.impl.question;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssaySimilarQuestionGroupInfo;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.manager.QuestionManager;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.repository.EssaySimilarQuestionGroupInfoRepository;
import com.huatu.tiku.essay.repository.EssaySimilarQuestionRepository;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.impl.question.QuestionTypeTreeComponent;
import com.huatu.tiku.essay.service.v2.question.SingleQuestionSearchV2;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.correct.ResponseExtendVO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/10/26
 */
@Service
@Slf4j
public class SingleQuestionSearchImplV2 implements SingleQuestionSearchV2 {

    @Autowired
    private QuestionTypeTreeComponent questionTypeTreeComponent;

    @Autowired
    private EssaySimilarQuestionRepository essaySimilarQuestionRepository;

    @Autowired
    private EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;

    @Autowired
    private EssayQuestionBaseRepository essayQuestionBaseRepository;

    @Autowired
    private EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private EssayQuestionService essayQuestionService;

    @Override
    public PageUtil<List<EssaySimilarQuestionGroupInfo>> findSimilarQuestionPageInfo(Pageable pageRequest, int type) {
        ValueOperations<String, PageUtil<List<EssaySimilarQuestionGroupInfo>>> valueOperations = redisTemplate.opsForValue();
        String cacheKey = RedisKeyConstant.getNewSingleQuestionPrefix(type, pageRequest.getPageNumber(), pageRequest.getPageSize());
        PageUtil<List<EssaySimilarQuestionGroupInfo>> cachePageInfo = valueOperations.get(cacheKey);

        //没有缓存信息 - 查询实体库
        if (null == cachePageInfo || cachePageInfo.getTotal() == 0) {
            Long count = getSingleQuestionTotalByType(type);
            PageUtil<List<EssaySimilarQuestionGroupInfo>> pageUtil = PageUtil.<List<EssaySimilarQuestionGroupInfo>>builder()
                    .total(0L)
                    .totalPage(0L)
                    .result(Lists.newArrayList())
                    .next(0)
                    .build();

            if (count > 0) {
                //查询该题型下所有题型，包括自己
                List<Integer> typeIdList = questionTypeTreeComponent.getQuestionTypeTreeAndReturnId(type);
                if (CollectionUtils.isNotEmpty(typeIdList)) {
                    //根据题型查询题型下当前分页所有可用题组
                    List<EssaySimilarQuestionGroupInfo> similarQuestionList = essaySimilarQuestionGroupInfoRepository.findByBizStatusAndStatusAndTypeIn(
                            EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(),
                            EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus(),
                            typeIdList, pageRequest);
                    //排序（showMsg倒序排列）
                    Collections.sort(similarQuestionList, (o1, o2) -> o2.getShowMsg().compareTo(o1.getShowMsg()));
                    pageUtil.setResult(similarQuestionList);
                    pageUtil.setNext(count > (pageRequest.getPageNumber() + 1) * pageRequest.getPageSize() ? 1 : 0);
                    pageUtil.setTotalPage(count / pageRequest.getPageSize() + (count % pageRequest.getPageSize()) > 0 ? 1 : 0);
                    pageUtil.setTotal(count);
                    //放入缓存
                    valueOperations.set(cacheKey, pageUtil, 5, TimeUnit.MINUTES);
                    return pageUtil;
                }
            }
            return pageUtil;
        }
        return cachePageInfo;

    }

    @Override
    public List<EssayQuestionVO> findSimilarQuestionList(List<EssaySimilarQuestionGroupInfo> similarQuestionGroupInfoList, final int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        //查询该题型下题目（5分钟缓存）
        if (CollectionUtils.isNotEmpty(similarQuestionGroupInfoList)) {
            //存储单题组-area列表的关联关系
            HashMap<Long, List<EssayQuestionAreaVO>> similarQuestionAreaListMap = new HashMap<>();
            //存储 此次查询的所有试题-area关联信息
            Map<Long, EssayQuestionAreaVO> allQuestionAreaMap = Maps.newHashMap();

            log.info("试题ID是:{}", allQuestionAreaMap.keySet());
            similarQuestionGroupInfoList.stream().forEach(essaySimilarQuestionGroupInfo -> {
                //根据题组id查询题目信息（5分钟缓存）
                List<EssayQuestionAreaVO> areaList = findAreaList(essaySimilarQuestionGroupInfo.getId());
                similarQuestionAreaListMap.put(essaySimilarQuestionGroupInfo.getId(), areaList);
                if (CollectionUtils.isNotEmpty(areaList)) {
                    for (EssayQuestionAreaVO essayQuestionAreaVO : areaList) {
                        allQuestionAreaMap.put(essayQuestionAreaVO.getQuestionBaseId(), essayQuestionAreaVO);
                    }
                }
            });
            //1.统计所有试题已被批改数量 intelligenceCorrectNumMap 智能批改次数，manualCorrectNumMap 人工批改次数
            Map<Long, Integer> intelligenceCorrectSumMap = new HashMap<>();
            Map<Long, Integer> manualCorrectSumMap = new HashMap<>();
            for (Long questionBaseId : allQuestionAreaMap.keySet()) {
                Map<CorrectModeEnum, Integer> questionCorrectNumMap = getQuestionCorrectCount(questionBaseId);
                intelligenceCorrectSumMap.put(questionBaseId, MapUtils.getIntValue(questionCorrectNumMap, CorrectModeEnum.INTELLIGENCE, 0));
                manualCorrectSumMap.put(questionBaseId, MapUtils.getIntValue(questionCorrectNumMap, CorrectModeEnum.MANUAL, 0));
            }
            //单体组所有的questionBaseIds
            List<Long> questionIds = allQuestionAreaMap.keySet().stream().collect(Collectors.toList());
            UserCorrectModeInfo userCorrectModeInfo = invoke(userId, questionIds,modeTypeEnum);
            List<EssayQuestionAnswer> userAllAnswerCardList = userCorrectModeInfo.getUserAllAnswerCardList();
            Map<Long, Long> userIntelligenceCorrectNumMap = userCorrectModeInfo.getUserIntelligenceCorrectNumMap();
            Map<Long, Long> userManualCorrectNumMap = userCorrectModeInfo.getUserManualCorrectNumMap();


            Map<Long, List<EssayQuestionAnswer>> userAllAnswerCardMap = userAllAnswerCardList.parallelStream()
                    .collect(Collectors.groupingBy(card -> card.getQuestionBaseId(), Collectors.toList()));

            //构建 最终 返回的Vo
            List<EssayQuestionVO> resultList = similarQuestionGroupInfoList.parallelStream().map(similarQuestion -> {

                EssayQuestionVO essayQuestionVO = EssayQuestionVO.builder()
                        .similarId(similarQuestion.getId())
                        .showMsg(similarQuestion.getShowMsg())
                        .correctTimes(0L)
                        .correctNum(0)
                        .correctSum(0)
                        .manualSum(0)
                        .manualNum(0)
                        .videoAnalyzeFlag(false)
                        .bizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus())
                        .build();

                List<EssayQuestionAreaVO> essayQuestionAreaVOList = similarQuestionAreaListMap.get(similarQuestion.getId());
                if (CollectionUtils.isEmpty(essayQuestionAreaVOList)) {
                    return essayQuestionVO;
                }

                for (EssayQuestionAreaVO questionAreaVO : essayQuestionAreaVOList) {
                    questionAreaVO.setQuestionDate(null);
                    questionAreaVO.setQuestionYear(null);
                    questionAreaVO.setPaperId(null);
                    questionAreaVO.setLimitTime(null);
                    questionAreaVO.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());

                    int currentCorrectSum = MapUtils.getIntValue(intelligenceCorrectSumMap, questionAreaVO.getQuestionBaseId(), 0);
                    int currentManualSum = MapUtils.getIntValue(manualCorrectSumMap, questionAreaVO.getQuestionBaseId(), 0);
                    //全站统计
                    essayQuestionVO.setCorrectSum(essayQuestionVO.getCorrectSum() + currentCorrectSum);
                    essayQuestionVO.setManualSum(essayQuestionVO.getManualSum() + currentManualSum);

                    int currentUserCorrectNum = MapUtils.getIntValue(userIntelligenceCorrectNumMap, questionAreaVO.getQuestionBaseId(), 0);
                    int currentUserManualNum = MapUtils.getIntValue(userManualCorrectNumMap, questionAreaVO.getQuestionBaseId(), 0);
                    questionAreaVO.setCorrectNum(currentUserCorrectNum);
                    questionAreaVO.setManualNum(currentUserManualNum);

                    //用户统计
                    essayQuestionVO.setCorrectNum(essayQuestionVO.getCorrectNum() + currentUserCorrectNum);
                    essayQuestionVO.setManualNum(essayQuestionVO.getManualNum() + currentUserManualNum);
                    List<EssayQuestionAnswer> answers = userAllAnswerCardMap.get(questionAreaVO.getQuestionBaseId());
                    dealEssayQuestionAreaVOInfo(answers, questionAreaVO);
                }
                //填充是否有视频解析的标志位
                boolean videoAnalyzeFlag = essayQuestionAreaVOList.stream().anyMatch(question -> null != question.getVideoId() && question.getVideoId() > 0);
                essayQuestionVO.setVideoAnalyzeFlag(videoAnalyzeFlag);
                essayQuestionVO.setQuestionType(similarQuestion.getType());
                essayQuestionVO.setEssayQuestionBelongPaperVOList(essayQuestionAreaVOList);
                return essayQuestionVO;
            })
                    .collect(Collectors.toList());

            return resultList;
        }
        return Lists.newArrayList();
    }

    /**
     * 处理每道题的答题卡状态
     *
     * @param answers
     * @param questionAreaVO
     */
    private void dealEssayQuestionAreaVOInfo(List<EssayQuestionAnswer> answers, EssayQuestionAreaVO questionAreaVO) {
        ResponseExtendVO responseExtendVO = ResponseExtendVO.builder().build();
        essayQuestionService.dealQuestionResponseExtendInfo(answers, responseExtendVO);
        questionAreaVO.setManualRecentStatus(responseExtendVO.getManualRecentStatus());
        questionAreaVO.setRecentStatus(responseExtendVO.getRecentStatus());
        questionAreaVO.setCorrectMode(responseExtendVO.getCorrectMode());
        questionAreaVO.setLastType(responseExtendVO.getLastType());
    }

    /**
     * 获取单题总量
     *
     * @param type 类型
     * @return 总数量
     */
    private Long getSingleQuestionTotalByType(int type) {
        List<Integer> typeIdList = questionTypeTreeComponent.getQuestionTypeTreeAndReturnId(type);
        if (CollectionUtils.isEmpty(typeIdList)) {
            return 0L;
        }
        return essaySimilarQuestionGroupInfoRepository.countByBizStatusAndStatusAndTypeIn(
                EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(),
                EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus(),
                typeIdList
        );
    }


    /**
     * 获取每道试题的智能 && 人工批改次数
     * <p>
     * 智能批改
     * 人工批改
     *
     * @param questionId 试题ID
     * @return 统计数量
     */
    private Map<CorrectModeEnum, Integer> getQuestionCorrectCount(long questionId) {
        Map<CorrectModeEnum, Integer> result = Maps.newHashMap();
        ValueOperations<String, Integer> valueOperations = redisTemplate.opsForValue();
        String intelligenceKey = RedisKeyConstant.getQuestionCorrectNumKey(questionId, CorrectModeEnum.INTELLIGENCE.getMode());
        String manualKey = RedisKeyConstant.getQuestionCorrectNumKey(questionId, CorrectModeEnum.MANUAL.getMode());
        Integer intelligenceCount = valueOperations.get(intelligenceKey);
        Integer manualCount = valueOperations.get(manualKey);
        //查询状态为1或者状态为2(被回收)
        List<Integer> statusList = Lists.newArrayList(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.RECYCLED.getStatus());
        if (null == intelligenceCount || intelligenceCount == 0) {
            intelligenceCount = essayQuestionAnswerRepository.countByQuestionBaseIdAndStatusInAndBizStatusAndCorrectModeIn(questionId, statusList, EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(), Lists.newArrayList(CorrectModeEnum.INTELLIGENCE.getMode()));
            valueOperations.set(intelligenceKey, intelligenceCount, 10, TimeUnit.MINUTES);
        }
        if (null == manualCount || manualCount == 0) {
            ArrayList<Integer> manualCorrectModes = Lists.newArrayList(CorrectModeEnum.MANUAL.getMode(), CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode());
            manualCount = essayQuestionAnswerRepository.countByQuestionBaseIdAndStatusInAndBizStatusAndCorrectModeIn(questionId, statusList, EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(), manualCorrectModes);
            valueOperations.set(manualKey, manualCount, 10, TimeUnit.MINUTES);
        }
        result.put(CorrectModeEnum.INTELLIGENCE, intelligenceCount);
        result.put(CorrectModeEnum.MANUAL, manualCount);
        return result;
    }


    /**
     * 获取题组下试题信息
     */
    public List<EssayQuestionAreaVO> findAreaList(Long similarQuestionId) {
        ValueOperations<String, List<EssayQuestionAreaVO>> valueOperations = redisTemplate.opsForValue();
        //根据题组id，获取题组下试题信息（走缓存，5分钟失效）
        String questionOfGroupKey = RedisKeyConstant.getQuestionOfGroupKey(similarQuestionId);
        List<EssayQuestionAreaVO> essayQuestionAreaVOList = valueOperations.get(questionOfGroupKey);
        if (CollectionUtils.isEmpty(essayQuestionAreaVOList)) {
            //根据similarId查询题组所有试题  走索引
            List<Long> questionIds = essaySimilarQuestionRepository.findQuestionBaseIdBySimilarIdAndStatus(similarQuestionId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            //根据相似题目的id查询题目base信息
            if (CollectionUtils.isNotEmpty(questionIds)) {
                LinkedList<EssayQuestionBase> baseList = essayQuestionBaseRepository.findList(questionIds);
                essayQuestionAreaVOList = QuestionManager.changeEssayQuestionBaseToEssayQuestionAreaVO(baseList);
                valueOperations.set(questionOfGroupKey, essayQuestionAreaVOList, 5, TimeUnit.MINUTES);
                return essayQuestionAreaVOList;
            }
        }
        return essayQuestionAreaVOList;
    }

    /**
     * @param userId
     * @param questionIds
     * @param modeTypeEnum
     * @return
     */
    @Override
    public UserCorrectModeInfo invoke(int userId, List<Long> questionIds, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        List<Integer> statusList = Lists.newArrayList(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.RECYCLED.getStatus());
        //2.统计用户所有试题答题卡信息
        List<EssayQuestionAnswer> userAllAnswerCardList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndStatusInAndQuestionBaseIdInAndAnswerCardType(
                userId, 0L, statusList, questionIds,modeTypeEnum.getType());

        //用户每道题智能答题卡数量统计 - 已批该
        Map<Long, Long> userIntelligenceCorrectNumMap = userAllAnswerCardList.parallelStream()
                .filter(card -> card.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus())
                .filter(card -> null != card.getCorrectMode() && card.getCorrectMode().equals(CorrectModeEnum.INTELLIGENCE.getMode()))
                .collect(Collectors.groupingBy(card -> card.getQuestionBaseId(), Collectors.counting()));

        //用户每道题人工答题卡数量统计 - 已批改
        Map<Long, Long> userManualCorrectNumMap = userAllAnswerCardList.parallelStream()
                .filter(card -> card.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus())
                .filter(card -> null != card.getCorrectMode())
                .filter(card -> card.getCorrectMode().equals(CorrectModeEnum.MANUAL.getMode()) || card.getCorrectMode().equals(CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode()))
                .collect(Collectors.groupingBy(card -> card.getQuestionBaseId(), Collectors.counting()));
        return UserCorrectModeInfo.builder().userAllAnswerCardList(userAllAnswerCardList)
                .userIntelligenceCorrectNumMap(userIntelligenceCorrectNumMap)
                .userManualCorrectNumMap(userManualCorrectNumMap)
                .questionIds(questionIds)
                .userId(userId)
                .build();
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class UserCorrectModeInfo implements Serializable {
        private int userId;
        private List<Long> questionIds;
        private List<EssayQuestionAnswer> userAllAnswerCardList;
        private Map<Long, Long> userIntelligenceCorrectNumMap;
        private Map<Long, Long> userManualCorrectNumMap;

        @Builder
        public UserCorrectModeInfo(int userId, List<Long> questionIds, List<EssayQuestionAnswer> userAllAnswerCardList, Map<Long, Long> userIntelligenceCorrectNumMap, Map<Long, Long> userManualCorrectNumMap) {
            this.userId = userId;
            this.questionIds = questionIds;
            this.userAllAnswerCardList = userAllAnswerCardList;
            this.userIntelligenceCorrectNumMap = userIntelligenceCorrectNumMap;
            this.userManualCorrectNumMap = userManualCorrectNumMap;
        }
    }
}
