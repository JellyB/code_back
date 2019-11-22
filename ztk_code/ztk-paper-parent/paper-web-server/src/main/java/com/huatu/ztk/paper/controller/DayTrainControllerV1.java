package com.huatu.ztk.paper.controller;

import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.paper.bean.DayTrain;
import com.huatu.ztk.paper.service.DayTrainService;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 每日特训相关接口
 * Created by shaojieyue
 * Created time 2016-05-20 22:13
 */

@RestController
@RequestMapping(value = "/v1/train",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DayTrainControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(DayTrainControllerV1.class);

    @Autowired
    private DayTrainService dayTrainService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    /**
     *
     * @param terminal
     * @param token
     * @param subject  科目
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @RequestMapping(method = RequestMethod.GET)
    public Object get(@RequestHeader int terminal,
                      @RequestHeader(required = false) String token,
                      @RequestParam(defaultValue = SubjectType.GWY_XINGCE + "") int subject) throws WaitException, BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);
        //如果是银行招聘和农信社的科目都直接归为“中国银行科目"
        subject = subjectDubboService.getBankSubject(subject);

        DayTrain dayTrain = dayTrainService.findCurrent(userId, subject);
        if (dayTrain == null) {
            dayTrain = dayTrainService.create(userId, subject);
        }
        return dayTrain;
    }

    /**
     * 获取要训练的知识点
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/smallRoutine",method = RequestMethod.GET)
    public Object getRandomPoints(@RequestParam(defaultValue = "5") int num,
                                  @RequestParam(defaultValue = SubjectType.GWY_XINGCE + "") int subject) throws BizException {
        DayTrain dayTrain=dayTrainService.getRandomPoints(num,subject);
        return dayTrain;
    }

}
