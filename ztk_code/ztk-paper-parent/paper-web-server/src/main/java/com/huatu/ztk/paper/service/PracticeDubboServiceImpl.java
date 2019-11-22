package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.paper.api.PracticeDubboService;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.paper.dao.PaperDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 练习dubboservice 实现
 * Created by shaojieyue
 * Created time 2016-07-05 16:40
 */
public class PracticeDubboServiceImpl implements PracticeDubboService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeDubboServiceImpl.class);

    public static final String WEI_XIN_PRACTICE = "微信答题";

    @Autowired
    private QuestionPointDubboService questionPointDubboService;
    @Autowired
    private QuestionStrategyDubboService questionStrategyDubboService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PaperDao paperDao;

    @Autowired
    private SubjectDubboService subjectDubboService;

    /**
     * 根据知识点组装练习试卷
     *
     * @param point
     * @param qcount
     * @param subject
     * @return
     */
    @Override
    public PracticePaper create(long uid, int subject, int point, int qcount) {
        final QuestionStrategy questionStrategy = questionStrategyDubboService.randomStrategy(uid, subject, point, qcount);
        final PracticePaper practicePaper = PracticePaper.builder().build();
        practicePaper.setQuestions(questionStrategy.getQuestions());
        practicePaper.setDifficulty(questionStrategy.getDifficulty());
        practicePaper.setSubject(subject);//设置科目
        practicePaper.setCatgory(subjectDubboService.getCatgoryBySubject(subject));
        practicePaper.setQcount(questionStrategy.getQuestions().size());
        practicePaper.setModules(questionStrategy.getModules());
        return practicePaper;
    }

    /**
     * 随机组卷
     *
     * @param subject 科目
     * @param point   知识点
     * @param qcount  试题数目
     * @return
     */
    @Override
    public PracticePaper create(int subject, int point, int qcount) {
        final QuestionStrategy questionStrategy = questionStrategyDubboService.randomStrategyNoUser(subject, point, qcount);
        final PracticePaper practicePaper = PracticePaper.builder().build();
        practicePaper.setQuestions(questionStrategy.getQuestions());
        practicePaper.setDifficulty(questionStrategy.getDifficulty());
        practicePaper.setSubject(subject);//设置科目
        practicePaper.setCatgory(subjectDubboService.getCatgoryBySubject(subject));
        practicePaper.setQcount(questionStrategy.getQuestions().size());
        practicePaper.setModules(questionStrategy.getModules());
        return practicePaper;
    }


    /**
     * 根据知识点组装微信练习试卷
     *
     * @param point
     * @param qcount
     * @return
     */
    @Override
    public PracticePaper createWeixinPaper(long uid, int subject, int point, int qcount) throws BizException{
        final QuestionPoint questionPoint = questionPointDubboService.findById(point);
        return practiceService.combinePracticePaper(WEI_XIN_PRACTICE, qcount, questionPoint, uid, subject);
    }
}
