package com.huatu.ztk.paper.api;

import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.self.generator.core.WaitException;

import java.util.List;

/**
 * 练习dubbo service
 * Created by shaojieyue
 * Created time 2016-06-21 14:44
 */
public interface PracticeCardDubboService {

    /**
     * 根据id查询答题卡
     *
     * @param id
     * @return
     */
    public AnswerCard findById(long id);

    /**
     * 创建练习试卷
     *
     * @param practicePaper
     * @param terminal
     * @return
     */
    public PracticeCard create(PracticePaper practicePaper, int terminal, int type, long userId) throws BizException;

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
    public PracticeCard create(PracticePaper practicePaper, int terminal, int type, long userId, int remainingTime) throws BizException;

    /**
     * 提交练习答案
     *
     * @param practiceId 练习id
     * @param userId     用户id
     * @param answers    用户答案
     * @param summary    是否进行汇总,汇总就是交卷了，否则只是提交答案
     * @param area       地区
     * @return
     * @throws BizException
     */
    public AnswerCard submitAnswers(long practiceId, long userId, List<Answer> answers, boolean summary, int area, int terminal, String cv) throws BizException;

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
    public PageBean findCards(long userId, int catgory, long cursor, int size, int cardType, String cardTime) throws WaitException, BizException;


    AnswerCard findCardTotalInfoById(long id) throws BizException;
    
    /**
     * 查询答题卡详情
     * @param id
     * @return
     */
    AnswerCard findAnswerCardDetail(long id, long uid, int terminal, String cv) throws BizException;
}
