package com.huatu.ztk.knowledge.service.v1.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.service.v1.QuestionPointServiceV1;
import com.huatu.ztk.knowledge.servicePandora.KnowledgeService;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.huatu.ztk.knowledge.constant.SubjectConstant.YHZP_SUBJECTS;

@Service
public class QuestionPointServiceImplV1 implements QuestionPointServiceV1 {

    private static final Logger logger = LoggerFactory.getLogger(QuestionPointServiceImplV1.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ModuleDubboService moduleDubboService;
    @Autowired
    private QuestionPointDubboService questionPointDubboService;
    @Autowired
    @Qualifier("knowledgeServiceImpl")
    private KnowledgeService knowledgeService;

    //知识点的缓存
    Cache<Integer, List<QuestionPoint>> POINTS_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(10, TimeUnit.DAYS)
                    .build();
    //知识点的缓存
    Cache<Integer, List<QuestionPoint>> POINTS_FILTER_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(20, TimeUnit.MINUTES)
                    .build();

    @Override
    public QuestionPoint findById(int pointId) {
        return knowledgeService.findById(pointId);
    }

    @Override
    public int count(int pointId) {
        try {
            final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            final String qcount = hashOperations.get(RedisKnowledgeKeys.getPointSummaryKey(), pointId + "");
            if (StringUtils.isNotBlank(qcount)) {
                return Integer.parseInt(qcount);
            }
        } catch (Exception e) {
            logger.error("getCount error :pointId={}", pointId);
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Map<Integer, Integer> countAll() {
        try {
            final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            Map<String, String> entries = hashOperations.entries(RedisKnowledgeKeys.getPointSummaryKey());
            if (MapUtils.isNotEmpty(entries)) {
                return entries.entrySet().stream().collect(Collectors.toMap(i -> Integer.parseInt(i.getKey()), i -> Integer.parseInt(i.getValue())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }


    @Override
    public List<String> getQuestionIds(int pointId) {
        final SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        final String pointQuestionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(pointId);
        return setOperations.members(pointQuestionIdsKey).stream().collect(Collectors.toList());
    }


    @Override
    public List<QuestionPoint> getAllQuestionPoints(int subject) {
        DebugCacheUtil.showCacheContent(POINTS_FILTER_CACHE, "POINTS_FILTER_CACHE");
        //此处做本地缓存
        List<QuestionPoint> questionPointsResult = POINTS_FILTER_CACHE.getIfPresent(subject);
        if (questionPointsResult != null) {
            return questionPointsResult;
        }
        List<QuestionPoint> questionPoints = getQuestionPoints(subject);

        if (CollectionUtils.isNotEmpty(questionPoints) && !YHZP_SUBJECTS.contains(subject)) {
            Map<Integer, Integer> hashMap = countAll();

            List<QuestionPoint> result = questionPoints.stream()
                    .filter(questionPoint ->
                            //过滤掉没有试题的知识点
                            hashMap.containsKey(questionPoint.getId())
                    ).filter(questionPoint -> hashMap.getOrDefault(questionPoint.getId(), 0) > 0)
                    .map(questionPoint -> {
                        questionPoint.setQnumber(hashMap.getOrDefault(questionPoint.getId(), 0));
                        return questionPoint;
                    })
                    .collect(Collectors.toList());

            POINTS_FILTER_CACHE.put(subject, result);
            return result;
        }
        return questionPoints;
    }

    @Override
    public List<QuestionPoint> getQuestionPointsByParent(int subject, int parent) {
        List<QuestionPoint> questionPoints = getAllQuestionPoints(subject);
        //非空判断
        if (CollectionUtils.isEmpty(questionPoints)) {
            return Lists.newArrayList();
        }
        List<QuestionPoint> pointList = questionPoints.stream()
                .filter(questionPoint -> questionPoint.getParent() == parent)
                .collect(Collectors.toList());
        return pointList;
    }

    @Override
    public List<QuestionPointTree> handlerCount(List<QuestionPointTree> treeList, Map<Integer, Integer> finishCountMap, Map<Integer, Integer> wrongCountMap, Map<Integer, Integer> pointQuestionMap, Map<Integer, Long> unfinishedPointMap, long userId) {
        return combine(treeList, finishCountMap, wrongCountMap, pointQuestionMap, unfinishedPointMap);
    }

    /**
     * 将统计信息合并到知识点树里面
     *
     * @param treeList
     * @param finishCountMap
     * @param wrongCountMap
     * @param pointQuestionMap
     * @return
     */
    private List<QuestionPointTree> combine(List<QuestionPointTree> treeList, Map<Integer, Integer> finishCountMap, Map<Integer, Integer> wrongCountMap,
                                            Map<Integer, Integer> pointQuestionMap, Map<Integer, Long> unfinishedPointIds) {
        for (QuestionPointTree questionPointTree : treeList) {
            final int pointId = questionPointTree.getId();
            final List<QuestionPointTree> children = questionPointTree.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {//含有子节点,则递归调用
                combine(children, finishCountMap, wrongCountMap, pointQuestionMap, unfinishedPointIds);
            }
            final Integer qnum = pointQuestionMap.getOrDefault(pointId, 0);
            //该知识点对应的试题个数
            //TODO: 如果实体库有试题信息，缓存中试题数量为 0,此处会把试题数量填充成0
            questionPointTree.setQnum(qnum);
            //取最小值，防止出现完成次数比总题数多的情况
            final Integer finishCount = Math.min(finishCountMap.getOrDefault(pointId, 0), qnum);
            Integer wrongCount = wrongCountMap.getOrDefault(pointId, 0);
            wrongCount = Math.min(wrongCount, finishCount);
            questionPointTree.setRnum(Math.max(finishCount - wrongCount, 0));//防止负数
            questionPointTree.setWnum(wrongCount);
            if (finishCount > 0) {
                questionPointTree.setAccuracy(100 * questionPointTree.getRnum() / finishCount);
            }

            //当前知识点是未完成的
            if (unfinishedPointIds.containsKey(pointId)) {
                questionPointTree.setUnfinishedPracticeId(unfinishedPointIds.get(pointId));
            }
        }
        //update by lijun 防止返回 试题数量 0 的节点信息
        //在缓存未更新的情况下，原始代码可能会出现问题，需要重启服务解决
        return treeList.stream().filter(questionPointTree -> questionPointTree.getQnum() > 0).collect(Collectors.toList());
    }

    List<QuestionPoint> getQuestionPoints(int subject) {
        DebugCacheUtil.showCacheContent(POINTS_CACHE, "POINTS_CACHE");
        List<QuestionPoint> questionPoints = POINTS_CACHE.getIfPresent(subject);
        if (questionPoints == null) {
            List<QuestionPoint> tmp = new ArrayList<>();
            final List<Module> modules = moduleDubboService.findSubjectModules(subject);
            for (Module module : modules) {
                final QuestionPoint questionPoint = questionPointDubboService.findById(module.getId());
                tmp.add(questionPoint);

                //查询二级节点
                final List<QuestionPoint> children = questionPointDubboService.findChildren(questionPoint.getId());
                tmp.addAll(children);

                //查询3级节点
                for (QuestionPoint child : children) {
                    final List<QuestionPoint> threeChildren = questionPointDubboService.findChildren(child.getId());
                    tmp.addAll(threeChildren);
                }
            }
//            logger.info("queryData => ,{}", tmp);
            if (tmp != null && tmp.size() > 0) {
                POINTS_CACHE.put(subject, tmp);
            }else {
            	logger.info("set POINTS_CACHE data => ,{}", tmp);
            }
            return tmp;
        }
        return questionPoints;
    }
}
