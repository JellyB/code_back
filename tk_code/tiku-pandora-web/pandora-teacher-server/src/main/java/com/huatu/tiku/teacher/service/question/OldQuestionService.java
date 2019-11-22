package com.huatu.tiku.teacher.service.question;

import com.huatu.ztk.question.bean.Question;

import java.util.List;

/**
 * mongo ztk_question
 * Created by huangqp on 2018\6\25 0025.
 */
public interface OldQuestionService {
    /**
     * 查询试题详情
     *
     * @param questionId
     * @return
     */
    Question findQuestion(Integer questionId);

    /**
     * 批量查询试题详情
     *
     * @param ids
     * @return
     */
    List<Question> findQuestions(List<Integer> ids);
}
