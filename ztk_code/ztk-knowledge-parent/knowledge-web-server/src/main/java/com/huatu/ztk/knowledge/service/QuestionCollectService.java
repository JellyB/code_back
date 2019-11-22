package com.huatu.ztk.knowledge.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.cacheTask.dao.QuestionPersistenceDao;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceDataGetUtil;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceKeyUtil;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceUtil;
import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.common.analysis.model.EventEntity;
import com.huatu.ztk.knowledge.common.analysis.model.EventType;
import com.huatu.ztk.knowledge.task.UserQuestionPointCheckTask;
import com.huatu.ztk.knowledge.util.PageUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 试题收藏service层
 * Created by ismyway on 16/5/18.
 */
@Service
public class QuestionCollectService {

    public static final Logger logger = LoggerFactory.getLogger(QuestionCollectService.class);

    /**
     * 收藏列表试题的最大数量
     */
    private static final int MAX_LIST_COUNT = 50;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private QuestionPointService questionPointService;

    @Autowired
    private ModuleDubboService moduleDubboService;

    @Autowired
    private QuestionPersistenceUtil questionPersistenceUtil;

    @Autowired
    private QuestionPersistenceDao questionPersistenceDao;

    @Autowired
    private QuestionPersistenceService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 收藏题目
     *
     * @param questionId 试题id
     * @param userId     用户id
     * @param subject    所属科目
     */
    public void collect(int questionId, long userId, int subject) throws BizException {
        logger.info("collect question. questionId = {}, userId = {}, subject = {}", questionId, userId, subject);
        long stime = System.currentTimeMillis();
        final Question question = questionDubboService.findById(questionId);
        logger.info("collect questionDubboService time={}", System.currentTimeMillis() - stime);

        if (question == null) {
            logger.error("collect question not exist. questionId={}", questionId);
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        //不是普通试题也不允许收藏
        if (!(question instanceof GenericQuestion)) {
            logger.error("collect question type not GenericQuestion,so can not collect.");
            throw new BizException(CommonErrors.NOT_SUPPORT_COLLECT);
//            return;
        }
        GenericQuestion genericQuestion = (GenericQuestion) question;
        List<Integer> points = genericQuestion.getPoints();
        if (CollectionUtils.isEmpty(points) || points.size() < 3) {
            logger.error("collect question knowledge not 3 level,so can not collect.");
            throw new BizException(CommonErrors.NOT_SUPPORT_COLLECT_KNOWLEDGE);
        }
        /*
          数据上报
         */
        upData(question);
        //时间戳最为score
        final long timeMillis = System.currentTimeMillis();
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        //遍历试题所属知识点,保存知识点对应的收藏,试题采用zset,保证收藏顺序
        for (Integer point : genericQuestion.getPoints()) {
            final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, point);
            zSetOperations.add(collectSetKey, String.valueOf(question.getId()), timeMillis);
            /**
             * 缓存收藏数据 2018-04-16
             */
            questionPersistenceUtil.addCollectQuestionPersistence(collectSetKey);

        }
        logger.info("add collection time={}", System.currentTimeMillis() - timeMillis);

        long stime1 = System.currentTimeMillis();
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        //遍历并保存各个收藏知识点个数
        for (Integer point : genericQuestion.getPoints()) {
            final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, point);
            final String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userId);
            //当前知识点收藏的试题数量
            final Long total = zSetOperations.size(collectSetKey);
            //保存知识点->试题数量关系
            hashOperations.put(collectCountKey, String.valueOf(point), String.valueOf(total.intValue()));

        }
        logger.info("compute collection num time={}", System.currentTimeMillis() - stime1);
    }

    /**
     * 取消收藏
     *
     * @param questionId 试题id
     * @param userId     用户id
     * @param subject    科目id
     */
    public void cancel(int questionId, long userId, int subject) throws BizException {
        logger.info("cancel collect question. questionId = {}, userId = {}, subject = {}", questionId, userId, subject);
        final Question question = questionDubboService.findById(questionId);
        if (question == null) {
            logger.error("collect cancel question not exist. questionId={}", questionId);
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        GenericQuestion genericQuestion = (GenericQuestion) question;

         /*
          数据上报
         */
        upData(question);


//        //遍历试题所属知识点,取消收藏
//        for (Integer point : genericQuestion.getPoints()) {
//            if(!isChangeForQuestionPoint(userId,point,String.valueOf(questionId))){
//                cancelCollectionCache(point,String.valueOf(questionId),userId);
//            }else{
//                cancelCollectionCacheById(userId,String.valueOf(questionId));
//                break;
//            }
//        }
        cancelCollectionCacheById(userId, String.valueOf(questionId));
    }


    /**
     * 异步删除逻辑
     *
     * @param userId
     * @param questionIdStr
     */
    @Async
    public void cancelCollectionCacheById(Long userId, String questionIdStr) {
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        final String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userId);
        Map<String, String> entries = hashOperations.entries(collectCountKey);
        entries.entrySet().stream().map(Map.Entry::getKey).map(Integer::parseInt)
                .filter(i -> !isChangeForQuestionPoint(userId, i, questionIdStr))
                .forEach(i -> cancelCollectionCache(i, questionIdStr, userId));
    }

    /**
     * 判断试题是否切换了知识点
     *
     * @param uid
     * @param point
     * @param questionIdStr
     * @return false 试题 在
     */
    private boolean isChangeForQuestionPoint(long uid, Integer point, String questionIdStr) {
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(uid, point);
        Double score = zSetOperations.score(collectSetKey, questionIdStr);
        if (null != score && score.intValue() > 0) {
            return false;
        }
        return true;
    }

    /**
     * 在收藏夹的某个知识点下删除某道试题
     *
     * @param point
     * @param questionIdStr
     * @param userId
     */
    private void cancelCollectionCache(Integer point, String questionIdStr, long userId) {
        //时间戳最为score
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        final String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userId);
        final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, point);
        //取消收藏
        zSetOperations.remove(collectSetKey, questionIdStr);
        //本节点下收藏题目数量
        final Long size = zSetOperations.size(collectSetKey);
        if (size == null || size == 0) {
            //没有收藏的情况下,则删除该收藏知识点,减少reids key的数量
            redisTemplate.delete(collectSetKey);
            //该知识点下收藏的数量为0的话,则删除该知识点
            hashOperations.delete(collectCountKey, point.toString());
        } else {
            //更新节点下收藏数量
            hashOperations.put(collectCountKey, point.toString(), size.toString());
        }
        /**
         * 缓存收藏数据 2018-04-16
         */
        questionPersistenceUtil.addCollectQuestionPersistence(collectSetKey);
    }

    /**
     * 上报三级知识点
     *
     * @param question
     */
    private void upData(Question question) {
        Optional.ofNullable(question.getPointList())
                .map(x -> x.get(0).getPointsName())
                .ifPresent(list -> {
                    EventEntity.putProperties(EventType.CollectTest.test_first_cate, Optional.ofNullable(list.get(0)).orElse("不存在该级知识点"));
                    EventEntity.putProperties(EventType.CollectTest.test_second_cate, Optional.ofNullable(list.get(1)).orElse("不存在该级知识点"));
                    EventEntity.putProperties(EventType.CollectTest.test_third_cate, Optional.ofNullable(list.get(2)).orElse("不存在该级知识点"));
                });
    }

    /**
     * 查看收藏列表,由于试题不多,所以一次性把一个知识点的所有试题返回
     *
     * @param pointId 知识点
     * @param userId  用户id
     * @return
     */
    public PageBean<Integer> findByPoint(int pointId, long userId) {
        final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, pointId);

        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        //所有错题
        final Set<String> set = zSetOperations.reverseRange(collectSetKey, 0, MAX_LIST_COUNT - 1);

        List<Integer> questions = new ArrayList<>(set.size());
        for (Object o : set) {//遍历转换为数字类型
            Integer id = Ints.tryParse(String.valueOf(o));
            if (id != null) {
                questions.add(id);
            }
        }

        PageBean<Integer> pageBean = new PageBean<Integer>(questions, 0, questions.size());
        return pageBean;
    }

    /**
     * 查询组装收藏知识点树
     *
     * @param userId  用户id
     * @param subject 科目
     * @return
     */
    public List<QuestionPointTree> findCollectPointTrees(long userId, int subject) {
        final String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userId);
        checkCollectPointRedis(userId,subject);
        return questionPointService.findCountPointTrees(collectCountKey, subject, false);
    }

    public void checkCollectPointRedis(long userId, int subject){
        String collectKey = RedisKnowledgeKeys.getCollectCountKey(userId) + "_check";      //试题检查错题本的分布式锁
        Boolean checkFlag = redisTemplate.opsForValue().setIfAbsent(collectKey, userId + "");
        if (checkFlag) {
            logger.info("用户需要例行检查收藏题目数据,userId={}", userId);
            redisTemplate.expire(collectKey, 7, TimeUnit.DAYS);
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("userId",userId);
            map.put("type", UserQuestionPointCheckTask.CHECK_COLLECT);
            map.put("subject", subject);
            rabbitTemplate.convertAndSend("","check_user_question_point",map);
        }
    }

    /**
     * 查询指定的试题列表里面,哪些试题被收藏过
     *
     * @param questionIds 试题id列表
     * @param userId      用户id
     * @param subject     用户设置的subject
     * @return
     */
    public Collection<String> findCollectQuestions(List<String> questionIds, long userId, int subject) {
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        final String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userId);

        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        final Set<String> keys = Sets.newHashSet();

        //查询科目下的顶级知识点
        List<String> moudleIds = moduleDubboService.findSubjectModules(subject).stream()
                .map(i -> i.getId() + "")
                .collect(Collectors.toList());

        final List<String> values = hashOperations.multiGet(collectCountKey, moudleIds);
        for (int i = 0; i < values.size(); i++) {
            if (StringUtils.isNoneBlank(values.get(i))) {
                keys.add(moudleIds.get(i));
            }
        }
        if (CollectionUtils.isEmpty(keys)) {//没有收藏记录则直接返回
            return Lists.newArrayList();
        }

        //收藏集合
        Set<String> collects = Sets.newHashSet();
        //遍历公务员
        for (String pointId : keys) {
            final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, Ints.tryParse(pointId));
            //获取模块下所有的收藏列表
            final Set<String> range = zSetOperations.range(collectSetKey, 0, -1);
            //添加到已收藏集合
            collects.addAll(range);
        }
        //返回两个集合的交集,交集就是用户收藏过的列表
        return CollectionUtils.intersection(questionIds, collects);
    }

    public PageUtil findByPointPage(int pointId, long userId, int page, int pageSize) {

        final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, pointId);

        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        //v1 查询所有收藏题  ---》 v2分页查询收藏题
        int start = (page - 1) * pageSize;
        int end = pageSize * page - 1;
        final Set<String> set = zSetOperations.reverseRange(collectSetKey, start, end);

        Long size = zSetOperations.size(collectSetKey);
        PageUtil.PageUtilBuilder<Object> builder = PageUtil.builder()
                .next((size > end) ? 1 : 0)
                .total(size);
        List<Integer> questions = new ArrayList<>(set.size());
        for (Object o : set) {//遍历转换为数字类型
            Integer id = Ints.tryParse(String.valueOf(o));
            if (id != null) {
                questions.add(id);
            }
        }
        builder.result(questions);

        return builder.build();
    }

    public void resetCollection(long userId, int subject) {
//        StringBuilder sb = new StringBuilder("resetCollection_").append(userId).append("_").append(subject);
//        File file = new File("/data/logs/knowledge-web-server/"+sb.toString()+".txt");
//        sb.append("\n");
        sendMysqlData2Redis(userId);        //补偿数据
        final String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userId);
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Set<String> keys = hashOperations.keys(collectCountKey);        //涉及收藏的知识点
        List<Module> subjectModules = moduleDubboService.findSubjectModules(subject);
        if (CollectionUtils.isEmpty(subjectModules) || CollectionUtils.isEmpty(keys)) {
            return;
        }
        List<QuestionPoint> questionPoints = questionPointService.getQuestionPoints(subject);
        if (CollectionUtils.isEmpty(questionPoints)) {
            return;
        }
        List<Integer> questionIds = Lists.newArrayList();
        for (QuestionPoint point : questionPoints) {
            String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, point.getId());
            Set<String> range = redisTemplate.opsForZSet().range(collectSetKey, 0, -1);
            if (CollectionUtils.isNotEmpty(range)) {
//                sb.append(point.getName()).append("(原数据):").append(range.stream().collect(Collectors.joining(","))).append("\n");
                questionIds.addAll(range
                        .stream()
                        .filter(NumberUtils::isDigits)
                        .map(Integer::parseInt).collect(Collectors.toList()));
            }
        }
        /**
         * 重置每个知识点下的试题Id集合
         */
        Map<Integer, List<Integer>> pointQuestionMap = countPointQuestionMap(questionIds
                .stream()
                .distinct()     //去重
                .collect(Collectors.toList()));
        /**
         * 处理该知识点下试题数据
         */
        Consumer<QuestionPoint> restQuestionIds = (questionPoint -> {
            int id = questionPoint.getId();
            String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, id);
            redisTemplate.delete(collectSetKey);
            List<Integer> list = pointQuestionMap.get(id);
            long currentTimeMillis = System.currentTimeMillis();
            if (CollectionUtils.isNotEmpty(list)) {
//                sb.append(questionPoint.getName()).append("(现有数据):").append(list.stream().map(String::valueOf).collect(Collectors.joining(","))).append("\n");
                for (Integer questionId : list) {
                    ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
                    zSetOperations.add(collectSetKey, questionId + "", currentTimeMillis);
                    hashOperations.put(collectCountKey, id + "", list.size() + "");
                }
            } else {
                hashOperations.delete(collectCountKey, id + "");
            }
        });
        /**
         * 遍历处理所有知识点的试题
         */
        questionPoints.forEach(restQuestionIds::accept);
//        try {
//            FileUtils.writeStringToFile(file,sb.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("resetCollection = " + sb.toString());
    }

    /**
     * 补偿redis收藏过期数据
     *
     * @param userId
     */
    private void sendMysqlData2Redis(long userId) {
        //1.处理搜藏数据
        QuestionPersistenceEnum.TableName collectTable = QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_COLLECT;
        List<QuestionPersistenceModel> collectQuestionPersistenceModels = questionPersistenceDao.findByUserId(String.valueOf(userId),
                collectTable);
        collectQuestionPersistenceModels.forEach(model -> {
            String pointId = model.getQuestionPointId();
            String collectSetKey = RedisKnowledgeKeys.getCollectSetKey(userId, Integer.valueOf(pointId));

            List<String> collectCacheData = service.getCollectCacheData(collectSetKey);
            service.judgeIsOutOfExpiredTime(userId, collectCacheData, pointId, collectSetKey, collectTable, model);
        });
    }


    /**
     * 用户收藏数据重新规划知识点
     *
     * @param questionIds
     * @return
     */
    private Map<Integer, List<Integer>> countPointQuestionMap(List<Integer> questionIds) {
        Map<Integer, List<Integer>> result = Maps.newHashMap();
        Consumer<List<Question>> consumer = (questions -> {
            if (CollectionUtils.isEmpty(questions)) {
                return;
            }
            questions.stream().filter(i -> i instanceof GenericQuestion)
                    .filter(i -> i.getStatus() != QuestionStatus.DELETED)
                    .forEach(i -> {
                        GenericQuestion genericQuestion = (GenericQuestion) i;
                        List<Integer> points = genericQuestion.getPoints();
                        BiConsumer<Integer, Integer> biConsumer = ((questionId, pointId) -> {
                            List<Integer> list = result.getOrDefault(pointId, Lists.newArrayList());
                            list.add(questionId);
                            result.put(pointId, list);
                        });
                        points.forEach(point -> biConsumer.accept(genericQuestion.getId(), point));
                    });
        });
        int index = 0;
        int size = 100;
        while (true) {
            int end = (questionIds.size() >= (index + size)) ? (index + size) : questionIds.size();
            if (index == end) {
                break;
            }
            List<Integer> ids = questionIds.subList(index, end);
            List<Question> questions = questionDubboService.findBath(ids);
            consumer.accept(questions);
            index = end;
        }
        return result;
    }
}
