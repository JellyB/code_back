package com.huatu.ztk.report.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.report.service.QuestionSummaryService;
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
 * 试题总结统计接口
 * Created by shaojieyue
 * Created time 2016-05-31 20:51
 */

@RestController
@RequestMapping(value = "/v1/summary/question",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionSummaryControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(QuestionSummaryControllerV1.class);

    @Autowired
    private QuestionSummaryService questionSummaryService;

    @Autowired
    private UserSessionService userSessionService;

    /**
     * 查询用户指定科目下总体答题统计
     * @param token
     * @return
     */
    @RequestMapping(value = "",method = RequestMethod.GET)
    public Object get(@RequestHeader(required = false) String token) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        //科目
        int subject = userSessionService.getSubject(token);
        return questionSummaryService.findByUserId(userId,subject);
    }
}
