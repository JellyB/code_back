package com.huatu.ztk.report.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.report.bean.ModuleSummary;
import com.huatu.ztk.report.bean.PowerSummary;
import com.huatu.ztk.report.bean.PracticeSummary;
import com.huatu.ztk.report.bean.QuestionSummary;
import com.huatu.ztk.report.service.*;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by shaojieyue
 * Created time 2016-06-03 15:05
 */

@RestController
@RequestMapping(value = "/v1/report",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PowerSummaryControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(PowerSummaryControllerV1.class);

    @Autowired
    private PowerSummaryService powerSummaryService;

    @Autowired
    private QuestionSummaryService questionSummaryService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private DayPracticeService dayPracticeService;

    @Autowired
    private ModuleSummaryService moduleSummaryService;

    @Autowired
    private PracticeSummaryService practiceSummaryService;

    /**
     * 查询能力概况
     * @param token
     * @return
     */
    @RequestMapping(value = "summary",method = RequestMethod.GET)
    public Object summary(@RequestHeader(required = false) String token) throws BizException{
        userSessionService.assertSession(token);

        long t1 = System.currentTimeMillis();
        //用户id
        long userId = userSessionService.getUid(token);
        //科目
        int subject = userSessionService.getSubject(token);
        final int area = userSessionService.getArea(token);
        logger.info("find userSessionService expendTime={}", System.currentTimeMillis() - t1);
        final PowerSummary powerSummary = powerSummaryService.find(userId, subject, area);
        final QuestionSummary questionSummary = questionSummaryService.findByUserId(userId, subject);
        long t2 = System.currentTimeMillis();
        final Set<Integer> userPoints = questionPointDubboService.findUserPoints(userId, subject);
        logger.info("findUserPoints expendTime={}", System.currentTimeMillis() - t2);
        //总记录数
        long t3 = System.currentTimeMillis();
        final int pointsCount = questionPointDubboService.findPointsCount(subject);
        logger.info("findPointsCount expendTime={}", System.currentTimeMillis() - t3);
        Map pointMap = Maps.newHashMap();
        pointMap.put("doCount",userPoints.size());//已做数量
        pointMap.put("allCount",pointsCount);//全部数量
        Map data = Maps.newHashMap();

        //试题汇总
        data.put("questionSummary",questionSummary);
        //能力汇总
        data.put("powerSummary",powerSummary);
        //知识点汇总
        data.put("pointSummary",pointMap);
        return data;
    }

    /**
     * 查询报告详情
     * @return
     */
    @RequestMapping(value = "detail",method = RequestMethod.GET)
    public Object detail(@RequestHeader(required = false) String token) throws BizException{
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);
        //科目
        int subject = userSessionService.getSubject(token);
        final int area = userSessionService.getArea(token);

        final PowerSummary powerSummary = powerSummaryService.find(userId, subject,area);

        List<ModuleSummary> moduleSummaries = moduleSummaryService.find(userId,subject);

        Line forecastSocreLine = dayPracticeService.queryForecastChart(userId,subject);

        //添加用时日志，以观察此方法的耗时
        long stime = System.currentTimeMillis();
        PracticeSummary monthSummary = practiceSummaryService.findMonthSummary(userId, subject);
        logger.info("findMonthSummary utime={},token={}", System.currentTimeMillis() - stime,token);

        Map data = Maps.newHashMap();
        //能力评估
        data.put("powerSummary",powerSummary);
        //分数预测
        data.put("forecast",forecastSocreLine);
        //模块预测
        data.put("moduleSummary",moduleSummaries);
        //每月统计
        data.put("monthSummary", monthSummary);
        return data;
    }
}