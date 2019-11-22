package com.huatu.ztk.report.service;

import com.huatu.ztk.report.bean.DayPractice;
import com.huatu.ztk.report.bean.PracticeSummary;
import com.huatu.ztk.report.bean.QuestionSummary;
import com.huatu.ztk.report.dao.PracticeSummaryDao;
import com.huatu.ztk.report.dubbo.PracticeSummaryDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-09-19 16:36
 */
public class PracticeSummaryDubboServiceImpl implements PracticeSummaryDubboService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeSummaryDubboServiceImpl.class);

    @Autowired
    private PracticeSummaryDao practiceSummaryDao;

    @Autowired
    private DayPracticeService dayPracticeService;

    @Autowired
    private QuestionSummaryService questionSummaryService;

    /**
     * 通过用户id查询我的统计
     *
     * @param uid 用户id
     * @return
     */
    @Override
    public PracticeSummary findByUid(long uid, int subject) {
        String totalSummaryId = practiceSummaryDao.getTotalSummaryId(uid, subject);
        PracticeSummary practiceSummary = practiceSummaryDao.findById(totalSummaryId);
        if (practiceSummary == null) {
            practiceSummary = initTotalSummary(uid, subject);
        }
        return practiceSummary;
    }


    /**
     * 初始化总练习统计数据
     *
     * @param userId
     * @param subject
     * @return
     */
    public PracticeSummary initTotalSummary(long userId, int subject) {
        final List<DayPractice> dayPractices = dayPracticeService.findAll(userId, subject);
        int count = 0;//练习次数
        int dayCount = 0;//练习天数
        double average = 0;//练习平均 次数/天
        int rcount = 0; //做对题数计数
        int wcount = 0; //做错题数计数
        for (DayPractice dayPractice : dayPractices) {
            count = count + dayPractice.getCount();
            dayCount++;

            rcount += dayPractice.getQuestionRightCount();
            wcount += dayPractice.getQuestionWrongCount();
        }
        if (dayCount > 0) {//大于0,则做处理
            average = BigDecimal.valueOf(count).divide(BigDecimal.valueOf(dayCount), 1, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        //查询试题的统计,获得做题总耗时和答题速度
        QuestionSummary questionSummary = questionSummaryService.findByUserId(userId, subject);
        int useTime = questionSummary.getTimes();
        int speed = questionSummary.getSpeed();

        final PracticeSummary practiceSummary = PracticeSummary.builder()
                .id(practiceSummaryDao.getTotalSummaryId(userId, subject))
                .uid(userId)
                .subject(subject)
                .practiceCount(count)
                .dayCount(dayCount)
                .average(average)
                .wcount(wcount)
                .rcount(rcount)
                .speed(speed)
                .times(useTime)
                .build();
        practiceSummaryDao.insert(practiceSummary);
        return practiceSummary;
    }
}
