package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.question.common.QuestionCorrectType;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ismyway on 16/5/19.
 */
public class PaperAnswerCardServiceTest extends BaseTest {
    //啥打法是否对
    private static final Logger logger = LoggerFactory.getLogger(PaperAnswerCardServiceTest.class);

    @Autowired
    private PaperAnswerCardService answerCardService;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private PracticeCardDubboService practiceCardDubboService;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    long praId = 1535379822685978624L;
    long uid = 267317;

    @Test
    public void submitAnswersTest() throws BizException {

        for (int j = 0; j < 4; j++) {
            final AnswerCard answerCard = answerCardService.findById(praId, uid);
            List<Integer> questions = null;
            if (answerCard instanceof PracticeCard) {
                questions = ((PracticeCard) answerCard).getPaper().getQuestions();
            } else if (answerCard instanceof StandardCard) {
                questions = ((StandardCard) answerCard).getPaper().getQuestions();
            }
            List<Answer> answers1 = new ArrayList<>();
            int rcount = 0;
            int wcount = 0;
            int sumTime = 0;
            for (Integer question : questions) {
                Answer answer = new Answer();
//                answer.setAnswer(RandomUtils.nextInt(0, 5));
                answer.setCorrect(RandomUtils.nextInt(0, 3));
                answer.setTime(RandomUtils.nextInt(50, 70));
                answer.setQuestionId(question);
                answers1.add(answer);
            }
            List<Answer> answers2 = new ArrayList<>();
            for (Integer question : questions) {
                Answer answer = new Answer();
//                answer.setAnswer(RandomUtils.nextInt(0, 5));
                answer.setCorrect(RandomUtils.nextInt(0, 3));
                answer.setTime(RandomUtils.nextInt(50, 70));
                answer.setQuestionId(question);
                answers2.add(answer);
                if (answer.getCorrect() == QuestionCorrectType.RIGHT) {
                    rcount++;
                } else if (answer.getCorrect() == QuestionCorrectType.WRONG) {
                    wcount++;
                }
                sumTime = sumTime + answer.getTime();
            }
            System.out.println("-------------->sum" + (rcount + wcount));
            answers1.addAll(answers2);
            final AnswerCard newCard = answerCardService.submitAnswers(praId, uid, answers1, -9, false);
            for (int i = 0; i < answers2.size(); i++) {
                final Answer answer = answers2.get(i);
                Assert.assertEquals(answer.getAnswer(), newCard.getAnswers()[i]);
                Assert.assertEquals(answer.getCorrect(), newCard.getCorrects()[i]);
                Assert.assertEquals(answer.getTime(), newCard.getTimes()[i]);
            }

            Assert.assertEquals(newCard.getSpeed(), sumTime / questions.size());
            Assert.assertEquals(newCard.getExpendTime(), sumTime);
            Assert.assertEquals(newCard.getRcount(), rcount);
            Assert.assertEquals(newCard.getWcount(), wcount);
            Assert.assertEquals(newCard.getUcount(), questions.size() - rcount - wcount);
            Assert.assertEquals(newCard.getStatus(), AnswerCardStatus.UNDONE);
        }

    }

    @Test
    public void submitPracticeTest() throws BizException {
        submitAnswersTest();
        final AnswerCard answerCard = answerCardService.submitPractice(praId, uid, new ArrayList<>(), -9, TerminalType.ANDROID, "7.0.0");
        Assert.assertEquals(answerCard.getStatus(), AnswerCardStatus.FINISH);
        Assert.assertNotNull(answerCard.getPoints());
    }

    @Test
    public void findByIdTest() {
        final AnswerCard answerCard = practiceCardDubboService.findById(1525088653376749568L);
        System.out.println(answerCard.getUserId());
    }

    @Test
    public void test() throws BizException {
        paperAnswerCardService.findAnswerCardDetail(1716758475310115077L,233982952,1,"7.1.160");
    }
}
