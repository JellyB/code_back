package com.huatu.ztk.report.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionMode;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import com.huatu.ztk.question.util.QuestionPointPoolUtil;
import com.huatu.ztk.report.dao.AnswerCardDao;
import com.huatu.ztk.report.dao.QuestionDao;
import com.huatu.ztk.report.dao.QuestionUserMetaDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class QuestionUserMetaService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionUserMetaService.class);

    @Autowired
    QuestionUserMetaDao questionUserMetaDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    AnswerCardDao answerCardDao;

    /**
     * 答题卡数据
     *
     * @param answerCard
     */
    public void updateUserQuestionMeta(AnswerCard answerCard) {
        StopWatch stopWatch = new StopWatch("答题卡处理");
        try {
            logger.error("id={},stage={},status={}",answerCard.getId(),answerCard.getStage(),answerCard.getStatus());
            if (answerCard.getStage() != 0 || answerCard.getStatus() != AnswerCardStatus.FINISH) {
                return;
            }
            stopWatch.start("getQuestionIds");
            List<Integer> questionIds = getQuestionIds.apply(answerCard);
            stopWatch.stop();
            if (CollectionUtils.isEmpty(questionIds)) {
                return;
            }
            stopWatch.start("question info find");
            List<Question> questions = questionDao.findByIds(questionIds);
            stopWatch.stop();
            int[] corrects = answerCard.getCorrects();
            for (Question question : questions) {
                stopWatch.start("question handler :"+question.getId());
                int i = questionIds.indexOf(question.getId());
                try {
                    handlerQuestionUserMeta(question, answerCard.getUserId(), corrects[i]);
                } catch (Exception e) {
                    logger.error("handler questionUserMeta error,question={}", JsonUtil.toJson(question));
                    e.printStackTrace();
                }
                stopWatch.stop();
            }
            stopWatch.start("save answerCard");
            answerCard.setStage(1);
            answerCardDao.save(answerCard);
            stopWatch.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(stopWatch.isRunning()){
                stopWatch.stop();
            }
            System.out.println(stopWatch.prettyPrint());
        }
    }

    /**
     * 处理用户试题数据
     *
     * @param question
     * @param userId
     * @param correct
     */
    private void handlerQuestionUserMeta(Question question, long userId, int correct) {
        if (!(question instanceof GenericQuestion)) {
            return;
        }
        if (correct == 0) {
            return;
        }
        GenericQuestion genericQuestion = (GenericQuestion) question;
        QuestionUserMeta meta = questionUserMetaDao.findById(userId, question.getId());
        QuestionUserMeta build = QuestionUserMeta.builder()
                .id(questionUserMetaDao.generId(userId, question.getId()))
                .questionId(genericQuestion.getId())
                .userId(userId)
                .pointsList(genericQuestion.getPoints())
                .pointsName(genericQuestion.getPointsName())
                .firstPointId(genericQuestion.getPoints().get(0))
                .secondPointId(genericQuestion.getPoints().get(1))
                .thirdPointId(genericQuestion.getPoints().get(2))
                .status(question.getStatus())
                .poolFlag(QuestionPointPoolUtil.isPoolFlag(genericQuestion) ? 1 : 0)
                .total(null == meta ? 0 : meta.getTotal())
                .errorCount(null == meta ? 0 : meta.getErrorCount())
                .build();
        build.setTotal(build.getTotal() + 1);
        if (correct == 2) {
            build.setErrorCount(build.getErrorCount() + 1);
            build.setErrorFlag(1);
        }else if(correct == 1){
            build.setErrorFlag(0);
        }
        questionUserMetaDao.save(build);
    }

    /**
     * 答题卡中试题ID获取
     */
    private Function<AnswerCard, List<Integer>> getQuestionIds = (answerCard -> {
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            return paper == null ? Lists.newArrayList() : paper.getQuestions();
        } else if (answerCard instanceof PracticeCard) {
            PracticePaper paper = ((PracticeCard) answerCard).getPaper();
            return paper == null ? Lists.newArrayList() : paper.getQuestions();
        }
        logger.info("getQuestionIds error,answerCard's class ={}", answerCard.getClass());
        return Lists.newArrayList();
    });
}
