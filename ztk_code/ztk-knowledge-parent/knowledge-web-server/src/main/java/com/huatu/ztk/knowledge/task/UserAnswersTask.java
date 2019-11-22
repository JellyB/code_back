package com.huatu.ztk.knowledge.task;

import com.google.common.collect.Sets;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceUtil;
import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.dao.InitDao;
import com.huatu.ztk.knowledge.service.QuestionErrorService;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.UserAnswers;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionCorrectType;
import com.yxy.ssdb.client.SsdbConnection;
import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 做题答案处理
 * 错题,已做过题的保存,存入redis
 * Created by shaojieyue
 * Created time 2016-06-13 10:30
 */
public class UserAnswersTask implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(UserAnswersTask.class);

    @Autowired
    private QuestionDubboService questionDubboService;

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private SsdbPooledConnectionFactory ssdbPooledConnectionFactory;

    @Autowired
    private InitDao initDao;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private QuestionPersistenceUtil questionPersistenceUtil;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private QuestionErrorService questionErrorService;

    @Override
    public void onMessage(Message message) {
        String content = new String(message.getBody());
        //logger.info("receive message,data={}", content);
        final UserAnswers userAnswers;
        try {
            userAnswers = JsonUtil.toObject(content, UserAnswers.class);
        } catch (Exception e) {
            logger.error("proccess fail. message={}", message, e);
            return;
        }
        logger.info("knowledge UserAnswer taskInfo = {} ", userAnswers);

        try {
            proccessMessage(userAnswers);
        } catch (Exception e) {
            logger.info(">>>>>>>用户试题收集处理逻辑失败，{}", userAnswers);
        }
    }

    private void proccessMessage(UserAnswers userAnswers) {
        logger.info("knowledge UserAnswer taskInfo = {} ", userAnswers);
        long uid = userAnswers.getUid();
        AnswerCard answerCard = initDao.findById(userAnswers.getPracticeId());
        final String finishedSmartKey = RedisKnowledgeKeys.getFinishedSmartKey(uid, userAnswers.getCatgory());
        int subject = userAnswers.getSubject();
        //去掉无效的答案,只处理做对或做错的情况
        final Answer[] answers = userAnswers.getAnswers()
                .stream()
                .filter(answer -> answer.getCorrect() == QuestionCorrectType.RIGHT || answer.getCorrect() == QuestionCorrectType.WRONG)
                .toArray(Answer[]::new);
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final HashOperations hashOperations = redisTemplate.opsForHash();
        Set<Integer> threeLevelPointSet = Sets.newHashSet();
        Set<Integer> oneLevelPointSet = Sets.newHashSet();
        Set<Integer> twoLevelPointSet = Sets.newHashSet();

        //循环处理所有的答案列表
        for (Answer answer : answers) {
            final int questionId = answer.getQuestionId();
            //对应的试题
            Question question = questionDubboService.findById(questionId);
            //试题不存在,不进行处理
            if (question == null) {
                logger.error("not exist question,skip it,questionId={}", questionId);
                continue;
            }

            //不是普通试题不进行处理
            if (!(question instanceof GenericQuestion)) {
                logger.error("unknow question type,data={}", JsonUtil.toJson(question));
                continue;
            }

            final GenericQuestion genericQuestion = (GenericQuestion) question;

            //知识点数量错误的不进行处理
            if (CollectionUtils.isEmpty(genericQuestion.getPoints()) || genericQuestion.getPoints().size() != 3) {
                logger.error("wrong point size,question={}", JsonUtil.toJson(question));
                continue;
            }

            final String questionIdStr = String.valueOf(questionId);

            /**
             * //做过的题,这里只处理第三级列表，因为set超出压缩列表长度后，会极大的消耗存，
             * 不把把做过的题添加到1,2级列表就是为了减少set超出压缩列表的概率
             */
            String finishedSetKey = RedisKnowledgeKeys.getFinishedSetKey(uid, genericQuestion.getPoints().get(2));
            //做过的题加入ssdb zset
            SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
            try {
                final String finishedCountKey = RedisKnowledgeKeys.getFinishedCountKey(uid);
                //logger.info("用户完成试题信息：{}，key = {}",questionIdStr,finishedSetKey);
                connection.zset(finishedSetKey, questionIdStr, System.currentTimeMillis());

                /**
                 * 缓存需要持化的key 值信息
                 * add by lijun 2018-03-20
                 */
                questionPersistenceUtil.addFinishQuestionPersistence(finishedSetKey);

                //知识点完成知识个数
                final int finishCount = connection.zsize(finishedSetKey);
                //完成个数 这是
                connection.hset(finishedCountKey, genericQuestion.getPoints().get(2) + "", finishCount + "");
            } catch (Exception e) {
                logger.error("ex", e);
            } finally {
                ssdbPooledConnectionFactory.returnConnection(connection);
            }
            //按照知识点分组
            oneLevelPointSet.add(genericQuestion.getPoints().get(0));
            twoLevelPointSet.add(genericQuestion.getPoints().get(1));
            threeLevelPointSet.add(genericQuestion.getPoints().get(2));

            //对每个知识点都进行处理
            for (Integer point : genericQuestion.getPoints()) {
                //做错的
                String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, point);
                String wrongCursor = RedisKnowledgeKeys.getWrongCursor(uid, point);
                if (answer.getCorrect() == QuestionCorrectType.RIGHT) {
                    //做对后,需要把该试题从错题库删除
                    Long remove = zSetOperations.remove(errorSetKey, questionIdStr);
                    if (remove < 1) {
                        questionErrorService.deleteWrongCacheById(genericQuestion.getId(), uid);
                    }
                    zSetOperations.remove(wrongCursor, questionIdStr);
                } else {
                    //做错,添加到错题库,通过提交答案时间排序
                    zSetOperations.add(errorSetKey, questionIdStr, userAnswers.getSubmitTime());
                    zSetOperations.add(wrongCursor, questionIdStr, userAnswers.getSubmitTime());
                }

                /**
                 * 缓存需要持化的key 值信息
                 * add by lijun 2018-03-20
                 */
                questionPersistenceUtil.addWrongQuestionPersistence(errorSetKey);
            }

            //logger.info("uid={},答题卡是否为空={}，答题卡类型answerCard.getType()={}",uid,(answerCard!=null),answerCard.getType());
            //处理智能抽题重复问题
            if (answerCard != null && answerCard.getType() == AnswerCardType.SMART_PAPER) {

                Long size = zSetOperations.size(finishedSmartKey);
                if (size >= 2000) {
                    zSetOperations.removeRange(finishedSmartKey, 0, size - 2000);
                }
                boolean result = zSetOperations.add(finishedSmartKey, questionIdStr, System.currentTimeMillis());
                Set<String> set = zSetOperations.range(finishedSmartKey, 0, -1);
                //logger.info("finishedSmartKey={},questionIdStr={},插入前长度={},result={}，插入后长度={}",finishedSmartKey,questionIdStr,size,result,zSetOperations.size(finishedSmartKey));
                //logger.info("set={}",set);
            }
        }


        //处理知识点计数
        final String finishedPointKey = RedisKnowledgeKeys.getFinishedPointKey(uid);
        final SetOperations setOperations = redisTemplate.opsForSet();
        for (Integer pointId : threeLevelPointSet) {
            //批量添加到已做知识点
            final String pointIdStr = pointId + "";
            setOperations.add(finishedPointKey, pointIdStr);

            //对每个知识点做错误数量统计
            String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, pointId);
            final String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(uid);
            final Long errorSize = zSetOperations.size(errorSetKey);
            hashOperations.put(wrongCountKey, pointIdStr, String.valueOf(errorSize));
        }

        //处理2级知识点
        proccessQuestionCountSummary(uid, twoLevelPointSet);

        //处理1级知识点
        proccessQuestionCountSummary(uid, oneLevelPointSet);

        int times = 0;
        int right = 0;
        for (Answer answer : answers) {
            times = times + answer.getTime();
            if (answer.getCorrect() == QuestionCorrectType.RIGHT) {
                right++;
            }
        }

        int wrong = answers.length - right;
        Map<String, Object> data = new HashMap<>();
        data.put("uid", userAnswers.getUid());
        data.put("subject", userAnswers.getSubject());
        data.put("wsum", wrong);
        data.put("rsum", right);
        data.put("times", times);
        data.put("area", userAnswers.getArea());
        //发送消息到report统计
        rabbitTemplate.convertAndSend("submit_answers_summary_queue", data);
    }

    /**
     * 处理试题个数统计,该方法只适合处理1级,2级知识点
     *
     * @param uid
     * @param levelPoints
     */
    private void proccessQuestionCountSummary(long uid, Set<Integer> levelPoints) {
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final HashOperations hashOperations = redisTemplate.opsForHash();

        for (Integer pointId : levelPoints) {
            //批量添加到已做知识点
            final String pointIdStr = pointId + "";
            //对每个知识点做错误数量统计
            String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, pointId);
            final String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(uid);
            final Long errorSize = zSetOperations.size(errorSetKey);

            if (errorSize == null || errorSize == 0) {
                //知识点没有错题的情况下,则删除该收藏知识点,减少reids key的数量
                redisTemplate.delete(errorSetKey);
                //没有错题了则删除该知识点
                hashOperations.delete(wrongCountKey, pointIdStr);

            } else {
                hashOperations.put(wrongCountKey, pointIdStr, String.valueOf(errorSize));
            }

            final String finishedCountKey = RedisKnowledgeKeys.getFinishedCountKey(uid);
            final QuestionPoint questionPoint = questionPointDubboService.findById(pointId);
            final List<String> childPoints = questionPoint.getChildren().stream().map(child -> child + "").collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childPoints)) {//child为空不处理,此处主要是防止以前的数据
                continue;
            }
            final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
            try {
                final Map<String, String> resutMap = connection.hget(finishedCountKey.getBytes(), childPoints);
                //知识点完成知识个数
                final int finishCount = resutMap.values().stream().filter(count -> count != null).mapToInt(Integer::valueOf).sum();
                //完成个数 这是
                connection.hset(finishedCountKey, pointIdStr, finishCount + "");
            } catch (Exception e) {
                logger.error("ex", e);
            } finally {
                ssdbPooledConnectionFactory.returnConnection(connection);
            }
        }
    }
}
