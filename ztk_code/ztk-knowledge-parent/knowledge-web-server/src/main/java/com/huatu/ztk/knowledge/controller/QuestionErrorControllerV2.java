package com.huatu.ztk.knowledge.controller;

import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.service.QuestionErrorService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v2/errors",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionErrorControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(QuestionErrorControllerV2.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private QuestionErrorService questionErrorService;

    /**
     * 查看错题列表
     * @param token
     * @param pointId 知识点
     * @return
     */
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public Object gets(@RequestHeader(required = false) String token,
                       @RequestParam int pointId) throws BizException {
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        PageBean<Integer> pageBean = questionErrorService.findByPoint(pointId, userId, -1);
        return pageBean;
    }
}
