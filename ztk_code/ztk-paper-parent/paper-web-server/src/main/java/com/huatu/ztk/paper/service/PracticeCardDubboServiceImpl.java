package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.paper.api.PracticeCardDubboService;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-06-21 14:52
 */
public class PracticeCardDubboServiceImpl implements PracticeCardDubboService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeCardDubboServiceImpl.class);

    @Autowired
    private AnswerCardDao answerCardDao;
    @Autowired
    private PaperAnswerCardService paperAnswerCardService;
    @Autowired
    PracticeService practiceService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    @Autowired
    private QuestionDubboService questionDubboService;

    /**
     * 根据id查询答题卡
     *
     * @param id
     * @return
     */
    @Override
    public AnswerCard findById(long id) {
        final AnswerCard answerCard = answerCardDao.findById(id);
        return answerCard;
    }

    /**
     * 创建练习试卷
     *
     * @param practicePaper
     * @param terminal
     * @param type
     * @param userId        @return
     */
    @Override
    public PracticeCard create(PracticePaper practicePaper, int terminal, int type, long userId) throws BizException {
        //默认是没有剩余时间
        return create(practicePaper, terminal, type, userId,-1);
    }

    /**
     * 创建练习试卷
     *
     * @param practicePaper
     * @param terminal      终端
     * @param type          试卷类型
     * @param userId        用户id
     * @param remainingTime 试卷总计时间
     * @return
     * @throws BizException
     */
    @Override
    public PracticeCard create(PracticePaper practicePaper, int terminal, int type, long userId, int remainingTime) throws BizException {
        PracticeCard practiceCard = PracticeCard.builder().build();
        long stime = System.currentTimeMillis();
        final int qcount = practicePaper.getQcount();//题量
        long id = 0;

//        for (int i = 0; i < 2; i++) {
////            try {
////                id = IdClient.getClient().nextCommonId();
////                break;
////            } catch (WaitException e) {
////                logger.error("get commonId fail.");
////            }
////        }
        id = Long.valueOf(String.valueOf(System.nanoTime()) + String.valueOf(System.currentTimeMillis()).substring(11));

        if (id < 1) {//获取id失败
            throw new BizException(CommonErrors.SERVICE_INTERNAL_ERROR);
        }
        logger.info("get PracticeCard id time={},uid={}", System.currentTimeMillis() - stime,userId);

        long stime1 = System.currentTimeMillis();
        practiceCard.setId(id);
        practiceCard.setUserId(userId);
        practiceCard.setPaper(practicePaper);
        practiceCard.setDifficulty(practicePaper.getDifficulty());
        int[] intAnswers = new int[qcount];
        practiceCard.setAnswers(Arrays.stream(intAnswers).mapToObj(String::valueOf).toArray(String[]::new));
        practiceCard.setCreateTime(System.currentTimeMillis());
        practiceCard.setExpendTime(0);
        practiceCard.setName(practicePaper.getName());
        practiceCard.setRcount(0);//正确数量
        practiceCard.setWcount(0);//错误数量
        practiceCard.setUcount(qcount);//未做数量
        practiceCard.setCorrects(new int[qcount]);
        practiceCard.setStatus(AnswerCardStatus.CREATE);
        practiceCard.setCatgory(subjectDubboService.getCatgoryBySubject(practicePaper.getSubject()));
        practiceCard.setSubject(practicePaper.getSubject());
        practiceCard.setTerminal(terminal);
        practiceCard.setTimes(new int[qcount]);
        practiceCard.setType(type);
        practiceCard.setRemainingTime(remainingTime);//答题剩余时间
        practiceCard.setDoubts(new int[qcount]);
        practiceCard.setRecommendedTime(questionDubboService.getRecommendedTime(practicePaper.getQuestions()));
        answerCardDao.saveWithReflectQuestion(practiceCard);

        logger.info("insert time={},uid={}", System.currentTimeMillis() - stime1, userId);
        return practiceCard;
    }

    /**
     * 提交答案
     *
     * @param practiceId 练习id
     * @param userId     用户id
     * @param answers    用户答案
     * @param summary    是否进行汇总,汇总就是交卷了，否则只是提交答案
     * @param area
     * @return
     * @throws BizException
     */
    @Override
    public AnswerCard submitAnswers(long practiceId, long userId, List<Answer> answers, boolean summary, int area,int terminal,String cv) throws BizException {
        AnswerCard answerCard = null;
        if (summary) {
            answerCard = paperAnswerCardService.submitPractice(practiceId, userId, answers, area,terminal,cv);
        }else {
            answerCard = paperAnswerCardService.submitAnswers(practiceId, userId, answers, area,false);
        }
        return answerCard;
    }

    /**
     * 分页查询做题记录
     *
     * @param userId   用户id
     * @param cursor   游标
     * @param size     每页大小
     * @param cardType 答题卡类型
     * @param cardTime 答题时间
     * @return
     */
    @Override
    public PageBean findCards(long userId, int catgory, long cursor, int size, int cardType, String cardTime) throws WaitException, BizException {
        return practiceService.findCards(userId, Arrays.asList(catgory), cursor, size,
                cardType, cardTime, false, Lists.newArrayList(-1));
    }

    @Override
    public AnswerCard findCardTotalInfoById(long id) throws BizException{
        AnswerCard card = findById(id);
        if (null == card){
            logger.info(" card error =>>> {}",id);
            return null;
        }
        return paperAnswerCardService.findById(id, card.getUserId());
    }

	@Override
	public AnswerCard findAnswerCardDetail(long id, long uid, int terminal, String cv) throws BizException {
		
		return paperAnswerCardService.findAnswerCardDetail(id, uid, terminal, cv);
	}
}
