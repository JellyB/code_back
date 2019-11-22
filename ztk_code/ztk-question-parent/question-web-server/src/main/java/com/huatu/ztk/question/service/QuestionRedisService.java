//package com.huatu.ztk.question.service;
//
//import com.huatu.ztk.commons.SubjectType;
//import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
//import com.huatu.ztk.knowledge.bean.QuestionGeneticBean;
//import com.huatu.ztk.knowledge.bean.QuestionPoint;
//import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
//import com.huatu.ztk.question.bean.*;
//import com.huatu.ztk.question.common.QuestionMode;
//import com.huatu.ztk.question.common.QuestionStatus;
//import com.huatu.ztk.question.common.QuestionType;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SetOperations;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by lenovo on 2017/10/13.
// */
//@Service
//public class QuestionRedisService {
//    private final static Logger logger = LoggerFactory.getLogger(QuestionRedisService.class);
//    @Autowired
//    private QuestionDubboServiceImpl questionDubboService;
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//    @Autowired
//    private QuestionPointDubboService questionPointDubboService;
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//    @Resource(name = "redisObjectTemplate")
//    private RedisTemplate<String, QuestionGeneticBean> redisTemplate2;
//
//    /**
//     * 根据试题的状态修改试题在redis中的存储情况
//     * 返回true表示redis会记录试题的统计信息，否则不统计
//     *
//     * @param question
//     */
//    public void updateQuestionInRedis(Question question) {
//        update2QuestionIds(question);
//        updateIdsYearModule(question);
//    }
//
//    /**
//     * 将试题添加到对应的年份，模块列表中
//     *
//     * @param question
//     */
//    private void updateIdsYearModule(Question question) {
//        //不处理主观题和复合题及非公务员行测题
//        if (question instanceof GenericSubjectiveQuestion || question instanceof CompositeSubjectiveQuestion || question instanceof CompositeQuestion || question.getSubject() != 1) {
//            return;
//        }
//
//        GenericQuestion genericQuestion = (GenericQuestion) question;
//        //年份小于2008，复合题的子题，模拟题不做处理
//        if (!CollectionUtils.isNotEmpty(genericQuestion.getPoints()) || genericQuestion.getYear() < 2008 || genericQuestion.getParent() > 0 || genericQuestion.getMode() != QuestionMode.QUESTION_TRUE ||
//                (question.getType() != QuestionType.WRONG_RIGHT && question.getType() != QuestionType.SINGLE_CHOICE && question.getType() != QuestionType.MULTIPLE_CHOICE)) {
//            return;
//        }
//
//
//        QuestionGeneticBean questionGeneticBean = QuestionGeneticBean.builder()
//                .id(genericQuestion.getId())
//                .difficulty(genericQuestion.getDifficult())
//                .moduleId(genericQuestion.getPoints().get(0))
//                .year(genericQuestion.getYear())
//                .build();
//        final String yearModuleKey = RedisKnowledgeKeys.getYearModuleQuestions(questionGeneticBean.getYear(), questionGeneticBean.getModuleId(), genericQuestion.getSubject());
//
//        if (question.getStatus() == QuestionStatus.DELETED || question.getStatus() == QuestionStatus.AUDIT_REJECT) {//删除则从推荐里面去掉
//            redisTemplate2.opsForSet().remove(yearModuleKey, questionGeneticBean);
//        } else {
//            redisTemplate2.opsForSet().add(yearModuleKey, questionGeneticBean);
//        }
//    }
//
//
//    /**
//     * 将试题id 添加到知识点对应的id列表
//     *
//     * @param question
//     */
//    private void update2QuestionIds(Question question) {
//        //不处理主观题
//        if (question instanceof GenericSubjectiveQuestion || question instanceof CompositeSubjectiveQuestion) {
//            return;
//        }
//        final String questionIdKey = RedisKnowledgeKeys.getQuestionIdKey(question.getId());
//        //把试题核心数据写入redis
//        if (question instanceof GenericQuestion) {
//            GenericQuestion genericQuestion = (GenericQuestion) question;
//            //把核心信息存入redis
//            Map<String, String> data = new HashMap();
//            data.put("area", genericQuestion.getArea() + "");
//            data.put("difficult", genericQuestion.getDifficult() + "");
//            data.put("year", genericQuestion.getYear() + "");
//            data.put("parent", genericQuestion.getParent() + "");
//            data.put("moduleId", genericQuestion.getPoints().get(0) + "");//模块id
//            redisTemplate.opsForHash().putAll(questionIdKey, data);
//        } else if (question instanceof CompositeQuestion) {
//            CompositeQuestion compositeQuestion = (CompositeQuestion) question;
//            if (CollectionUtils.isNotEmpty(compositeQuestion.getQuestions())) {//复合题里面没有子题
//                final Question subQuestion = questionDubboService.findById(compositeQuestion.getQuestions().get(0));
//                //把核心信息存入redis
//                Map<String, String> data = new HashMap();
//                //复合题只存储id列表
//                data.put("subIds", StringUtils.join(compositeQuestion.getQuestions(), ","));
//                //取复合题第一个试题的模块id来作为复合题的模块id,目的是保证子题在抽题的时候的连续性(属于同一个模块)
//                if (subQuestion == null) {
//                    logger.info("复合题的子题{}未同步过来", compositeQuestion.getQuestions().get(0));
//                    return;
//                }
//                List<Integer> points = ((GenericQuestion) subQuestion).getPoints();
//
//                if (CollectionUtils.isEmpty(points)) {
//                    return;
//                }
//
//                data.put("moduleId", points.get(0) + "");
//                redisTemplate.opsForHash().putAll(questionIdKey, data);
//            }
//        }
//
//        //复合题不用加入到组卷,由其子题加入组卷
//        if (question instanceof CompositeQuestion) {
//            CompositeQuestion compositeQuestion = (CompositeQuestion) question;
//            return;
//        }
//
//        GenericQuestion genericQuestion = (GenericQuestion) question;
//        //2008之前的题不参与推送，不在区域内的也不参与推荐
//        if (question.getSubject() == SubjectType.GWY_XINGCE) {
//            if (question.getYear() < 2008) {
//                deleteFromRedis(genericQuestion);
//                return;
//            }
//
//            //只处理真题
//            if (question.getMode() != QuestionMode.QUESTION_TRUE) {
//                deleteFromRedis(genericQuestion);
//                return;
//            }
//
//            //只处理单选，复合，对错题
//            if (question.getType() != QuestionType.WRONG_RIGHT &&
//                    question.getType() != QuestionType.SINGLE_CHOICE &&
//                    question.getType() != QuestionType.MULTIPLE_CHOICE) {
//                deleteFromRedis(genericQuestion);
//                return;
//            }
//
//        }
//
//        if (question.getStatus() != QuestionStatus.AUDIT_SUCCESS) {//删除则从推荐里面去掉
//            deleteFromRedis(genericQuestion);
//        } else {
//            addFromRedis(genericQuestion);
//        }
//    }
//
//    /**
//     * 把试题从添加到组卷列表
//     *
//     * @param genericQuestion
//     */
//    private void addFromRedis(GenericQuestion genericQuestion) {
//        //三级知识点
//        final String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(genericQuestion.getPoints().get(2));
//        final SetOperations setOperations = redisTemplate.opsForSet();
//        logger.info("add question to point, {}:{}", pointQuesionIdsKey, genericQuestion.getId());
//        setOperations.add(pointQuesionIdsKey, genericQuestion.getId() + "");
//        //更新试题个数
//        List<Integer> points = genericQuestion.getPoints();
//        updatePointSummary(points);
//    }
//
//
//    /**
//     * 从组卷列表里面删除
//     *
//     * @param genericQuestion
//     */
//    private void deleteFromRedis(GenericQuestion genericQuestion) {
//        //三级知识点
//        final String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(genericQuestion.getPoints().get(2));
//        final SetOperations setOperations = redisTemplate.opsForSet();
//        logger.info("deleteFromRedis question to point, {}:{}", pointQuesionIdsKey, genericQuestion.getId());
//        setOperations.remove(pointQuesionIdsKey, genericQuestion.getId() + "");
//        //更新试题个数
//        updatePointSummary(genericQuestion.getPoints());
//    }
//
//    /**
//     * 更新知识点个数
//     *
//     * @param points
//     */
//    private void updatePointSummary(List<Integer> points) {
//
//
//         for (int index = points.size() - 1;index >= 0; index --){
//
//
//         }
//        questionPointDubboService.findById();
//    }
//
//    public long countPointSummary(int pointId, int level, final SetOperations setOperations) {
//        if (level == 2) {
//            final String pointQuestionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(pointId);
//            long size = setOperations.size(pointQuestionIdsKey);
//            return size;
//        } else {
//            List<QuestionPoint> children = questionPointDubboService.findParent();
//            long size = 0;
//            for (QuestionPoint child : children) {
//                size += countPointSummary(child.getId(), level + 1, setOperations);
//            }
//            return size;
//        }
//    }
//    /**
//     * 遍历知识点,获取知识点试题个数
//     *
//     * @param children
//     * @return
//     */
//    private int getPointQuetsionCount(List<QuestionPoint> children) {
//        int count = 0;
//        for (QuestionPoint questionPoint : children) {
//            final Long size = redisTemplate.opsForSet().size(RedisKnowledgeKeys.getPointQuesionIds(questionPoint.getId()));
//            if (size != null) {
//                count = size.intValue() + count;
//            }
//        }
//        return count;
//    }
//    public void changeQuestionInRedis(int questionId, int newPoint, int oldPoint, int level) {
//        if (level == 2) {
//            SetOperations setOperations = redisTemplate.opsForSet();
//            final String pointQuestionDelIdsKey = RedisKnowledgeKeys.getPointQuesionIds(oldPoint);
//            logger.info("delete question to point, {}:{}", pointQuestionDelIdsKey, questionId);
//            setOperations.remove(pointQuestionDelIdsKey, String.valueOf(questionId));
//            final String pointQuestionAddIdsKey = RedisKnowledgeKeys.getPointQuesionIds(newPoint);
//            logger.info("add question to point, {}:{}", pointQuestionAddIdsKey, questionId);
//            setOperations.add(pointQuestionAddIdsKey, String.valueOf(questionId));
//        }
//        final String pointQuestionCountKey = RedisKnowledgeKeys.getPointSummaryKey();
//        SetOperations setOperations = redisTemplate.opsForSet();
//        long newPointCount = countPointSummary(newPoint, level, setOperations);
//        logger.info("newPoint={},newPointCount={}", newPoint, newPointCount);
//        long oldPointCount = countPointSummary(oldPoint, level, setOperations);
//        logger.info("oldPoint={},oldPointCount={}", oldPoint, oldPointCount);
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        hashOperations.put(pointQuestionCountKey, String.valueOf(newPoint), newPointCount + "");
//        hashOperations.put(pointQuestionCountKey, String.valueOf(oldPoint), oldPointCount + "");
//    }
//}
