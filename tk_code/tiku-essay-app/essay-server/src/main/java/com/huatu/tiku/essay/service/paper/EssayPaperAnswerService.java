package com.huatu.tiku.essay.service.paper;

import com.huatu.tiku.essay.entity.EssayPaperAnswer;

public interface EssayPaperAnswerService {
    EssayPaperAnswer findById(long paperAnswerCardId);

    void save(EssayPaperAnswer paperAnswer);

}
