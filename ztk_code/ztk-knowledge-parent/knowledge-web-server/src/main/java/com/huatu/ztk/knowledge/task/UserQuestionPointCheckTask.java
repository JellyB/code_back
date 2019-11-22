package com.huatu.ztk.knowledge.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.cacheTask.service.QuestionPersistenceService;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceUtil;
import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.service.PoxyUtilService;
import com.huatu.ztk.knowledge.service.QuestionCollectService;
import com.huatu.ztk.knowledge.service.QuestionPointService;
import com.huatu.ztk.knowledge.service.userCheck.CleanAllDataService;
import com.huatu.ztk.knowledge.service.userCheck.SendQuestionToRedisService;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.transaction.NotSupportedException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: UserQuestionPointCheckTask
 * @description: 检查用户错题和收藏数据，有问题的进行回复
 * @date 2019-09-1721:14
 */
public class UserQuestionPointCheckTask implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(UserQuestionPointCheckTask.class);

    @Autowired
    PoxyUtilService poxyUtilService;
    @Autowired
    QuestionCollectService questionCollectService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SendQuestionToRedisService sendQuestionToRedisService;
    @Autowired
    private QuestionPointService questionPointService;
    @Autowired
    private QuestionDubboService questionDubboService;
    @Autowired
    private QuestionPersistenceUtil questionPersistenceUtil;


    public static final String CHECK_COLLECT = "check_collect";
    public static final String CHECK_WRONG = "check_wrong";


    @Override
    public void onMessage(Message message) {
        String content = new String(message.getBody());
        logger.info("receive message={}", content);
        Map data = new HashMap();
        try {
            data = JsonUtil.toMap(content);
            Long userId = MapUtils.getLong(data, "userId");
            String type = MapUtils.getString(data, "type");
            Integer subject = MapUtils.getInteger(data, "subject");
            if (CHECK_COLLECT.equals(type)) {
                boolean b = checkCollect(userId, subject);
                if (b) {
                    questionCollectService.resetCollection(userId, subject);
                }
            } else if (CHECK_WRONG.equals(type)) {
                Set<Integer> questionIds = Sets.newHashSet();
                Map<Integer, Integer> oldMap = poxyUtilService.getQuestionErrorService(1).countAll(userId);
                boolean b = checkError(userId, subject,questionIds,oldMap);
                if (b) {
//                    sendQuestionToRedisService.writeUserActionDataToRedis(userId);
                    //通过所有redis错题数据，以及mysql中查到的持久化数据组合，汇总完整的错题数据
                    System.out.println("questionIds.size() = " + questionIds.size());
                    sendQuestionToRedisService.mergeUserActionDataAndRedis(userId,questionIds);
                    System.out.println("questionIds.size() = " + questionIds.size());
                    //查询所有试题信息
                    List<Question> questions = findBath(questionIds);
                    System.out.println("questionIds.size() = " + questions.size());
                    //重新生成错题数据，并覆盖redis中的数据
                    Set<String> keys = sendQuestionToRedisService.restRedis(questions, oldMap, userId);
                    System.out.println("keys = " + keys);
                    //异步将redis数据持久化到数据库
                    keys.forEach(questionPersistenceUtil::addWrongQuestionPersistence);

//                    cleanAllDataService.cleanError(userId);
                }
            } else {
                throw new NotSupportedException();
            }
        } catch (Exception e) {
            logger.error("handler message error,{}", content);
            e.printStackTrace();
        }
    }

    private List<Question> findBath(Set<Integer> questionIds) {
        ArrayList<Integer> ids = new ArrayList<>(questionIds);
        List<Question> questions = Lists.newArrayList();
        int i = 0;
        int size = 100;
        while (true){
            List<Integer> collect = ids.stream().skip(i * size).limit(size).collect(Collectors.toList());
            if(collect.isEmpty()){
                break;
            }
            List<Question> bath = questionDubboService.findBathWithFilter(collect);
            if(CollectionUtils.isNotEmpty(bath)){
                questions.addAll(bath.stream().filter(q->q.getStatus()!= QuestionStatus.DELETED).distinct().collect(Collectors.toList()));
            }
            i++;
        }
        return questions;
    }

    private boolean checkError(Long userId, Integer subject, final Set<Integer> ids, Map<Integer, Integer> map) {

        boolean checkFlag = false;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            Integer pointId = entry.getKey();
            Integer num = entry.getValue();
            if(num.intValue() == 0){        //如果缓存HASH为空，则直接跳过处理逻辑
                continue;
            }
            Set<Integer> questionIds = poxyUtilService.getQuestionErrorService(1).getQuestionIds(pointId, userId);
            ids.addAll(questionIds);        //1.整合所有缓存中的数据
            //2.判断是否需要重新整理数据（HASH和试题数量是否对应的上，对应不上返回true,之后循环不再做判断）
            if(!checkFlag){   //用户redis HASH有值，且还未确定是否更新知识点数据
                if(CollectionUtils.isEmpty(questionIds) ||          //缓存中无试题数据或者试题数据与HASH不适配
                        questionIds.size() != num.intValue()){
                    checkFlag = true;
                    System.out.println("checkFlag = " + checkFlag);
                }
            }
        }
        System.out.println("result checkFlag = " + checkFlag);
        return checkFlag;
    }

    /**
     * 检查用户收藏数据是否需要维护
     *
     * @param userId
     * @param subject
     * @return true 表示需要维护 false表示无需维护
     */
    private boolean checkCollect(Long userId, Integer subject) {
        String collectCountKey = RedisKnowledgeKeys.getCollectCountKey(userId);
        List<QuestionPointTree> collectPointTrees = questionPointService.findCountPointTrees(collectCountKey, subject, false);
        if (CollectionUtils.isEmpty(collectPointTrees)) {
            return false;
        }
        Predicate<QuestionPointTree> checkLoss = (questionPointTree -> {
            int pointTreeId = questionPointTree.getId();
            final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(userId, pointTreeId);
            Set range = redisTemplate.opsForZSet().range(collectSetKey, 0, -1);
            return CollectionUtils.isEmpty(range) && questionPointTree.getQnum() > 0;
        });
        List<QuestionPointTree> tempList = Lists.newArrayList();
        tempList.addAll(collectPointTrees);
        while (true) {
            if (CollectionUtils.isEmpty(tempList)) {
                break;
            }
            boolean b = tempList.stream().anyMatch(checkLoss);
            if (b) {
                return true;        //如果试题的知识点ID,则直接返回true表示需要做同步
            }
            List<QuestionPointTree> collect = tempList
                    .stream()
                    .filter(i -> CollectionUtils.isNotEmpty(i.getChildren()))
                    .flatMap(i -> i.getChildren().stream())
                    .collect(Collectors.toList());
            tempList.clear();
            tempList.addAll(collect);
        }
        return false;
    }
}
