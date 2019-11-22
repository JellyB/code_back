package com.huatu.ztk.paper.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.DayTrain;
import com.huatu.ztk.paper.service.DayTrainService;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/v2/train", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DayTrainControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(DayTrainControllerV2.class);

    @Autowired
    private DayTrainService dayTrainService;

    @Autowired
    private UserSessionService userSessionService;

    /**
     * 第二版通过token属性来确认subject的默认属性（但不对银行相关的科目做特殊指定了）
     *
     * @param token
     * @param subject
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @RequestMapping(method = RequestMethod.GET)
    public Object get(@RequestHeader(required = false) String token,
                      @RequestParam(defaultValue = "-1") int subject) throws WaitException, BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);

        if (subject < 0) {
            subject = userSessionService.getSubject(token);
        }

        DayTrain dayTrain = dayTrainService.findDayTrain(userId, subject);
        return dayTrain;
    }

}
