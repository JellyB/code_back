package com.huatu.tiku.teacher.service.impl.knowledge;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.knowledge.QuestionKnowledge;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.question.QuestionKnowledgeMapper;
import com.huatu.tiku.teacher.service.knowledge.QuestionKnowledgeService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\12 0012.
 */
@Service
public class QuestionKnowledgeServiceImpl extends BaseServiceImpl<QuestionKnowledge> implements QuestionKnowledgeService {
    public QuestionKnowledgeServiceImpl() {
        super(QuestionKnowledge.class);
    }

    @Autowired
    QuestionKnowledgeMapper questionKnowledgeMapper;
    @Override
    public void insertQuestionKnowledgeInfo(List<Long> knowledgeIds, Long questionId) {
        /**
         * 数据 - 批量添加 - 知识点试题关联表
         */
        Function<Long, List<QuestionKnowledge>> transData = (id -> {
            if (CollectionUtils.isEmpty(knowledgeIds)) {
                return Lists.newArrayList();
            }
            return knowledgeIds.stream()
                    .map(i -> QuestionKnowledge.builder().knowledgeId(i).questionId(id).build())
                    .collect(Collectors.toList());
        });
        insertAll(transData.apply(questionId));
    }

    @Override
    public int deleteByQuestionId(long questionId) {
        Example example = new Example(QuestionKnowledge.class);
        example.and().andEqualTo("questionId",questionId);
        return questionKnowledgeMapper.deleteByExample(example);
    }




}

