package com.huatu.tiku.essay.service.impl.question;

import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 试题答题卡逻辑处理
 */
@Service
public class EssayQuestionAnswerServiceImpl implements EssayQuestionAnswerService {

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;


    /**
     * 根据试卷答题卡Id查询 试题答题卡
     *
     * @param paperAnswerId
     * @return
     */
    @Override
    public List<EssayQuestionAnswer> findByAnswerId(long paperAnswerId) {
        List<EssayQuestionAnswer> answers = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus
                (paperAnswerId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "id"));
        return answers;
    }

    @Override
    public EssayQuestionAnswer findById(Long answerId) {
        return essayQuestionAnswerRepository.findByIdAndStatus(answerId,EssayStatusEnum.NORMAL.getCode());
    }

    @Override
    public void save(EssayQuestionAnswer questionAnswer) {
        essayQuestionAnswerRepository.save(questionAnswer);
    }
}
