package com.huatu.ztk.report.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.report.bean.PracticeSummary;
import com.huatu.ztk.report.service.PracticeSummaryService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by shaojieyue
 * Created time 2016-05-30 18:45
 */


@RestController
@RequestMapping(value = "/v1/summary/practice",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PracticeSummaryControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(PracticeSummaryControllerV1.class);

    @Autowired
    private PracticeSummaryService practiceSummaryService;

    @Autowired
    private UserSessionService userSessionService;

    @RequestMapping(value = "",method = RequestMethod.GET)
    public Object find(@RequestHeader(required = false) String token) throws BizException{
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        //科目
        int subject = userSessionService.getSubject(token);
        final int area = userSessionService.getArea(token);
        final PracticeSummary practiceSummary = practiceSummaryService.findTotalSummary(userId, subject);
        return practiceSummary;
    }



}
