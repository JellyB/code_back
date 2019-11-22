package com.huatu.tiku.teacher.service;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.subject.SubjectQuestionType;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.subject.SubjectQuestionTypeService;
import com.huatu.tiku.teacher.service.question.QuestionTypeService;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by x6 on 2018/7/25.
 */
public class QuestionTypeServiceT extends TikuBaseTest {
    @Autowired
    QuestionTypeService questionTypeService;
    @Autowired
    SubjectQuestionTypeService subjectQuestionTypeService;
    @Autowired
    KnowledgeService knowledgeService;

    @Test
    public void test() {
        List<Long> types = Lists.newArrayList(110L, 111L, 112L);
        List<Long> subjects = Lists.newArrayList(1000000L, 1000001L, 1000002L, 1000003L, 1000004L);
        for (Long subject : subjects) {
            for (Long type : types) {
                Example example = new Example(SubjectQuestionType.class);
                example.and().andEqualTo("subjectId", subject).andEqualTo("questionType", type);
                List<SubjectQuestionType> subjectQuestionTypes = subjectQuestionTypeService.selectByExample(example);
                if (CollectionUtils.isNotEmpty(subjectQuestionTypes)) {
                    continue;
                }
                SubjectQuestionType subjectQuestionType = SubjectQuestionType.builder().subjectId(subject).questionType(type).build();
                subjectQuestionTypeService.insert(subjectQuestionType);
            }
        }


    }

    @Test
    public void testKnowledge() {
        String knowledges = knowledgeService.getKnowledgeInfo(1L, 774L);
        System.out.println("科目Id是----：" + knowledges);

    }
}
