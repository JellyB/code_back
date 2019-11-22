package com.huatu.tiku.dataClean;

import com.google.common.collect.Lists;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.knowledge.QuestionKnowledge;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.teacher.dao.knowledge.KnowledgeSubjectMapper;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.knowledge.QuestionKnowledgeService;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 清理 在mysql删除 却没有再 mongoDB 中删除的数据
 * Created by lijun on 2018/11/8
 */
public class CleanDeletedMultiQuestion extends TikuBaseTest {

    @Autowired
    private ImportService importService;

    @Autowired
    private Mapper<BaseQuestion> mapper;

    @Autowired
    private QuestionKnowledgeService questionKnowledgeService;

    @Autowired
    private KnowledgeSubjectMapper knowledgeSubjectMapper;


    @Test
    public void clean() {
        WeekendSqls<BaseQuestion> weekendSql = WeekendSqls.custom();
        weekendSql.andEqualTo(BaseQuestion::getStatus, -1);
        weekendSql.andGreaterThan(BaseQuestion::getMultiId, 0);
        Example example = Example.builder(BaseQuestion.class)
                .where(weekendSql)
                .build();
        List<BaseQuestion> select = mapper.selectByExample(example);
        select.parallelStream()
                .forEach(baseQuestion -> importService.importQuestion(baseQuestion.getId()));

        System.out.println(select.size());
    }


    /**
     * wrongKnowledgeId 绑定错误的知识点
     * rightKnowledgeId 需要修改为正确的知识点
     * 需求:系统中只支持三级知识点，但是由于前端未做限制，导致录入时，部分试题绑定了错误的二级知识点;所以需要以知识点
     * 为单位，批量纠正错误的试题信息
     */
    @Test
    public void changeQuestionKnowledge() {
        Long wrongKnowledgeId = 67539L;
        Long rightKnowledgeId = 67540L;

        Example example = new Example(QuestionKnowledge.class);
        example.and().andEqualTo("knowledgeId", wrongKnowledgeId);
        List<QuestionKnowledge> knowledgeList = questionKnowledgeService.selectByExample(example);

        if (CollectionUtils.isNotEmpty(knowledgeList)) {
            final List<Long> questionIds = knowledgeList.stream().map(QuestionKnowledge::getQuestionId).collect(Collectors.toList());
            knowledgeSubjectMapper.updateKnowledgeIdByQuestionId(rightKnowledgeId, questionIds);
            System.out.print("需要修改的试题ID是:{}" + questionIds);
            questionIds.forEach(questionId -> {
                importService.sendQuestion2Mongo(questionId.intValue());
                System.out.print("同步成功试题ID是:{}" + questionId);
            });
        }
    }

}
