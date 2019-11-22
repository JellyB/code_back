package com.huatu.ztk.paper.controller;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.paper.bean.DayTrainSettings;
import com.huatu.ztk.paper.service.DayTrainSettingsService;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 每日训练配置
 * Created by shaojieyue
 * Created time 2016-05-20 16:24
 */

@RestController
@RequestMapping(value = "/v1/train/settings",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DayTrainSettingsControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(DayTrainSettingsControllerV1.class);

    @Autowired
    private DayTrainSettingsService dayTrainSettingsService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    /**
     * 获取每日训练设置
     * @return
     * @throws WaitException
     */
    @RequestMapping(method = RequestMethod.GET)
    public Object get(@RequestHeader(required = false) String token,
                      @RequestParam(defaultValue = SubjectType.GWY_XINGCE + "") int subject) throws WaitException,BizException {
        long stime0 = System.currentTimeMillis();
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        logger.info("get dayTrainSettings,time={},token={}", System.currentTimeMillis() - stime0,token);

        subject = subjectDubboService.getBankSubject(subject);

        long stime = System.currentTimeMillis();
        //获取用户的每日特训设置参数（携带顶级知识点信息）
        DayTrainSettings dayTrainSettings = dayTrainSettingsService.findByUserId(userId,subject);
        logger.info("find DayTrainSettings time={},token={}", System.currentTimeMillis() - stime, token);
        if (dayTrainSettings == null) {
            long stime1 = System.currentTimeMillis();
            //如果用户无每日特训设置,创建每日特训设置参数
            dayTrainSettings = dayTrainSettingsService.create(userId,subject);
            logger.info("create DayTrainSettings utime={},token={}", System.currentTimeMillis() - stime1, token);
        }
        return dayTrainSettings;
    }

    /**
     * 更新每日训练设置
     * @param body
     */
    @RequestMapping(method = RequestMethod.PUT)
    public Object update(@RequestBody String body,
                         @RequestHeader(required = false) String token,
                         @RequestParam(defaultValue = SubjectType.GWY_XINGCE + "") int subject) throws BizException {
        final DayTrainSettings dayTrainSettings = JsonUtil.toObject(body, DayTrainSettings.class);
        userSessionService.assertSession(token);
        //用户id
        final long uid = userSessionService.getUid(token);

        subject = subjectDubboService.getBankSubject(subject);

        final DayTrainSettings update = dayTrainSettingsService.update(dayTrainSettings,uid,subject);
        return update;
    }
}
