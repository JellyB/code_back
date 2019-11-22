package com.huatu.tiku.essay.service.impl.question;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssaySimilarQuestionGroupInfo;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.manager.QuestionManager;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.repository.EssaySimilarQuestionGroupInfoRepository;
import com.huatu.tiku.essay.repository.EssaySimilarQuestionRepository;
import com.huatu.tiku.essay.service.question.SingleQuestionSearch;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/10/26
 */
@Service
@Slf4j
public class SingleQuestionSearchImpl implements SingleQuestionSearch {

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

    @Override
    public PageUtil<List<EssaySimilarQuestionGroupInfo>> findSimilarQuestionPageInfo(Pageable pageRequest, int type) {
        ValueOperations<String, PageUtil<List<EssaySimilarQuestionGroupInfo>>> valueOperations = redisTemplate.opsForValue();
        String cacheKey = RedisKeyConstant.getNewSingleQuestionPrefix(type, pageRequest.getPageNumber(), pageRequest.getPageSize());
        PageUtil<List<EssaySimilarQuestionGroupInfo>> cachePageInfo = valueOperations.get(cacheKey);

        //没有缓存信息 - 查询实体库
        if (null == cachePageInfo || cachePageInfo.getTotal() == 0) {
            Long count = getSingleQuestionTotalByType(type);
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
                    PageUtil<List<EssaySimilarQuestionGroupInfo>> pageUtil = PageUtil.<List<EssaySimilarQuestionGroupInfo>>builder()
                            .result(similarQuestionList)
                            .next(count > pageRequest.getPageNumber() * pageRequest.getPageSize() ? 1 : 0)
                            .total(count)
                            .build();
                    //放入缓存
                    valueOperations.set(cacheKey, pageUtil, 5, TimeUnit.MINUTES);
                    return valueOperations.get(cacheKey);
                }
            }
            return PageUtil.<List<EssaySimilarQuestionGroupInfo>>builder()
                    .total(0L)
                    .totalPage(0L)
                    .result(Lists.newArrayList())
                    .next(0)
                    .build();
        }
        return cachePageInfo;

    }

    @Override
    public List<EssayQuestionVO> findSimilarQuestionList(List<EssaySimilarQuestionGroupInfo> similarQuestionGroupInfoList, final int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        //查询该题型下题目（5分钟缓存）
        if (CollectionUtils.isNotEmpty(similarQuestionGroupInfoList)) {
            //存储 所有单题-试题的关联关系
            HashMap<Long, List<EssayQuestionAreaVO>> map = new HashMap<>();
            //存储 此次查询的所有试题信息
            List<EssayQuestionAreaVO> allQuestionAreaList = Lists.newArrayList();
            similarQuestionGroupInfoList
                    .stream()
                    .forEach(essaySimilarQuestionGroupInfo -> {
                        //根据题组id查询题目信息（5分钟缓存）
                        List<EssayQuestionAreaVO> areaList = findAreaList(essaySimilarQuestionGroupInfo.getId());
                        map.put(essaySimilarQuestionGroupInfo.getId(), areaList);

                        if (CollectionUtils.isNotEmpty(areaList)) {
                            allQuestionAreaList.addAll(areaList);
                        }
                    });
            //log.info("allQuestionAreaList = {}", allQuestionAreaList);
            //1.统计所有试题 已被批改 数量
            Map<Long, Integer> questionCorrectNumMap = new HashMap<>();
//                    allQuestionAreaList.stream()
//                    .filter(vo -> null != vo && null != vo.getId())
//                    .collect(Collectors.toMap(EssayQuestionAreaVO::getId, vo -> getQuestionCorrectCount(vo.getId())));
            for( EssayQuestionAreaVO areaVO: allQuestionAreaList){
                if(areaVO != null && areaVO.getId() != null ){
                    int questionCorrectCount = getQuestionCorrectCount(areaVO.getId());
                    questionCorrectNumMap.put(areaVO.getId(),questionCorrectCount);

                }
            }

            //2.统计用户所有答题卡信息
            List<EssayQuestionAnswer> userAnswerCardInfoList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndStatusAndQuestionBaseIdInAndAnswerCardType(
                    userId, 0L, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    allQuestionAreaList.stream().map(EssayQuestionAreaVO::getQuestionBaseId).collect(Collectors.toList()),
                    modeTypeEnum.getType()
            );
            //log.info("userAnswerCardInfoList = {}", userAnswerCardInfoList);
            //用户已完成 数量统计
            Map<Long, Long> userCorrectNumMap = userAnswerCardInfoList.parallelStream()
                    .filter(essayQuestionAnswer -> null != essayQuestionAnswer)
                    .filter(card -> card.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus())
                    .collect(Collectors.groupingBy(card -> card.getQuestionBaseId(), Collectors.counting()));
            //用户未 完成数量统计
            Set<Long> userUnfinishedSet = userAnswerCardInfoList.parallelStream()
                    .filter(card -> card.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus())
                    .filter(card -> !userCorrectNumMap.containsKey(card.getQuestionBaseId()))
                    .map(EssayQuestionAnswer::getQuestionBaseId)
                    .collect(Collectors.toSet());

            //构建 最终 返回的Vo
            List<EssayQuestionVO> resultList = similarQuestionGroupInfoList.parallelStream()
                    .map(similarQuestion -> {
                        EssayQuestionVO essayQuestionVO = EssayQuestionVO.builder()
                                .similarId(similarQuestion.getId())
                                .showMsg(similarQuestion.getShowMsg())
                                .correctTimes(0L)
                                .correctSum(0)
                                .videoAnalyzeFlag(false)
                                .bizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus())
                                .build();
                        List<EssayQuestionAreaVO> essayQuestionAreaVOList = map.get(similarQuestion.getId());
                        if (CollectionUtils.isNotEmpty(essayQuestionAreaVOList)) {
                            for (EssayQuestionAreaVO questionAreaVO : essayQuestionAreaVOList) {
                                questionAreaVO.setQuestionDate(null);
                                questionAreaVO.setQuestionYear(null);
                                questionAreaVO.setPaperId(null);
                                questionAreaVO.setLimitTime(null);
                                questionAreaVO.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
                                //总交卷次数
                                int questionCorrectNum = questionCorrectNumMap.getOrDefault(questionAreaVO.getQuestionBaseId(), 0).intValue();
                                questionAreaVO.setCorrectSum(questionCorrectNum);
                                essayQuestionVO.setCorrectSum(essayQuestionVO.getCorrectSum() + questionCorrectNum);
                                //用户交卷次数
                                int userCorrectNum = userCorrectNumMap.getOrDefault(questionAreaVO.getQuestionBaseId(), 0L).intValue();
                                questionAreaVO.setCorrectTimes(userCorrectNum);
                                essayQuestionVO.setCorrectTimes(essayQuestionVO.getCorrectTimes() + userCorrectNum);
                                if (userCorrectNum == 0) {
                                    //如果没有批改次数
                                    if (userUnfinishedSet.contains(questionAreaVO.getQuestionBaseId())) {
                                        questionAreaVO.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
                                        essayQuestionVO.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
                                    }
                                }
                            }

                            //填充是否有视频解析的标志位
                            boolean videoAnalyzeFlag = essayQuestionAreaVOList.stream().anyMatch(question -> null != question.getVideoId() && question.getVideoId() > 0);
                            essayQuestionVO.setVideoAnalyzeFlag(videoAnalyzeFlag);
                        }
                        essayQuestionVO.setEssayQuestionBelongPaperVOList(essayQuestionAreaVOList);
                        return essayQuestionVO;
                    })
                    .collect(Collectors.toList());

            return resultList;
        }
        return Lists.newArrayList();
    }


    @Override
    public List<EssayQuestionAreaVO> findSimilarQuestionAreaVOInfoList(long similarId, int userId) {
        EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(similarId);
        if (null == similarQuestionGroupInfo) {
            return Lists.newArrayList();
        }
        ArrayList<EssaySimilarQuestionGroupInfo> infoList = Lists.newArrayList();
        infoList.add(similarQuestionGroupInfo);
        List<EssayQuestionVO> singleQuestionList = findSimilarQuestionList(infoList, userId, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
        if (CollectionUtils.isEmpty(singleQuestionList)) {
            return Lists.newArrayList();
        }
        return singleQuestionList.stream()
                .filter(similarQuestion -> CollectionUtils.isNotEmpty(similarQuestion.getEssayQuestionBelongPaperVOList()))
                .flatMap(similarQuestion -> similarQuestion.getEssayQuestionBelongPaperVOList().stream())
                .collect(Collectors.toList());
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
     * 获取每道试题的更改次数
     *
     * @param questionId 试题ID
     * @return 统计数量
     */
    private int getQuestionCorrectCount(long questionId) {
        if(1416 == questionId){
            log.info("=======1416=======");
        }
        ValueOperations<String, Integer> valueOperations = redisTemplate.opsForValue();
        String questionCorrectNumKey = RedisKeyConstant.getQuestionCorrectNumKey(questionId);
        Integer cacheCount = valueOperations.get(questionCorrectNumKey);
        if (null == cacheCount || cacheCount == 0) {
            int count = essayQuestionAnswerRepository.countByQuestionBaseIdAndStatusAndBizStatus(
                    questionId,
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()
            );
            valueOperations.set(questionCorrectNumKey, count, 5, TimeUnit.MINUTES);
            return valueOperations.get(questionCorrectNumKey);
        }
        return cacheCount == null ? 0 : cacheCount;
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
                return valueOperations.get(questionOfGroupKey);
            }
        }
        return essayQuestionAreaVOList;
    }


}
