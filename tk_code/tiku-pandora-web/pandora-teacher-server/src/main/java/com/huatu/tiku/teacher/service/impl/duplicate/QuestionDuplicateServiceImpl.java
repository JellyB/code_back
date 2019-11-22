package com.huatu.tiku.teacher.service.impl.duplicate;

import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.question.BaseQuestionMapper;
import com.huatu.tiku.teacher.service.duplicate.QuestionDuplicateService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\7\13 0013.
 */
@Service
public class QuestionDuplicateServiceImpl extends BaseServiceImpl<QuestionDuplicate> implements QuestionDuplicateService{
    @Autowired
    BaseQuestionMapper baseQuestionMapper;
    public QuestionDuplicateServiceImpl() {
        super(QuestionDuplicate.class);
    }

    @Override
    public List<Map> findWithDuplicateByQuestionId(Long questionId) {
        Example example = new Example(QuestionDuplicate.class);
        example.and().andEqualTo("status",1).andEqualTo("questionId",questionId);
        List<QuestionDuplicate> questionDuplicates =  selectByExample(example);
        if(CollectionUtils.isNotEmpty(questionDuplicates)){
            return findWithDuplicateByDuplicateId(questionDuplicates.get(0).getDuplicateId());
        }
        return null;
    }

    /**
     * 查询所有复用相关数据的连带关系
     * @param duplicateId
     * @return
     */
    public List<Map> findWithDuplicateByDuplicateId(Long duplicateId) {
        return baseQuestionMapper.findWithDuplicateByDuplicateId(duplicateId);
    }

}

