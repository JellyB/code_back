package com.huatu.ztk.question.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.question.bean.QuestionAdvice;
import com.huatu.ztk.question.service.QuestionAdviceService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 纠错控制层
 * Created by shaojieyue
 * Created time 2016-07-22 09:30
 */


@RestController
@RequestMapping(value = "/v1/questions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionAdviceController {
    private final static Logger logger = LoggerFactory.getLogger(QuestionAdviceController.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private QuestionAdviceService questionAdviceService;

    /**
     * 用户提交纠错内容
     */
    @RequestMapping(value = "/{qid}/advice", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object create(@RequestHeader(required = false) String token,
                         @PathVariable int qid,
                         @RequestBody QuestionAdvice questionAdvice) throws BizException {
        logger.info("参数:token->{},qid->{},questioinAdvice->{}", token, qid, questionAdvice);
        userSessionService.assertSession(token);

        long userId = userSessionService.getUid(token);
        int area = userSessionService.getArea(token);
        questionAdvice.setUserArea(area);
        questionAdvice.setQid(qid);
        questionAdvice.setUid(userId);

        questionAdviceService.create(questionAdvice);

        return SuccessMessage.create("您的纠错建议提交成功");
    }
}
