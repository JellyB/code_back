package com.huatu.ztk.paper.controller.v3;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.paper.bean.DayTrain;
import com.huatu.ztk.paper.bean.TrainPoint;
import com.huatu.ztk.paper.service.DayTrainService;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhouwei
 */
@RestController
@RequestMapping(value = "/v3/train", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DayTrainControllerV3 {
    private static final Logger logger = LoggerFactory.getLogger(DayTrainControllerV3.class);

    @Autowired
    private DayTrainService dayTrainService;

    @Autowired
    private UserSessionService userSessionService;


    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    /**
     * 每日训练
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

        /**
         * 修改返回名称为全字段 - lijun
         * 2018-01-25
         */
        Function<TrainPoint, TrainPoint> getTrainPointFullName = (TrainPoint trainPoint) -> {
            StringBuilder fullName = new StringBuilder();
            int questionPointId = trainPoint.getQuestionPointId();
            QuestionPoint questionPoint = questionPointDubboService.findById(questionPointId);
            if (null != questionPoint) {
                //三级
                fullName.append(questionPoint.getName());
                QuestionPoint secondLevelPoint = questionPointDubboService.findById(questionPoint.getParent());
                if (null != secondLevelPoint) {
                    //二级
                    fullName.insert(0, secondLevelPoint.getName() + "-");
                    QuestionPoint firstLevelPoint = questionPointDubboService.findById(secondLevelPoint.getParent());

                    //一级
                    if (null != firstLevelPoint) {
                        fullName.insert(0, firstLevelPoint.getName() + "-");
                    }
                }
            }
            trainPoint.setName(fullName.toString());
            return trainPoint;
        };
        List<TrainPoint> trainPoints = dayTrain.getPoints().stream().map(getTrainPointFullName).collect(Collectors.toList());
        dayTrain.setPoints(trainPoints);
        return dayTrain;
    }

}
