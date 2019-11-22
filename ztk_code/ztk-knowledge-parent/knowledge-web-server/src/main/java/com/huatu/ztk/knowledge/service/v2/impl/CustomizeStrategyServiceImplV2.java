package com.huatu.ztk.knowledge.service.v2.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.knowledge.common.QuestionPointLevel;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.dao.QuestionUserMetaDao;
import com.huatu.ztk.knowledge.service.v1.QuestionFinishServiceV1;
import com.huatu.ztk.knowledge.service.v1.QuestionPointServiceV1;
import com.huatu.ztk.knowledge.service.v2.CustomizeStrategyServiceV2;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.CompositeQuestion;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionUserMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class CustomizeStrategyServiceImplV2 implements CustomizeStrategyServiceV2 {

    private static final Logger logger = LoggerFactory.getLogger(CustomizeStrategyServiceImplV2.class);


    @Autowired
    private ModuleDubboService moduleDubboService;

    @Autowired
    @Qualifier("questionFinishServiceImplV2")
    private QuestionFinishServiceV1 questionFinishServiceV2;

    @Autowired
    @Qualifier("questionPointServiceImplV2")
    private QuestionPointServiceV1 questionPointServiceV2;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QuestionUserMetaDao questionUserMetaDao;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private QuestionDubboService questionDubboService;

    /**
     * 专线训练抽题策略
     *
     * @param userId
     * @param subject
     * @param pointId
     * @param size
     * @return
     */
    @Override
    public QuestionStrategy randCustomizeStrategy(long userId, int subject, Integer pointId, int size) {
        //初始化数据结构
        Supplier<QuestionStrategy> initStrategySupplier = (() -> QuestionStrategy.builder()
                .modules(Lists.newArrayList(Module.builder().category(pointId).build()))
                .questions(Lists.newArrayList())
                .difficulty(0)
                .build());
        List<QuestionPoint> questionPoints = getAllPoints(pointId, subject);    //知识点下已经包含了题量信息
        if (CollectionUtils.isEmpty(questionPoints)) {
            return initStrategySupplier.get();
        }
        Map<Integer, Integer> finishCountMap = questionFinishServiceV2.countByPoints(questionPoints.stream().map(QuestionPoint::getId).collect(Collectors.toList()), userId);
        /**
         * 组装策略分配
         */
        Map<CustomizeUtil.CustomizeStrategyEnum, List<Integer>> strategyEnumMap = CustomizeUtil.assignPointStrategy(questionPoints, finishCountMap, size);
        System.out.println("组卷策略:" + JsonUtil.toJson(strategyEnumMap));
        if (MapUtils.isEmpty(strategyEnumMap)) {
            return initStrategySupplier.get();
        }
        /**
         * 针对试题单个知识点执行抽题策略
         */
        Map<Integer, List<Integer>> pointQuestionIds = Maps.newHashMap();
        if (strategyEnumMap.containsKey(CustomizeUtil.CustomizeStrategyEnum.NO_REPEAT)) {
            fillUnfinishedQuestionIds(userId, pointQuestionIds, strategyEnumMap.get(CustomizeUtil.CustomizeStrategyEnum.NO_REPEAT));
        }
        if (strategyEnumMap.containsKey(CustomizeUtil.CustomizeStrategyEnum.ERROR_COUNT)) {
            fillErrorCountMaxQuestionIds(userId, pointQuestionIds, strategyEnumMap.get(CustomizeUtil.CustomizeStrategyEnum.ERROR_COUNT), size);
        }
        //平衡每个知识点下抽题数量
        averageQuestionByPoint(pointQuestionIds, size);
        List<Integer> questionIds = Lists.newArrayList();
        for (Map.Entry<Integer, List<Integer>> entry : pointQuestionIds.entrySet()) {
            questionIds.addAll(entry.getValue());
        }
        return getRandomQuestionStrategy(questionIds, size);
    }

    /**
     * 知识点下试题平均抽取
     * @param pointQuestionIds
     * @param size
     */
    private void averageQuestionByPoint(Map<Integer, List<Integer>> pointQuestionIds, int size) {
        if (MapUtils.isEmpty(pointQuestionIds)) {
            return;
        }
        List<Map.Entry<Integer, List<Integer>>> entries = pointQuestionIds.entrySet().stream().sorted(Comparator.comparing(i -> i.getValue().size())).collect(Collectors.toList());
        int pointSize = entries.size();
        for (Map.Entry<Integer, List<Integer>> entry : entries) {
            int average = size / Math.max(1,pointSize) + 1;
            Integer key = entry.getKey();
            List<Integer> ids = entry.getValue();
            int min = Math.min(ids.size(), average);
            pointQuestionIds.put(key, ids.subList(0, min));
            size -= min;
            pointSize--;
        }

    }

    /**
     * 组装试题Id策略--复合题数据连带查询
     *
     * @param questionIds
     * @param size
     * @return
     */
    private QuestionStrategy getRandomQuestionStrategy(List<Integer> questionIds, int size) {
        Collections.shuffle(questionIds);
        List<Integer> tempList = questionIds.subList(0, Math.min(size,questionIds.size()));
        //模块试题对应关系,用array list 来保证试题的顺序，这里主要是解决复合题之间的连续问题
        final ArrayListMultimap<Integer, Integer> multimap = ArrayListMultimap.create();
        int difficultSum = 0;
        //包含的复合题id列表
        Set<Integer> parents = new HashSet<>();
        final Map<Integer, Question> questionMap = getQuestions(tempList);

        Function<Integer, Integer> getParent = (id -> {
            try {
                if (null == id || id <= 0) {
                    return null;
                }
                Question question = questionMap.get(id);
                if (null != question && question instanceof GenericQuestion) {
                    return ((GenericQuestion) question).getParent();
                }
            } catch (Exception e) {
                logger.error("ex={}", e.getMessage());
            }
            return null;
        });

        Function<Integer, Integer> handlerSingleQuestion = (id -> {
            if (null == id || id <= 0) {
                return 0;
            }
            Question question = questionMap.get(id);
            if (question instanceof GenericQuestion) {
                List<Integer> points = ((GenericQuestion) question).getPoints();
                if (CollectionUtils.isNotEmpty(points)) {
                    //加入模块
                    multimap.put(points.get(0), id);
                }
            }

            //难度系数返回
            return question.getDifficult();
        });
        for (Integer questionId : tempList) {
            try {
                Integer parent = getParent.apply(questionId);
                if (null == parent) {
                    logger.info("question is not existed parent,questionId={}", questionId);
                    continue;
                } else if (parent > 0) {//说明是复合题
                    if (parents.contains(parent)) {//包含说明已经处理过,则不进行处理
                        continue;
                    }
                    parents.add(parent);//添加到已处理列表
                    Question parentQuestion = questionMap.get(parent);
                    if (null == parentQuestion) {
                        difficultSum = difficultSum + handlerSingleQuestion.apply(questionId);
                        continue;
                    }
                    if (!(parentQuestion instanceof CompositeQuestion)) {
                        continue;
                    }
                    List<Question> childrens = parentQuestion.getChildrens();
                    if (CollectionUtils.isEmpty(childrens)) {
                        difficultSum = difficultSum + handlerSingleQuestion.apply(questionId);
                        logger.error("parent={} has not sub ids.", parent);
                        continue;
                    }
                    //复合题的子题是用,分割的
                    List<Integer> ids = childrens.stream().map(Question::getId).collect(Collectors.toList());
                    for (Integer id : ids) {
                        //难度系数添加
                        difficultSum = difficultSum + handlerSingleQuestion.apply(id);
                        ;
                    }
                } else {//普通试题
                    //难度系数添加
                    difficultSum = difficultSum + handlerSingleQuestion.apply(questionId);
                }

                if (multimap.size() >= size) {//达到数量则会跳出
                    break;
                }
            } catch (Exception e) {
                logger.error("ex={}", e);
            }


        }
        List<Module> modules = new ArrayList<>();
        List<Integer> questions = new ArrayList<>();
        //遍历多值map,组装QuestionStrategy
        for (Integer moduleId : multimap.keySet()) {
            final QuestionPoint point = questionPointDubboService.findById(moduleId);
            final List<Integer> ids = multimap.get(moduleId);
            final Module module = Module.builder().category(point.getId())
                    .name(point.getName())
                    .qcount(ids.size()).build();
            modules.add(module);
            questions.addAll(ids);
        }
        //计算难度,保证一位小数点
        BigDecimal difficulty = new BigDecimal(6);
        if (questions.size() > 0) {
            difficulty = new BigDecimal(difficultSum).divide(new BigDecimal(questions.size()), 1, BigDecimal.ROUND_HALF_UP);
        }
        final QuestionStrategy questionStrategy = QuestionStrategy.builder().modules(modules)
                .questions(questions)
                .difficulty(difficulty.doubleValue()).build();

        return questionStrategy;
    }

    /**
     * 查询questionId相关的试题，及存在的复合题信息+子题信息
     *
     * @param questionIds
     * @return
     */
    private Map<Integer, Question> getQuestions(List<Integer> questionIds) {
        Map<Integer, Question> resultMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(questionIds)) {
            return resultMap;
        }
        List<Question> questions = questionDubboService.findBath(questionIds);
        if (CollectionUtils.isEmpty(questions)) {
            return resultMap;
        }
        List<Integer> parentIds = questions.stream().filter(i -> i instanceof GenericQuestion)
                .map(i -> ((GenericQuestion) i).getParent())
                .filter(i -> null != i && i > 0)
                .collect(Collectors.toList());
        resultMap.putAll(questions.stream().distinct().collect(Collectors.toMap(i -> i.getId(), i -> i)));
        if (CollectionUtils.isEmpty(parentIds)) {
            return resultMap;
        }
        List<Question> parents = questionDubboService.findBath(parentIds);
        for (Question parent : parents) {
            if (parent instanceof CompositeQuestion) {
                List<Question> childrens = parent.getChildrens();
                if (CollectionUtils.isNotEmpty(childrens)) {
                    resultMap.put(parent.getId(), parent);
                    resultMap.putAll(childrens.stream().distinct().collect(Collectors.toMap(i -> i.getId(), i -> i)));
                }
            }
        }
        return resultMap;
    }

    /**
     * 取特定知识点下错题次数最多的那几道试题
     *
     * @param userId
     * @param pointQuestionIdsMap
     * @param pointIds
     * @param size
     */
    private void fillErrorCountMaxQuestionIds(long userId, Map<Integer, List<Integer>> pointQuestionIdsMap, List<Integer> pointIds, int size) {
        int total = 0;
        if (MapUtils.isNotEmpty(pointQuestionIdsMap)) {
            int sum = pointQuestionIdsMap.entrySet().stream().map(Map.Entry::getValue).mapToInt(List::size).sum();
            total += sum;
        }
        if (total >= size) {       //取错题策略的前提是未做完试试题量已经处理完
            return;
        }
        List<QuestionUserMeta> questionUserMetas = questionUserMetaDao.findBathForErrorCount(userId, pointIds, size - total);
        for (QuestionUserMeta questionUserMeta : questionUserMetas) {
            List<Integer> ids = pointQuestionIdsMap.getOrDefault(questionUserMeta.getThirdPointId(), Lists.newArrayList());
            ids.add(questionUserMeta.getQuestionId());
            pointQuestionIdsMap.put(questionUserMeta.getThirdPointId(), ids);
        }
    }

    /**
     * 对每个知识点查询未做完的试题ID，和试题ID对应的兄弟试题
     *
     * @param userId
     * @param pointQuestionIdsMap
     * @param pointIds
     */
    private void fillUnfinishedQuestionIds(long userId, Map<Integer, List<Integer>> pointQuestionIdsMap, List<Integer> pointIds) {
        for (Integer pointId : pointIds) {
            List<String> questionIds = questionPointServiceV2.getQuestionIds(pointId);
            Set<String> tempIds = questionFinishServiceV2.filterQuestionIds(userId, pointId, questionIds.stream().collect(Collectors.toSet()));
            List<String> list = ListUtils.removeAll(questionIds, tempIds);
            List<Integer> tempList = pointQuestionIdsMap.getOrDefault(pointId, Lists.newArrayList());
            tempList.addAll(list.stream().map(Integer::parseInt).collect(Collectors.toList()));
            pointQuestionIdsMap.put(pointId, tempList);
        }
    }

    /**
     * 拿到所有抽题涉及的三级知识点
     *
     * @param pointId
     * @param subject
     * @return
     */
    private List<QuestionPoint> getAllPoints(Integer pointId, int subject) {
        List<QuestionPoint> result = Lists.newArrayList();
        QuestionPoint questionPoint = questionPointServiceV2.findById(pointId);
        if (null != questionPoint) {
            fillLeafNode2Result(result, questionPoint);  //result写入三级知识点
        }
        if (CollectionUtils.isEmpty(result)) {
            logger.info("getAllPoints error,pointId={}; change getPoints by subject,subject = {}", pointId, subject);
            List<com.huatu.ztk.commons.Module> subjectModules = moduleDubboService.findSubjectModules(subject);
            for (com.huatu.ztk.commons.Module subjectModule : subjectModules) {
                fillLeafNode2Result(result, questionPointServiceV2.findById(subjectModule.getId()));
            }
        }
        return result;
    }

    private void fillLeafNode2Result(List<QuestionPoint> result, QuestionPoint questionPoint) {
        if (null == questionPoint) {
            return;
        }
        if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_THREE) {
            result.add(questionPoint);
        } else if (questionPoint.getLevel() < QuestionPointLevel.LEVEL_THREE) {
            List<Integer> children = questionPoint.getChildren();
            for (Integer child : children) {
                fillLeafNode2Result(result, questionPointServiceV2.findById(child));
            }
        }
    }
}
