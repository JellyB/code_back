package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: OrderAddUserID
 * @description: TODO
 * @date 2019-07-3113:48
 */
public class OrderAddUserID extends TikuBaseTest {

    @Autowired
    CorrectOrderRepository correctOrderRepository;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssayPaperAnswerRepository paperAnswerRepository;

    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;

    @Test
    public void test() {
        List<CorrectOrder> all = correctOrderRepository.findAll();
        all.parallelStream().filter(i -> i.getAnswerCardType() == EssayAnswerCardEnum.TypeEnum.QUESTION.getType())
                .filter(i -> i.getUserId() == 0)
                .forEach(i -> {
                    EssayQuestionAnswer one = essayQuestionAnswerRepository.findOne(i.getAnswerCardId());
                    if (null != one) {
                        i.setUserId(one.getUserId());
                        correctOrderRepository.save(i);
                    }
                });

        all.parallelStream().filter(i -> i.getAnswerCardType() == EssayAnswerCardEnum.TypeEnum.PAPER.getType())
                .filter(i -> i.getUserId() == 0)
                .forEach(i -> {
                    EssayPaperAnswer one = paperAnswerRepository.findOne(i.getAnswerCardId());
                    if (null != one) {
                        i.setUserId(one.getUserId());
                        correctOrderRepository.save(i);
                    }
                });
    }

    /**
     * 刷人工批改套题答题卡的分数
     */
    @Test
    public void test1() {
        List<EssayPaperAnswer> all = paperAnswerRepository.findAll();
        List<EssayPaperAnswer> collect = all.parallelStream().filter(i -> i.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus())
                .filter(i -> i.getCorrectMode() != CorrectModeEnum.INTELLIGENCE.getMode())
                .collect(Collectors.toList());
        for (EssayPaperAnswer answer : collect) {
            long id = answer.getId();
            List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository.findByPaperAnswerIdAndUserIdAndStatus(id, answer.getUserId(), EssayStatusEnum.NORMAL.getCode());
            if (CollectionUtils.isEmpty(questionAnswers)) {
                continue;
            }
            double sum = questionAnswers.stream().mapToDouble(EssayQuestionAnswer::getExamScore).sum();
            if(answer.getExamScore() != sum){
                System.out.println("答题卡试卷和试题分数不一致：id = " + id);
                for (EssayQuestionAnswer questionAnswer : questionAnswers) {
                    List<EssayLabelTotal> labelTotal = essayLabelTotalRepository.findByAnswerIdAndStatusAndLabelFlag(questionAnswer.getId(), EssayStatusEnum.NORMAL.getCode(),
                            LabelFlagEnum.STUDENT_LOOK.getCode());
                    if(CollectionUtils.isEmpty(labelTotal)){
                        questionAnswer.setExamScore(0D);
                    }else{
                        Double score = labelTotal.get(0).getScore();
                        questionAnswer.setExamScore(score == null ? 0D:score);
                    }
                }
                essayQuestionAnswerRepository.save(questionAnswers);
                double sum1 = questionAnswers.stream().mapToDouble(EssayQuestionAnswer::getExamScore).sum();
                answer.setExamScore(sum1);
                paperAnswerRepository.save(answer);
            }
        }
    }
}
