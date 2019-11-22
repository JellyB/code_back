package com.huatu.ztk.report.service;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.report.bean.PracticeSummary;
import com.huatu.ztk.report.dao.DayPracticeDao;
import com.huatu.ztk.report.dao.PracticeSummaryDao;
import com.huatu.ztk.report.dubbo.PracticeSummaryDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Created by shaojieyue
 * Created time 2016-05-30 18:46
 */

@Service
public class PracticeSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeSummaryService.class);

    @Autowired
    private PracticeSummaryDubboService practiceSummaryDubboService;


    @Autowired
    private PracticeSummaryDao practiceSummaryDao;

    @Autowired
    private DayPracticeDao dayPracticeDao;

    /**
     * 根据用户查询练习总结
     *
     * @param uid
     * @param subject
     * @return
     */
    public PracticeSummary findTotalSummary(long uid, int subject) {
        return practiceSummaryDubboService.findByUid(uid, subject);
    }


    /**
     * 查询每月统计
     *
     * @param userId
     * @param subject
     * @return
     */
    public PracticeSummary findMonthSummary(long userId, int subject) {
        PracticeSummary practiceSummary = practiceSummaryDao.findById(practiceSummaryDao.getMonthSummaryId(userId, subject));
        if (practiceSummary == null) {
            practiceSummary = initMonthSummary(userId, subject);
        }
        return practiceSummary;
    }


    /**
     * 更新统计
     *
     * @param answerCard
     */
    public void updateSummary(AnswerCard answerCard) {
        long userId = answerCard.getUserId();
        int subject = answerCard.getSubject();
        String monthSummaryId = practiceSummaryDao.getMonthSummaryId(userId, subject);
        String totalSummaryId = practiceSummaryDao.getTotalSummaryId(userId, subject);

        PracticeSummary monthSummary = findMonthSummary(userId, subject);
        int monthPracticeDayCount = dayPracticeDao.queryMonthPracticeDayCount(userId, subject);
        updateSummary(monthSummary, answerCard, monthSummaryId, monthPracticeDayCount);

        PracticeSummary totalSummary = findTotalSummary(userId, subject);
        int totalPracticeDayCount = dayPracticeDao.queryTotalPracticeDayCount(userId, subject);

        updateSummary(totalSummary, answerCard, totalSummaryId, totalPracticeDayCount);
    }

    /**
     * 月统计与总统计的更新操作大致相同
     * @param summary
     * @param answerCard
     * @param summaryId
     * @param dayCount
     */
    private void updateSummary(PracticeSummary summary, AnswerCard answerCard, String summaryId, int dayCount) {
        //总做题数
        int totalCount = summary.getRcount() + summary.getWcount()
                + answerCard.getRcount() + answerCard.getWcount();
        //总耗时
        int totalTime = summary.getTimes() + answerCard.getExpendTime();

        Update update = new Update();
        //增加
        update.inc("rcount", answerCard.getRcount());
        update.inc("wcount", answerCard.getWcount());
        update.inc("times", answerCard.getExpendTime());
        update.inc("practiceCount", 1);

        //设置
        //练习天数
        update.set("dayCount", dayCount);
        //平均答题速度，四舍五入
        update.set("speed", totalCount == 0 ? 0 :
                new BigDecimal((double)totalTime / totalCount).setScale(0,BigDecimal.ROUND_HALF_UP));
        //练习平均值，练习次数(加上本次）/天数，取一位小数
        update.set("average", dayCount == 0 ? 0 :
                new BigDecimal((double) (summary.getPracticeCount() + 1) / dayCount).setScale(1, BigDecimal.ROUND_HALF_UP));

        practiceSummaryDao.updateSummary(summaryId, update);
    }

    /**
     * 初始化月统计
     * @param userId
     * @param subject
     * @return
     */
    private PracticeSummary initMonthSummary(long userId, int subject) {
        PracticeSummary monthSummary = new PracticeSummary();

        monthSummary.setId(practiceSummaryDao.getMonthSummaryId(userId,subject));
        monthSummary.setUid(userId);
        monthSummary.setSubject(subject);

        //其它数据取默认值
        practiceSummaryDao.insert(monthSummary);

        return monthSummary;
    }
}
