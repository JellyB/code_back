package com.huatu.tiku.match.service.v1.paper;

import com.huatu.tiku.match.bo.paper.QuestionSimpleBo;
import com.huatu.ztk.question.bean.Question;

import java.util.List;

/**
 * Created by lijun on 2018/11/1
 */
public interface QuestionService {

    /**
     * 查询试题详情
     */
    Question findQuestionCacheById(Integer questionId);

    /**
     * 批量查询试题详情
     * @param questionIds
     * @return
     */
    List<Question> findQuestionCacheByIds(List<Integer> questionIds);

    /**
     * 查询试题详情
     */
    List<QuestionSimpleBo> findQuestionSimpleBoById(List<Integer> questionIdList);

    /**
     * 根据试卷id 获取试题信息
     */
    List<QuestionSimpleBo> findQuestionSimpleBoByPaperId(int paperId);

}
