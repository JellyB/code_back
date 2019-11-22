package com.huatu.ztk.paper.controller.v4;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.service.mock.SmallPaperMockServiceImpl;
import com.huatu.ztk.paper.service.v4.SmallEstimateService;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小模考功能实现
 * Created by huangqingpeng on 2019/2/13.
 */
@RestController
@RequestMapping(value = "/v4/small/estimate", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class EstimatePaperController {

    private final static Logger logger = LoggerFactory.getLogger(EstimatePaperController.class);

    private final static boolean mockFlag = false;
    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    SmallEstimateService smallEstimateService;

    @Autowired
    SmallPaperMockServiceImpl mockDataService;


    /**
     * 小模考首页数据加载
     * --1、只有一场模考--2、一天换一场--3、只展示当天的小模考--4、无小模考的直接返回空（是报错数据吗？）
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getEstimateHeaderPage(@RequestHeader(required = true) String token,
                                        @RequestHeader(defaultValue = "1") String cv,
                                        @RequestHeader(defaultValue = "0") int terminal,
                                        @RequestHeader(defaultValue = "-1") int subject) throws BizException {

        logger.info("getEstimateHeaderPage's params: token={},cv={},termnal={},subject={}", token, cv, terminal, subject);
        userSessionService.assertSession(token);
        if (subject == -1) {
            subject = userSessionService.getSubject(token);
        }
        long uid = userSessionService.getUid(token);
        if (mockFlag) {
            return mockDataService.getEstimateHeaderPage(subject, uid);
        }
        return smallEstimateService.findTodaySmallEstimateInfo(subject, uid);
    }

    /**
     * 小模考报告数据加载
     * --1、只有一场模考--2、一天换一场--3、只展示当天的小模考--4、无小模考的直接返回空（是报错数据吗？）
     *
     * @return
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public Object getEstimateReportPage(@RequestHeader(required = true) String token,
                                        @RequestHeader(defaultValue = "1") String cv,
                                        @RequestHeader(defaultValue = "0") int terminal,
                                        @RequestHeader(defaultValue = "-1") int subject,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(defaultValue = "-1") long startTime,
                                        @RequestParam(defaultValue = "-1") long endTime) throws BizException {

        logger.info("getEstimateReportPage's params: token={},cv={},terminal={},startTime={},endTime={},subject={},page={},size={}", token, cv, terminal, startTime, endTime, subject, page, size);
        userSessionService.assertSession(token);
        if (subject == -1) {
            subject = userSessionService.getSubject(token);
        }
        long uid = userSessionService.getUid(token);
        if (mockFlag) {
            return mockDataService.getEstimateReportPage(subject, uid);
        }
        return smallEstimateService.getEstimateReportPage(subject,uid,startTime,endTime,page,size);
    }

    /**
     * 创建小模考答题卡|查询答题卡（开始考试和继续考试）
     *
     * @param id
     * @param token
     * @param cv
     * @param terminal
     * @param subject
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @RequestMapping(value = "/papers", method = RequestMethod.POST)
    public Object createSmallEstimateAnswerCard(@RequestParam int id,
                                                @RequestHeader(required = true) String token,
                                                @RequestHeader(defaultValue = "1") String cv,
                                                @RequestHeader(defaultValue = "0") int terminal,
                                                @RequestHeader(defaultValue = "-1") int subject) throws WaitException, BizException {
        logger.info("createSmallEstimateAnswerCard's params: paperId={},token={},cv={},termnal={},subject={}", id, token, cv, terminal, subject);
        userSessionService.assertSession(token);
        if (subject == -1) {
            subject = userSessionService.getSubject(token);
        }
        long userId = userSessionService.getUid(token);
        if (mockFlag) {
            StandardCard answerCard = mockDataService.createAnswerCard(id, subject, userId, terminal);
            return answerCard;
        }
        final StandardCard practicePaper = smallEstimateService.create(id, subject, userId, terminal);
        if (practicePaper == null) {//资源不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        return practicePaper;
    }

    /**
     * 查询报告有两种情况
     * 1、答题卡状态未交卷，触发交卷操作，然后获得报告
     * 2、答题卡状态已交卷，直接返回报告
     *
     * @param token
     * @param cv
     * @param terminal
     * @param practiceId
     * @return
     */
    @RequestMapping(value = "practice/{practiceId}", method = RequestMethod.GET)
    public Object getAnswerCardInfo(@RequestHeader(required = true) String token,
                                    @RequestHeader(defaultValue = "1") String cv,
                                    @RequestHeader(defaultValue = "0") int terminal,
                                    @PathVariable Long practiceId) throws BizException {
        logger.info("getAnswerCardInfo's params: practiceId={},token={},cv={},termnal={}", practiceId, token, cv, terminal);
        userSessionService.assertSession(token);

        long userId = userSessionService.getUid(token);
        if (mockFlag) {
            int subject = userSessionService.getSubject(token);
            AnswerCard answerCard = mockDataService.getAnswerCard(practiceId, userId, terminal, subject);
            return answerCard;
        }
        return smallEstimateService.findAnswerCardDetail(practiceId, userId, terminal,cv);
    }

    /**
     * 提交试卷
     *
     * @return
     */
    @RequestMapping(value = "/{practiceId}", method = RequestMethod.POST)
    public AnswerCard submitAnswerCard(@RequestHeader(required = true) String token,
                                       @RequestHeader(defaultValue = "1") String cv,
                                       @RequestHeader(defaultValue = "0") int terminal,
                                       @PathVariable Long practiceId,
                                       @RequestBody List<Answer> answers) throws BizException {
        logger.info("submitAnswerCard's params: practiceId={},token={},cv={},terminal={},answers={}", practiceId, token, cv, terminal, JsonUtil.toJson(answers));
        userSessionService.assertSession(token);

        long userId = userSessionService.getUid(token);
        if (mockFlag) {
            int subject = userSessionService.getSubject(token);
            AnswerCard answerCard = mockDataService.getAnswerCard(practiceId, userId, terminal, subject);
            return answerCard;
        }
        int area = userSessionService.getArea(token);
        return smallEstimateService.submitAnswer(practiceId,userId,answers,area,terminal,cv);
    }

    /**
     * 5题以一保存试题
     *
     * @return
     */
    @RequestMapping(value = "/{practiceId}/answer", method = RequestMethod.PUT)
    public Object saveUserAnswers(@RequestHeader(required = true) String token,
                                  @RequestHeader(defaultValue = "1") String cv,
                                  @RequestHeader(defaultValue = "0") int terminal,
                                  @PathVariable Long practiceId,
                                  @RequestBody List<Answer> answers) throws BizException {
        logger.info("saveUserAnswers's params: practiceId={},token={},cv={},terminal={},answers={}", practiceId, token, cv, terminal, JsonUtil.toJson(answers));
        userSessionService.assertSession(token);

        long userId = userSessionService.getUid(token);
        if (!mockFlag) {
            smallEstimateService.saveAnswers(practiceId, userId, answers);
        }
        return answers;
    }
}
