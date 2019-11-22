package com.huatu.tiku.essay.service.question;

import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;

import java.util.List;

public interface EssayQuestionAnswerService {
    List<EssayQuestionAnswer> findByAnswerId(long paperAnswerCardId);

    EssayQuestionAnswer findById(Long answerId);

    void save(EssayQuestionAnswer questionAnswer);
}
