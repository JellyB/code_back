package com.huatu.tiku.teacher.listener;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.dao.question.BaseQuestionMapper;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.duplicate.QuestionDuplicateService;
import com.huatu.ztk.question.bean.ReflectQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\7\2 0002.
 */
@Slf4j
@Component
//@RabbitListener(queues = "sync_question_2_mongo")
public class SyncQuestion2MongoListener {

    @Autowired
    ImportService importService;

    @Autowired
    BaseQuestionMapper baseQuestionMapper;

    @Autowired
    QuestionDuplicateService questionDuplicateService;

    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    /**
     * 同步试题时，同时同步它的子题和相关复用数据的试题
     *
     * @param message
     */
    @RabbitHandler
    public void onMessage(Map message) {
        try {
            log.info("message={}", message);
            Long id = Long.parseLong(message.get("id").toString());
            List<Long> relationIds = Lists.newArrayList(id);      //试题id对应的子题或者有复用关系的试题ID集合，也需要同步更新
            ReflectQuestion reflectQuestion = reflectQuestionDao.findById(id.intValue());
            if (null != reflectQuestion) {
                id = new Long(reflectQuestion.getNewId());
                relationIds.add(id);
            }
            BaseQuestion baseQuestion = baseQuestionMapper.selectByPrimaryKey(id);
            if (null == baseQuestion) {
                log.error("sync_question_2_mongo: error ,id ={}", id);
                return;
            }
            /**
             * 查询试题的子题
             */
            Function<Long, List<Long>> findChildren = (parent -> {
                Example example = new Example(BaseQuestion.class);
                example.and().andEqualTo("multiId", parent);
                List<BaseQuestion> baseQuestions = baseQuestionMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(baseQuestions)) {
                    return Lists.newArrayList();
                }
                return baseQuestions.stream().map(BaseQuestion::getId).collect(Collectors.toList());
            });
            /**
             * 直接关联的服用数据ID查询
             */
            Function<List<Long>, List<Long>> findDuplicateIds = (questionIds -> {
                if (CollectionUtils.isEmpty(questionIds)) {
                    return Lists.newArrayList();
                }
                Example example = new Example(QuestionDuplicate.class);
                example.and().andIn("questionId", questionIds);
                List<QuestionDuplicate> questionDuplicates = questionDuplicateService.selectByExample(example);
                if (CollectionUtils.isEmpty(questionDuplicates)) {
                    return Lists.newArrayList();
                }
                return questionDuplicates.stream().map(QuestionDuplicate::getDuplicateId).distinct().collect(Collectors.toList());
            });
            /**
             * 所有关联的试题ID查询
             */
            Function<List<Long>, List<Long>> findAllRelations = (duplicateIds -> {
                if (CollectionUtils.isEmpty(duplicateIds)) {
                    return Lists.newArrayList();
                }
                Example example = new Example(QuestionDuplicate.class);
                example.and().andIn("duplicateId", duplicateIds);
                List<QuestionDuplicate> questionDuplicates = questionDuplicateService.selectByExample(example);
                if (CollectionUtils.isEmpty(questionDuplicates)) {
                    return Lists.newArrayList();
                }
                return questionDuplicates.stream().map(QuestionDuplicate::getQuestionId).distinct().collect(Collectors.toList());
            });
            //如果是复合题，查询复合题信息
            if (baseQuestion.getMultiFlag() == BaseInfo.YESANDNO.YES.getKey()) {
                List<Long> children = findChildren.apply(id);
                if (CollectionUtils.isNotEmpty(children)) {
                    relationIds.addAll(children);
                }
            }
            List<Long> apply = findAllRelations.apply(findDuplicateIds.apply(relationIds));
            if (CollectionUtils.isNotEmpty(apply)) {
                relationIds.addAll(apply);
            }

            /**
             * 同步子题的时,再同步一遍复合题,保证mongo中和redis中数据的正确性
             *
             */
            if (baseQuestion.getMultiId() != 0L) {
                relationIds.add(baseQuestion.getMultiId());
            }
            log.info("同步试题ID为:{}", relationIds);
            for (Long relationId : relationIds.stream().distinct().collect(Collectors.toList())) {
                importService.importQuestion(relationId);
            }
        } catch (Exception e) {
            log.error("sync_question_2_mongo fail ,message = {}", message);
            e.printStackTrace();
        }

    }
}

