package com.huatu.tiku.essay.service.impl.paper;

import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.service.paper.EssayPaperAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EssayPaperAnswerServiceImpl implements EssayPaperAnswerService {

    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;

    /**
     * 根据答题卡ID查询答题卡信息
     *
     * @param paperAnswerCardId
     * @return
     */
    @Override
    public EssayPaperAnswer findById(long paperAnswerCardId) {
        return essayPaperAnswerRepository.findByIdAndStatus(paperAnswerCardId, EssayStatusEnum.NORMAL.getCode());
    }

    @Override
    public void save(EssayPaperAnswer paperAnswer) {
        essayPaperAnswerRepository.save(paperAnswer);
    }
}
