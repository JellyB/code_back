package com.huatu.tiku.teacher.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.util.QuestionPointPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InitQuestionPointTreeTask extends TaskService {


    @Autowired
    private CommonQuestionServiceV1 commonQuestionServiceV1;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private KnowledgeService knowledgeService;

    private static List<Knowledge> knowledgeList = Lists.newArrayList();

    /**
     * 一二三层级知识点信息（id+name+level+qnumber+parent）
     */
    private static List<QuestionPoint> questionPoints = Lists.newArrayList();
    /**
     * 三级知识点ID映射到下属的试题ID集合
     */
    private static Map<Integer, Set<Integer>> pointQuestionMap = Maps.newHashMap();
    private static String NANO_TIME_LOCK = "";
    private static final long CacheLockExpireTime = 10 ;      //分布式锁生命周期（分钟）--半小时
    /**
     * 分布式锁key（redis实现）
     */
    private static final String INIT_QUESTION_POINT_TREE_LOCK_KEY = "init_question_point_tree_lock_key";

    @Scheduled(fixedRate = 60000 * 30)
    public void initQuestionPointTree() {
        task();
    }

    /**
     * 定时任务实现逻辑
     */
    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        if (!CollectionUtils.isEmpty(questionPoints)) {       //任务开始前保证共有队列中无数据，否则证明有任务在同时知识点数据
            return;
        }
        final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        Long expire = redisTemplate.getExpire(pointSummaryKey, TimeUnit.MINUTES);
        if(expire >= 60){       //缓存超过60分钟不做更新，直接返回
            return;
        }
        /**
         * 遍历试题，并针对试题做抽题池数据整理
         */
        commonQuestionServiceV1.findAndHandlerQuestion(handlerQuestionPointPool, -1);
        writeResult2Redis();
        long end = System.currentTimeMillis();
        log.info("InitQuestionPointTreeTask 耗时：{}",(end-currentTimeMillis));
    }

    @Override
    protected long getExpireTime() {
        return CacheLockExpireTime;
    }

    /**
     * 将最终的数据写入redis缓存
     */
    private void writeResult2Redis() {
        setPointSummary2Redis();
        setPointQuestionIds2Redis();
        //TODO 知识树数据接口是否需要生成
    }

    private void setPointQuestionIds2Redis() {
        SetOperations setOperations = redisTemplate.opsForSet();
        for (Map.Entry<Integer, Set<Integer>> entry : pointQuestionMap.entrySet()) {
            Integer key = entry.getKey();
            String pointQuestionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(key);
            String[] questionIds = entry.getValue().stream().map(String::valueOf).collect(Collectors.toList()).toArray(new String[]{});
            redisTemplate.delete(pointQuestionIdsKey);
            setOperations.add(pointQuestionIdsKey,questionIds);
        }
    }

    /**
     * 知识点下试题数量写入redis
     */
    private void setPointSummary2Redis() {
        final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        redisTemplate.delete(pointSummaryKey);
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map<String, String> resultMap = questionPoints.stream().collect(Collectors.toMap(i -> i.getId() + "", i -> i.getQnumber() + ""));
        hashOperations.putAll(pointSummaryKey,resultMap);
        redisTemplate.expire(pointSummaryKey,1, TimeUnit.DAYS);
    }

    /**
     * 批量整理试题数据到本地公用变量
     */
    private Consumer<List<Question>> handlerQuestionPointPool = (questions -> {
        long l = System.nanoTime();
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }
        /**
         * 筛选可以入抽题池的试题，然后统计生成新的知识树题量和知识点下所有试题ID集合的数据
         */
        questions.stream()
                .filter(QuestionPointPoolUtil::isPoolFlag)
                .forEach(i -> {
                    if (i instanceof GenericQuestion) {
                        GenericQuestion genericQuestion = (GenericQuestion) i;
                        //校验试题的知识点是否有效
                        boolean flag = QuestionPointUtil.checkPoints(genericQuestion, getKnowledgeList());
                        if (flag) {
                            updatePointQuestionSet(genericQuestion);
                            updateQuestionCount(genericQuestion);
                        } else {
                            //TODO 目前只做跳过操作，之后可以试着发送同步试题消息
                            log.error("question checkPoints error,id={}", genericQuestion.getId());
                        }

                    }
                });
        System.out.println((System.nanoTime()-l));
    });

    public List<Knowledge> getKnowledgeList() {
        if (CollectionUtils.isEmpty(knowledgeList)) {
            List<Knowledge> knowledges = knowledgeService.selectAll();
            knowledgeList.addAll(knowledges.stream().filter(i -> i.getStatus() == StatusEnum.NORMAL.getValue())
                    .collect(Collectors.toList()));
        }
        return knowledgeList;
    }

    /**
     * 修改本地缓存中知识点下试题数量
     *
     * @param question
     */
    private void updateQuestionCount(GenericQuestion question) {
        List<Integer> points = question.getPoints();
        List<String> pointsName = question.getPointsName();
        for (int i = 0; i < points.size(); i++) {
            Integer point = points.get(i);
            Optional<QuestionPoint> any = questionPoints.stream().filter(k -> k.getId() == point.intValue()).findAny();
            if (any.isPresent()) {
                QuestionPoint temp = any.get();
                temp.setQnumber(temp.getQnumber() + 1);
            } else {
                QuestionPoint build = QuestionPoint.builder().id(point)
                        .name(pointsName.get(i))
                        .parent(i == 0 ? 0 : points.get(i - 1))
                        .level(i + 1)
                        .qnumber(1)
                        .build();
                questionPoints.add(build);
            }
        }
    }

    /**
     * 更新知识点下的试题Id集合
     *
     * @param question
     */
    private void updatePointQuestionSet(GenericQuestion question) {
        List<Integer> points = question.getPoints();
        Integer point = points.get(points.size() - 1);
        Set<Integer> ids = pointQuestionMap.getOrDefault(point, Sets.newHashSet());
        ids.add(question.getId());
        pointQuestionMap.put(point, ids);
    }

    @Override
    public void unlock() {
        log.info("解锁---------InitQuestionPointTreeTask");
        super.unlock();
        questionPoints.clear();
        pointQuestionMap.clear();
        knowledgeList.clear();
    }

    @Override
    public String getCacheKey() {
        return INIT_QUESTION_POINT_TREE_LOCK_KEY;
    }

    @Override
    public String getNanoTime() {
        return NANO_TIME_LOCK;
    }

    @Override
    public void setNanoTime(String nanoTime) {
        NANO_TIME_LOCK = nanoTime;
    }


}
