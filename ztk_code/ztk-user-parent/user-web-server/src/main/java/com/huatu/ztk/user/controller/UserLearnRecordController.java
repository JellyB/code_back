package com.huatu.ztk.user.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.service.LearnRecordService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-23  14:58 .
 */
@RestController
@RequestMapping(value = "/v1/users/record", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserLearnRecordController {
    private static final Logger logger = LoggerFactory.getLogger(UserPostAddressController.class);

    @Autowired
    private LearnRecordService learnRecordService;

    @Autowired
    private UserSessionService userSessionService;

    /**
     * 获得用户各学科最后一次学习记录；
     * @param token
     * @return
     */
    @RequestMapping(value = "/findLearnRecord", method = RequestMethod.GET)
    public Object findTeacherByName(@RequestParam String token) throws BizException {
        logger.info("token={}",token);
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        return learnRecordService.findLearnRecord(userId);
    }
}
