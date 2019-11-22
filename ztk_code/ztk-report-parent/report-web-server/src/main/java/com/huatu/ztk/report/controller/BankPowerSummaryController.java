package com.huatu.ztk.report.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.exception.BizException;
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

@RestController
@RequestMapping(value = "/v1/report/bank",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class BankPowerSummaryController {
    private static final Logger logger = LoggerFactory.getLogger(BankPowerSummaryController.class);

    @Autowired
    private PowerSummaryService powerSummaryService;

    @Autowired
    private QuestionSummaryService questionSummaryService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private DayPracticeService dayPracticeService;

    @Autowired
    private ModuleSummaryService moduleSummaryService;

    @Autowired
    private PracticeSummaryService practiceSummaryService;

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

        final QuestionSummary questionSummary = questionSummaryService.findByUserId(userId, subject);
        data.put("questionSummary",questionSummary);
        return data;
    }
}