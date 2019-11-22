package com.huatu.ztk.knowledge.task;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionGeneticBean;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.dao.InitDao;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionMode;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by shaojieyue
 * Created time 2016-07-14 13:03
 */
public class QuestionUpdateTask implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(QuestionUpdateTask.class);

    @Autowired
    private InitDao initDao;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Resource(name = "redisObjectTemplate")
    private RedisTemplate<String, QuestionGeneticBean> redisTemplate2;

    @Override
    public void onMessage(Message message) {
        try {
            String content = new String(message.getBody());
            logger.info("receive message,data={}", content);
            final Map map = JsonUtil.toMap(content);
            int qid = MapUtils.getIntValue(map, "qid", 0);
            if (qid < 1) {
                return;
            }
            final Question question = initDao.findQuestionById(qid);
            if (question == null) {
                logger.info("qid={} not exist question.", qid);
                return;
            }
            update2QuestionIds(question);
            updateIdsYearModule(question);
        } catch (Exception e) {
            logger.error("消息处理失败：,message = {}", message);
            e.printStackTrace();
        }
    }

    /**
     * 将试题添加到对应的年份，模块列表中
     *
     * @param question
     */
    private void updateIdsYearModule(Question question) {
        //不处理主观题和复合题及非公务员行测题

        List<Integer> subjectIds = Lists.newArrayList(1, 4, 100100175, 200100063, 100100263);
         /*  产品调增，允许智能刷题科目
        1，公务员-行测 1
        2，事业单位 职测 4
        3，招警考试 100100175
        4，军队文职 200100063
        5，国家电网 100100263 */
        logger.info("试题ID是:{},试题科目是:{}", question.getId(), question.getSubject());
        if (question instanceof GenericSubjectiveQuestion || question instanceof CompositeSubjectiveQuestion || question instanceof CompositeQuestion || subjectIds.contains(question.getSubject()) == false) {
            logger.info("非允许智能刷题科目");
            return;
        }
        logger.info("可正常进入刷题池");
        GenericQuestion genericQuestion = (GenericQuestion) question;
        QuestionGeneticBean questionGeneticBean = QuestionGeneticBean.builder()
                .id(genericQuestion.getId())
                .difficulty(genericQuestion.getDifficult())
                .moduleId(genericQuestion.getPoints().get(0))
                .year(genericQuestion.getYear())
                .build();
        final String yearModuleKey = RedisKnowledgeKeys.getYearModuleQuestionsV3(questionGeneticBean.getYear(), questionGeneticBean.getModuleId(), genericQuestion.getSubject());
        //年份小于2008，复合题的子题，模拟题不做处理
        if (!CollectionUtils.isNotEmpty(genericQuestion.getPoints()) || genericQuestion.getYear() < 2008 || genericQuestion.getParent() > 0 || genericQuestion.getMode() != QuestionMode.QUESTION_TRUE ||
                (question.getType() != QuestionType.WRONG_RIGHT && question.getType() != QuestionType.SINGLE_CHOICE && question.getType() != QuestionType.MULTIPLE_CHOICE)) {
            redisTemplate2.opsForSet().remove(yearModuleKey, questionGeneticBean);
            return;
        }

        if (question.getStatus() == QuestionStatus.DELETED || question.getStatus() == QuestionStatus.AUDIT_REJECT || question.getStatus() == QuestionStatus.CREATED) {//删除则从推荐里面去掉
            redisTemplate2.opsForSet().remove(yearModuleKey, questionGeneticBean);
        } else {
            redisTemplate2.opsForSet().add(yearModuleKey, questionGeneticBean);
        }
    }


    /**
     * 将试题id 添加到知识点对应的id列表
     *
     * @param question
     */
    private void update2QuestionIds(Question question) {
        logger.info("question={}", question);
        //不处理主观题
        if (question instanceof GenericSubjectiveQuestion || question instanceof CompositeSubjectiveQuestion) {
            return;
        }
        final String questionIdKey = RedisKnowledgeKeys.getQuestionIdKey(question.getId());
        //把试题核心数据写入redis
        if (question instanceof GenericQuestion) {
            GenericQuestion genericQuestion = (GenericQuestion) question;
            //把核心信息存入redis
            Map<String, String> data = new HashMap();
            data.put("area", genericQuestion.getArea() + "");
            data.put("difficult", genericQuestion.getDifficult() + "");
            data.put("year", genericQuestion.getYear() + "");
            data.put("parent", genericQuestion.getParent() + "");
            data.put("moduleId", genericQuestion.getPoints().get(0) + "");//模块id
            redisTemplate.opsForHash().putAll(questionIdKey, data);
        } else if (question instanceof CompositeQuestion) {
            CompositeQuestion compositeQuestion = (CompositeQuestion) question;
            if (CollectionUtils.isNotEmpty(compositeQuestion.getQuestions())) {//复合题里面没有子题
                final Question subQuestion = initDao.findById(compositeQuestion.getQuestions().get(0));
                //把核心信息存入redis
                Map<String, String> data = new HashMap();
                //复合题只存储id列表
                data.put("subIds", StringUtils.join(compositeQuestion.getQuestions(), ","));
                //取复合题第一个试题的模块id来作为复合题的模块id,目的是保证子题在抽题的时候的连续性(属于同一个模块)
                if (subQuestion == null) {
                    logger.info("复合题的子题{}未同步过来", compositeQuestion.getQuestions().get(0));
                    return;
                }
                List<Integer> points = ((GenericQuestion) subQuestion).getPoints();

                if (CollectionUtils.isEmpty(points)) {
                    return;
                }

                data.put("moduleId", points.get(0) + "");
                redisTemplate.opsForHash().putAll(questionIdKey, data);
            }
        }

        //复合题不用加入到组卷,由其子题加入组卷
        if (question instanceof CompositeQuestion) {
            CompositeQuestion compositeQuestion = (CompositeQuestion) question;
            return;
        }

        GenericQuestion genericQuestion = (GenericQuestion) question;
        //2008之前的题不参与推送，不在区域内的也不参与推荐
        if (question.getSubject() == SubjectType.GWY_XINGCE) {
            if (question.getYear() < 2008) {
                deleteFromRedis(genericQuestion);
                return;
            }

            //只处理真题
            if (question.getMode() != QuestionMode.QUESTION_TRUE) {
                deleteFromRedis(genericQuestion);
                return;
            }

            //只处理单选，复合，对错题
            if (question.getType() != QuestionType.WRONG_RIGHT &&
                    question.getType() != QuestionType.SINGLE_CHOICE &&
                    question.getType() != QuestionType.MULTIPLE_CHOICE) {
                deleteFromRedis(genericQuestion);
                return;
            }

        }

        if (question.getStatus() != QuestionStatus.AUDIT_SUCCESS) {//删除则从推荐里面去掉
            deleteFromRedis(genericQuestion);
        } else {
            addFromRedis(genericQuestion);
        }
    }

    /**
     * 把试题从添加到组卷列表
     *
     * @param genericQuestion
     */
    private void addFromRedis(GenericQuestion genericQuestion) {
        //三级知识点
        final String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(genericQuestion.getPoints().get(2));
        final SetOperations setOperations = redisTemplate.opsForSet();
        logger.info("add question to point, {}:{}", pointQuesionIdsKey, genericQuestion.getId());
        setOperations.add(pointQuesionIdsKey, genericQuestion.getId() + "");
        //更新试题个数
        updatePointSummary(genericQuestion.getPoints());
    }

    /**
     * 从组卷列表里面删除
     *
     * @param genericQuestion
     */
    private void deleteFromRedis(GenericQuestion genericQuestion) {
        //三级知识点
        final String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(genericQuestion.getPoints().get(2));
        final SetOperations setOperations = redisTemplate.opsForSet();
        logger.info("deleteFromRedis question to point, {}:{}", pointQuesionIdsKey, genericQuestion.getId());
        setOperations.remove(pointQuesionIdsKey, genericQuestion.getId() + "");
        //更新试题个数
        updatePointSummary(genericQuestion.getPoints());
    }

    /**
     * 更新知识点个数
     *
     * @param points
     */
    private void updatePointSummary(List<Integer> points) {
        final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        //三级知识点
        final Long size = redisTemplate.opsForSet().size(RedisKnowledgeKeys.getPointQuesionIds(points.get(2)));
        //设置1级节点个数
        redisTemplate.opsForHash().put(pointSummaryKey, points.get(2) + "", size + "");

        //处理二级节点
        final List<QuestionPoint> children = questionPointDubboService.findChildren(points.get(1));//二级知识点
        redisTemplate.opsForHash().put(pointSummaryKey, points.get(1) + "", getPointQuetsionCount(children) + "");
        logger.info("parent={},chilren={}", points.get(1), children.stream().map(QuestionPoint::getId).collect(Collectors.toList()));
        final List<QuestionPoint> threeLevelPoints = new ArrayList<>();
        final List<QuestionPoint> children1 = questionPointDubboService.findChildren(points.get(0));//二级知识点
        for (QuestionPoint questionPoint : children1) {//遍历二级知识点
            final List<QuestionPoint> subs = questionPointDubboService.findChildren(questionPoint.getId());//三级知识点
            threeLevelPoints.addAll(subs);
        }
        //设置1级节点个数
        redisTemplate.opsForHash().put(pointSummaryKey, points.get(0) + "", getPointQuetsionCount(threeLevelPoints) + "");
        logger.info("parent={},chilren={}", points.get(0), threeLevelPoints.stream().map(QuestionPoint::getId).collect(Collectors.toList()));
    }

    /**
     * 遍历知识点,获取知识点试题个数
     *
     * @param children
     * @return
     */
    private int getPointQuetsionCount(List<QuestionPoint> children) {
        int count = 0;
        for (QuestionPoint questionPoint : children) {
            final Long size = redisTemplate.opsForSet().size(RedisKnowledgeKeys.getPointQuesionIds(questionPoint.getId()));
            if (size != null) {
                count = size.intValue() + count;
            }
        }
        return count;
    }


}
