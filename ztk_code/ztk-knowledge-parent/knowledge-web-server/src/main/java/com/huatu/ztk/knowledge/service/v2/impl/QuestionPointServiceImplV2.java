package com.huatu.ztk.knowledge.service.v2.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.bean.BaseEntity;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.common.PointStatus;
import com.huatu.ztk.knowledge.common.QuestionPointLevel;
import com.huatu.ztk.knowledge.service.PoxyUtilService;
import com.huatu.ztk.knowledge.service.v1.QuestionPointServiceV1;
import com.huatu.ztk.knowledge.servicePandora.KnowledgeService;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import com.huatu.ztk.knowledge.util.RestPandoraUtil;
import com.huatu.ztk.paper.common.ResponseMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sun.security.ssl.Debug;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuestionPointServiceImplV2 implements QuestionPointServiceV1 {

    @Autowired
    @Qualifier("knowledgeServiceImplV2")
    private KnowledgeService knowledgeServiceV2;

    @Autowired
    private PoxyUtilService poxyUtilService;

    /**
     * 知识点下试题数量统计
     */
    private static Cache<Integer, Integer> POINT_QUESTION_COUNT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    /**
     * 知识点下试题ID集合
     */
    private static Cache<Integer, List<Integer>> POINT_QUESTION_LIST_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    /**
     * 每个知识点下父子结点和题量的缓存整合
     */
    private static Cache<Integer, QuestionPoint> QUESTION_POINT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(12, TimeUnit.HOURS)
            .build();

    /**
     * 每个科目下的知识点和题量的缓存整合
     */
    private static Cache<Integer, List<QuestionPoint>> SUBJECT_POINT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(12, TimeUnit.HOURS)
            .build();
    private static AtomicLong expireTime = new AtomicLong(-1L);
    private static Lock lock = new ReentrantLock();
    private static final int preMinutes = 5;    //提前5分钟刷新数据

    /**
     * rest请求获取知识点下试题数量
     *
     * @return
     */
    public Map<Integer, Integer> getPointQuestionCount() {
        ResponseEntity<ResponseMsg> sysPointQuestion = RestPandoraUtil.getSysPointQuestion(RestPandoraUtil.POINT_QUESTION_COUNT);
        LinkedHashMap<String, String> apply = RestPandoraUtil.getData.apply(sysPointQuestion);
        if (null == apply) {
            return Maps.newHashMap();
        }
        try {
            Map<Integer, Integer> collect = apply.entrySet().stream().collect(Collectors.toMap(i -> Integer.parseInt(i.getKey()), i -> Integer.parseInt(i.getValue())));
            return collect;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }

    /**
     * 远程rest获取知识点下绑定的试题
     *
     * @return
     */
    public Map<Integer, List<Integer>> getPointQuestionIds() {
        ResponseEntity<ResponseMsg> sysPointQuestion = RestPandoraUtil.getSysPointQuestion(RestPandoraUtil.POINT_QUESTION_LIST);
        LinkedHashMap<String, String> apply = RestPandoraUtil.getData.apply(sysPointQuestion);
        Map<Integer, List<Integer>> result = Maps.newHashMap();
        if (null == apply) {
            return result;
        }
        for (Map.Entry<String, String> entry : apply.entrySet()) {
            try {
                List<Integer> ids = Arrays.stream(entry.getValue().split(",")).map(Integer::parseInt).collect(Collectors.toList());
                result.put(Integer.parseInt(entry.getKey()), ids);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 查询知识点信息
     *
     * @param knowledgeId
     * @return
     */
    @Override
    public QuestionPoint findById(int knowledgeId) {
        DebugCacheUtil.showCacheContent(QUESTION_POINT_CACHE, "QUESTION_POINT_CACHE");
        if (knowledgeId <= 0) {
            return null;
        }
        QuestionPoint me = QUESTION_POINT_CACHE.getIfPresent(knowledgeId);
        if (null != me) {
            setQuestionNumber(me);
            return me;
        }
        Function<Integer, Knowledge> getMe = (id -> knowledgeServiceV2.selectByPrimaryKey(new Long(id)));
        Function<Knowledge, List<Knowledge>> getChildren = (parent -> {
            if (null == parent || parent.getLevel() >= 3) {
                return Lists.newArrayList();
            }
            Example example = new Example(Knowledge.class);
            example.and().andEqualTo("parentId", parent.getId()).andEqualTo("level", parent.getLevel() + 1);
            return knowledgeServiceV2.selectByExample(example);
        });
        Knowledge knowledge = getMe.apply(knowledgeId);
        if (null == knowledge) {
            return null;
        }
        List<Knowledge> children = getChildren.apply(knowledge);
        QuestionPoint build = QuestionPoint.builder().id(knowledge.getId().intValue())
                .level(knowledge.getLevel() - 1)
                .parent(knowledge.getParentId().intValue())
                .name(knowledge.getName())
                .children(children.stream().map(BaseEntity::getId).map(Long::intValue).collect(Collectors.toList()))
                .status(PointStatus.AUDIT_SUCCESS)
                .build();
        setQuestionNumber(build);
        QUESTION_POINT_CACHE.put(knowledgeId, build);
        return build;
    }

    @Override
    public int count(int pointId) {
        DebugCacheUtil.showCacheContent(POINT_QUESTION_COUNT_CACHE, "POINT_QUESTION_COUNT_CACHE");
        DebugCacheUtil.doInt(() -> {
            log.info("guava缓存过期时间为：{}", new Date(expireTime.get()));
        });
        listener();
        Integer number = POINT_QUESTION_COUNT_CACHE.getIfPresent(pointId);
        if (null != number && number > 0) {
            return number;
        }

        return 0;
    }

    public void listener() {
        if (POINT_QUESTION_COUNT_CACHE.size() == 0 || POINT_QUESTION_LIST_CACHE.size() == 0) {
            log.error("缓存数据丢失，强制补偿数据！！！");
            expireTime.set(-1L);
        }
        long currentTimeMillis = System.currentTimeMillis();
        boolean updateFlag = expireTime.get() < currentTimeMillis;
        if (updateFlag) {
            initQuestionPointCache();
        }
    }

    @Override
    public Map<Integer, Integer> countAll() {
        DebugCacheUtil.showCacheContent(POINT_QUESTION_COUNT_CACHE, "POINT_QUESTION_COUNT_CACHE");
        listener();
        ConcurrentMap<Integer, Integer> concurrentMap = POINT_QUESTION_COUNT_CACHE.asMap();
        return concurrentMap;
    }

    @Override
    public List<String> getQuestionIds(int pointId) {
        DebugCacheUtil.showCacheContent(POINT_QUESTION_LIST_CACHE, "POINT_QUESTION_LIST_CACHE");
        listener();
        List<Integer> ids = POINT_QUESTION_LIST_CACHE.getIfPresent(pointId);
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return ids.stream().map(String::valueOf).collect(Collectors.toList());
    }

    @Override
    public List<QuestionPoint> getAllQuestionPoints(int subject) {
        DebugCacheUtil.showCacheContent(SUBJECT_POINT_CACHE, "SUBJECT_POINT_CACHE");
        List<QuestionPoint> list = SUBJECT_POINT_CACHE.getIfPresent(subject);
        if (CollectionUtils.isNotEmpty(list)) {
            return list;
        }
        List<Module> module = knowledgeServiceV2.findModule(subject);
        Example example = new Example(Knowledge.class);
        //三级及以上的知识点
        example.and().andLessThanOrEqualTo("level", QuestionPointLevel.LEVEL_THREE + 1);
        List<Knowledge> knowledges = knowledgeServiceV2.selectByExample(example);
        List<QuestionPoint> resultList = Lists.newArrayList();
        convertAndFilterQuestionPoint(resultList, knowledges, module.stream().map(Module::getId).map(Long::new).collect(Collectors.toList()));
        SUBJECT_POINT_CACHE.put(subject, resultList);
        return resultList;
    }

    private void convertAndFilterQuestionPoint(List<QuestionPoint> resultList, List<Knowledge> knowledges, List<Long> moduleIds) {
        knowledges.sort(Comparator.comparing(Knowledge::getSortNum).thenComparing(BaseEntity::getId));
        Function<Knowledge, QuestionPoint> convert = (knowledge -> {
            QuestionPoint build = QuestionPoint.builder().id(knowledge.getId().intValue())
                    .name(knowledge.getName())
                    .parent(knowledge.getParentId().intValue())
                    .level(knowledge.getLevel() - 1)
                    .status(PointStatus.AUDIT_SUCCESS)
                    .build();
            setQuestionNumber(build);
            return build;
        });
        if (CollectionUtils.isEmpty(moduleIds)) {
            return;
        }
        List<QuestionPoint> questionPoints = knowledges.stream()
                .filter(i -> moduleIds.contains(i.getId()))
                .map(convert::apply)
//                .filter(i -> i.getQnumber() > 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(questionPoints)) {
            return;
        }
        resultList.addAll(questionPoints);
        convertAndFilterQuestionPoint(resultList, knowledges,
                knowledges
                        .stream()
                        .filter(i -> moduleIds.contains(i.getParentId()))     //所有子结点继续填充
                        .map(BaseEntity::getId).collect(Collectors.toList()));
    }

    @Override
    public List<QuestionPoint> getQuestionPointsByParent(int subject, int parent) {
        List<QuestionPoint> allQuestionPoints = getAllQuestionPoints(subject);
        if (CollectionUtils.isNotEmpty(allQuestionPoints)) {
            return allQuestionPoints.stream().filter(i -> i.getParent() == parent).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * @param treeList
     * @param finishCountMap     专项训练已完成数量（缺整体已完成数量）
     * @param wrongCountMap      错题数量
     * @param pointQuestionMap
     * @param unfinishedPointMap
     * @param userId
     * @return
     */
    @Override
    public List<QuestionPointTree> handlerCount(List<QuestionPointTree> treeList, Map<Integer, Integer> finishCountMap, Map<Integer, Integer> wrongCountMap, Map<Integer, Integer> pointQuestionMap, Map<Integer, Long> unfinishedPointMap, long userId) {
        Map<Integer, Integer> allFinishCountMap = (-1 == userId) ? Maps.newHashMap() : poxyUtilService.getQuestionFinishService(1).countAll(userId);
        return combine(treeList, finishCountMap, wrongCountMap, pointQuestionMap, unfinishedPointMap, allFinishCountMap);
    }

    /**
     * 将统计信息合并到知识点树里面
     *
     * @param treeList
     * @param finishCountMap
     * @param wrongCountMap
     * @param pointQuestionMap
     * @param allFinishCountMap
     * @return
     */
    private List<QuestionPointTree> combine(List<QuestionPointTree> treeList, Map<Integer, Integer> finishCountMap, Map<Integer, Integer> wrongCountMap,
                                            Map<Integer, Integer> pointQuestionMap, Map<Integer, Long> unfinishedPointIds, Map<Integer, Integer> allFinishCountMap) {
        for (QuestionPointTree questionPointTree : treeList) {
            final int pointId = questionPointTree.getId();
            final List<QuestionPointTree> children = questionPointTree.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {//含有子节点,则递归调用
                List<QuestionPointTree> combine = combine(children, finishCountMap, wrongCountMap, pointQuestionMap, unfinishedPointIds, allFinishCountMap);
                questionPointTree.setChildren(combine);
            }
            /**
             * 总题量
             */
            final Integer qnum = pointQuestionMap.getOrDefault(pointId, 0);
            questionPointTree.setQnum(qnum);
            /**
             * 正确率计算
             */
            Integer allFinishCount = allFinishCountMap.getOrDefault(pointId, 0);
            Integer wrongCount = wrongCountMap.getOrDefault(pointId, 0);
            int accuracy = 0;
            wrongCount = Math.min(allFinishCount, wrongCount);
            if (allFinishCount > 0) {
                accuracy = 100 * (allFinishCount - wrongCount) / allFinishCount;
            }
            questionPointTree.setAccuracy(accuracy);
            //取最小值，防止出现完成次数比总题数多的情况
            final Integer finishCount = Math.min(finishCountMap.getOrDefault(pointId, 0), qnum);
            questionPointTree.setRnum(finishCount * accuracy / 100);
            questionPointTree.setWnum(finishCount - questionPointTree.getRnum());
            //当前知识点是未完成的
            if (unfinishedPointIds.containsKey(pointId)) {
                questionPointTree.setUnfinishedPracticeId(unfinishedPointIds.get(pointId));
            }
        }
        //update by lijun 防止返回 试题数量 0 的节点信息
        //在缓存未更新的情况下，原始代码可能会出现问题，需要重启服务解决
        return treeList.stream().filter(questionPointTree -> questionPointTree.getQnum() > 0).collect(Collectors.toList());
    }

    /**
     * 补充知识点下试题数量
     *
     * @param build
     */
    private void setQuestionNumber(QuestionPoint build) {
        int count = count(build.getId());
        if (count > 0) {
            build.setQnumber(count);
        }
    }

    /**
     * 初始化用户数据
     */
    private void initQuestionPointCache() {

        try {
            Runnable runnable = () -> {
                boolean b = lock.tryLock();
                try {
                    if (!b) {
                        return;
                    }
                    log.info("获得锁，开始更新guava缓存");
                    long currentTimeMillis = System.currentTimeMillis();
                    boolean updateFlag = expireTime.get() > currentTimeMillis;
                    if (updateFlag) {
                        return;
                    }
                    Map<Integer, Integer> pointQuestionCount = getPointQuestionCount();
                    Map<Integer, List<Integer>> pointQuestionIds = getPointQuestionIds();
                    if (MapUtils.isEmpty(pointQuestionIds) || MapUtils.isEmpty(pointQuestionCount)) {
                        log.error("查询缓存数据为空，更新guava失败！！！");
                        return;
                    }
                    Integer times = pointQuestionCount.getOrDefault(-1, new Long(TimeUnit.HOURS.toMinutes(1)).intValue());
                    POINT_QUESTION_COUNT_CACHE.putAll(pointQuestionCount);
                    POINT_QUESTION_LIST_CACHE.putAll(pointQuestionIds);
                    //缓存时间最少为5分钟
                    expireTime.set(currentTimeMillis + TimeUnit.MINUTES.toMillis(Math.max(times - preMinutes, preMinutes)));
                    log.info("更新guava缓存，更新缓存周期为：{}", new Date(expireTime.get()));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (b) {
                        log.info("更新guava线程结束，释放缓存");
                        lock.unlock();
                    }
                }
            };
            if (POINT_QUESTION_COUNT_CACHE.size() == 0) {
                new Thread(runnable).run();
            } else {
                new Thread(runnable).start();
            }
        } catch (Exception e) {
            log.error("更新guava缓存失败");
            e.printStackTrace();
        }
    }


}
