package com.huatu.ztk.knowledge.dataClean;

import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.cacheTask.dao.QuestionPersistenceDao;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceEnum;
import com.huatu.ztk.knowledge.cacheTask.model.QuestionPersistenceModel;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceKeyUtil;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.daoPandora.KnowledgeMapper;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/3/12
 * @描述 处理数据行为数据（错题,收藏,做题记录）丢失
 * @描述 2018年3月和2018年6月进行过数据刷新和调整, 2018年9月题库重构后, 当时只是刷新了一个月的50万活跃用户的数据到redis中
 * 所以会出现用户2018年6月之后，未登陆app，导致用户行为数据丢失的情况
 * @描述 此方法会从mysql中将用户的做题记录, 重新刷新到redis集合中, 只是暂时使用。
 * 后面hqp会整合一个完整的方法
 */
public class SendQuestionToRedisService extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(CleanAllDataService.class);

    @Autowired
    private QuestionPersistenceService service;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private QuestionPersistenceDao questionPersistenceDao;

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    //需要写入的用户ID
    private static final Long userId = 235029918L;

    //单元测试配置备份
     /* System.setProperty("webapp.dir", "/Users/workspace/2.workcode/knowledge/knowledge-web-server/src/main/webapp");
        System.setProperty("server_name", "paper-web-server");
        System.setProperty("server_ip", "localhost");
        System.setProperty("disconf.user_define_download_dir", "/Users/lizhenjuan/03workCode/disconf");
        System.setProperty("disconf.env", "qa");*/


    /**
     * 将mysql的用户行为数据刷新到redis集合中
     */
    @Test
    public void writeUserActionDataToRedis() {
        com.google.common.base.Stopwatch stopwatch = com.google.common.base.Stopwatch.createStarted();


        //1.处理搜藏数据
        QuestionPersistenceEnum.TableName collectTable = QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_COLLECT;
        List<QuestionPersistenceModel> collectQuestionPersistenceModels = questionPersistenceDao.findByUserId(userId.toString(),
                collectTable);
        collectQuestionPersistenceModels.forEach(model -> {
            String pointId = model.getQuestionPointId();
            String collectSetKey = RedisKnowledgeKeys.getCollectSetKey(userId, Integer.valueOf(pointId));

            List<String> collectCacheData = service.getCollectCacheData(collectSetKey);
            judgeIsOutOfExpiredTime(userId, collectCacheData, pointId, collectSetKey, collectTable, model);
        });
        System.out.print("stop" + String.valueOf(stopwatch.stop()));

        //2.处理错误数据
        QuestionPersistenceEnum.TableName wrongTable = QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG;
        final List<QuestionPersistenceModel> wrongQuestionPersistenceModels = questionPersistenceDao.findByUserId(userId.toString(),
                wrongTable);
        wrongQuestionPersistenceModels.forEach(model -> {
            String pointId = model.getQuestionPointId();
            String wrongKey = RedisKnowledgeKeys.getWrongSetKey(userId, Integer.valueOf(pointId));
            List<String> wrongCacheData = service.getWrongCacheData(wrongKey);
            judgeIsOutOfExpiredTime(userId, wrongCacheData, pointId, wrongKey, wrongTable, model);
        });
        System.out.print("错题刷新到redis,完毕");

        //3.已经完成的试题,存放在ssdb中(不需处理)

    }


    /**
     * 处理redis中是否有此key(redis中是否有此key,如果没有,直接将mysql写入redis)
     * 更新时间以缓存时间90天为界限,只刷新90天之前的数据
     */
    public void judgeIsOutOfExpiredTime(Long userId, List<String> list, String pointId, String key, QuestionPersistenceEnum.TableName tableName,
                                        QuestionPersistenceModel model) {
        //更新时间是否在10号之前
        if (CollectionUtils.isEmpty(list)) {
            this.writeDataToRedis(key, userId, pointId, tableName, false);
        } else {
            if (null != model.getUpdateTime()) {
                Date startTime = model.getUpdateTime();
                Instant instant = startTime.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDate startLocalDate = instant.atZone(zoneId).toLocalDate();
                long until = startLocalDate.until(LocalDate.now(), ChronoUnit.DAYS);
                if (until > 90) {
                    this.writeDataToRedis(key, userId, pointId, tableName, true);
                }
            }
        }
    }

    /**
     * @param key       reids key
     * @param userId    用户ID
     * @param pointId   知识点ID
     * @param tableName 表名
     * @param flag      是否刷新此key数据到redis
     */
    private void writeDataToRedis(String key, Long userId, String pointId, QuestionPersistenceEnum.TableName tableName, Boolean flag) {

        //补偿策略 - 回写数据
        List<String> questionList = service.getQuestionIdByUserIdAndPointId(userId.toString(), pointId, tableName);
        List<String> questionListResult = new ArrayList<>(questionList);
        if (null == questionList || questionList.size() == 0) {
            return;
        }
        if (flag) {
            List<String> questionsFromRedis = service.getCollectCacheData(key);
            questionListResult.addAll(questionsFromRedis);
        }

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        long time = System.currentTimeMillis() / 1000 - 1000;
        for (int index = 0; index < questionList.size(); index++) {
            zSetOperations.add(key, questionList.get(index), time - index * 1000);
        }
        redisTemplate.expire(key, QuestionPersistenceKeyUtil.TTL, TimeUnit.DAYS);
    }


    /**
     * 刷数据之前,校验用户数据数量是否真的出现丢失情况
     */
    @Test
    public void testUseInfo() {

        //tableName可根据自己需要更改
        QuestionPersistenceEnum.TableName tableName = QuestionPersistenceEnum.TableName.QUESTION_USER_CACHE_WRONG;

        final List<QuestionPersistenceModel> wrongQuestionPersistenceModels = questionPersistenceDao.findByUserId(userId.toString(), tableName);
        String pointIds = wrongQuestionPersistenceModels.stream()
                .map(model -> model.getQuestionPointId())
                .collect(Collectors.joining(","));
        List<HashMap<String, Object>> knowledgeBySubjectIds = knowledgeMapper.getKnowledgeBySubjectId(pointIds, 1);

        //获得本科目知识点
        List<String> pointList = knowledgeBySubjectIds.stream()
                .filter(knowledge -> null != knowledge.get("id"))
                .map(knowledge -> knowledge.get("id").toString())
                .collect(Collectors.toList());

        List<QuestionPersistenceModel> list = new ArrayList<>();
        for (QuestionPersistenceModel questionPersistenceModel : wrongQuestionPersistenceModels) {
            if (pointList.contains(questionPersistenceModel.getQuestionPointId())) {
                QuestionPersistenceModel newModel = new QuestionPersistenceModel();
                newModel.setQuestionPointId(questionPersistenceModel.getQuestionPointId());
                newModel.setQuestionId(questionPersistenceModel.getQuestionId());
                list.add(newModel);
            }
        }
        //校验试题数目是否跟app显示一致
        String collect = list.stream()
                .map(i -> i.getQuestionId()).collect(Collectors.joining());
        List<String> result = Arrays.stream(collect.split(",")).collect(Collectors.toList());
        List<String> newResult = result.stream().distinct().collect(Collectors.toList());
        System.out.print("去重前" + result.size() + "去重后" + newResult.size());
    }


}
