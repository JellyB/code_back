package com.huatu.ztk.paper.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.service.PaperAnswerCardUtilComponent;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/10/18
 * @描述
 */

@RestController
@RequestMapping(value = "estimate", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class EstimateController {

    private static final Logger logger = LoggerFactory.getLogger(PaperAnswerCardUtilComponent.class);

    @Autowired
    UserSessionService userSessionService;

    @Autowired
    PaperAnswerCardUtilComponent paperAnswerCardUtilComponent;

    /**
     * 精准估分,显示活动公共信息
     * type 考试类型
     *
     * @return
     */
    @RequestMapping(value = "estimateInfo", method = RequestMethod.GET)
    public Object getEstimateInfo(@RequestParam int type,
                                  @RequestHeader(defaultValue = "-1") int subject,
                                  //跨科目跳转时 传入的subject
                                  @RequestParam(defaultValue = "-1") int subjectId,
                                  @RequestHeader(required = false, defaultValue = "") String token) throws BizException {
        long start = System.currentTimeMillis();
        int subject_ = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subjectId, subject);
        HashMap map = paperAnswerCardUtilComponent.dealEstimateIsShow(subject_, type);
        long totalTime = System.currentTimeMillis() - start;
        logger.info("查询活动介绍用时:{}", totalTime);
        return map;
    }


    /**
     * 精准估分活动介绍
     *
     * @return
     */
    @RequestMapping(value = "estimateActivityInfo", method = RequestMethod.GET)
    public Object getEstimateActivityInfo(@RequestHeader(required = false) String token,
                                          @RequestParam int category) throws BizException {
        return paperAnswerCardUtilComponent.dealEstimateIsShow(category, AnswerCardType.ESTIMATE);
    }

    /**
     * 模考大赛活动介绍
     *
     * @return
     */
    @RequestMapping(value = "matchActivityInfo", method = RequestMethod.GET)
    public Object getMatchActivityInfo(@RequestHeader(required = false) String token,
                                       @RequestParam int category) throws BizException {

        return paperAnswerCardUtilComponent.dealEstimateIsShow(category, AnswerCardType.MATCH);
    }
}
