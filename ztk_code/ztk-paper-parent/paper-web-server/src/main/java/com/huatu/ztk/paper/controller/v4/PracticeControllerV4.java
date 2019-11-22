package com.huatu.ztk.paper.controller.v4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.enums.CustomizeEnum;
import com.huatu.ztk.paper.service.PracticeCardService;
import com.huatu.ztk.paper.service.PracticeService;
import com.huatu.ztk.paper.service.v4.CustomizeService;
import com.huatu.ztk.paper.service.v4.PracticeCourseService;
import com.huatu.ztk.paper.service.v4.impl.AnswerCardUtil;
import com.huatu.ztk.paper.vo.PracticeReportVo;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import com.self.generator.core.WaitException;

/**
 * @author shanjigang
 * @date 2019/3/15 13:44
 */
@RequestMapping(value = "/v4/practice", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class PracticeControllerV4 {
    private static final Logger logger = LoggerFactory.getLogger(PracticeControllerV4.class);
    @Autowired
    private PracticeCourseService practiceCourseService;

    @Autowired
    private UserSessionService userSessionService;

    private static final boolean customizeFlag = false;
    private static final int errorMaxCount = 100;
    private static final String UN_PRACTICE = "(背题模式)";

    @Autowired
    private CustomizeService customizeService;

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private PracticeCardService practiceCardService;


    /**
     * 查看随堂练习报告
     *
     * @throws BizException
     */
    @RequestMapping(value = "/{courseId}/{videoType}/report", method = RequestMethod.GET)
    public Object getClassPracticeReport(@PathVariable long courseId,
                                         @PathVariable Integer videoType,
                                         //@RequestHeader(required = true) String token,
                                         //@RequestHeader(required = false) String cv,
                                         //@RequestHeader(required = false) int terminal,
                                         @RequestHeader(defaultValue = "-1") int subject,
                                         Long uid) throws BizException {
        //userSessionService.assertSession(token);
        //final long uid = userSessionService.getUid(token);
        logger.info("getClassPracticeReport userId={},courseId={}", uid, courseId);
        PracticeReportVo practiceReportVo = practiceCourseService.getPracticeReport(courseId, AnswerCardType.COURSE_BREAKPOINT, subject, uid, videoType);
        return practiceReportVo;
    }

    /**
     * 批量查询随堂练习报告状态
     *
     * @throws BizException
     */
    @RequestMapping(value = "/status/{userId}", method = RequestMethod.POST)
    public Object getPracticeReportStatus(@PathVariable("userId") Long userId, @RequestBody List<HashMap<String, Object>> practiceStatusList) throws BizException {
        return practiceCourseService.getBatchCoursePracticeStatus(userId, practiceStatusList);
    }


    /**
     * 专项训练（添加背题模式答题记录）
     *
     * @param pointId  知识点ID，默认不输入ID，则完全随机抽题
     * @param size     每次抽题的量
     * @param flag     1标识做题模式2标识背题模式
     * @param terminal 终端
     * @param token
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @RequestMapping(value = "customizes", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object customizes(@RequestParam Integer pointId,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "1") int flag,
                             @RequestHeader int terminal,
                             @RequestHeader(required = false) String token,
                             @RequestHeader(defaultValue = "-1") int subject,
                             HttpServletRequest httpServletRequest) throws WaitException, BizException {
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        StopWatch stopWatch = new StopWatch("专项训练任务");
        PracticePaper practicePaper;
        stopWatch.start("用户校验");
        logger.info("url={},params={}", httpServletRequest.getRequestURL().toString(),
        		JsonUtil.toJson(parameterMap));
        userSessionService.assertSession(token);
        try {
            CustomizeEnum.ModeEnum modeEnum = CustomizeEnum.ModeEnum.create(flag);
            // 用户id
            long userId = userSessionService.getUid(token);
            int headerSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
            stopWatch.stop();

            stopWatch.start("用户做题模式未完成答题卡");
            AnswerCard unFinishedCard = practiceService.findUnFinishedCardV2(pointId, userId, headerSubject, size,
                    modeEnum);
            stopWatch.stop();
            if (null != unFinishedCard) {
                return unFinishedCard;
            }

            if (customizeFlag) {
                stopWatch.start("创建试卷（新）");
                practicePaper = customizeService.createPracticePaper(pointId, size, userId, headerSubject, modeEnum);
                stopWatch.stop();
            } else {
                stopWatch.start("创建试卷（新）");
                practicePaper = practiceService.createPracticePaperV2(pointId, size, userId, headerSubject, modeEnum);
                stopWatch.stop();
            }
            if (practicePaper == null) {// 没有查到
                return CommonErrors.RESOURCE_NOT_FOUND;
            }
            int cardType = modeEnum == CustomizeEnum.ModeEnum.Write ? AnswerCardType.CUSTOMIZE_PAPER
                    : AnswerCardType.CUSTOMIZE_PAPER_RECITE;
            stopWatch.start("创建答题卡");
            final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, cardType, userId);
            stopWatch.stop();
            // 添加未完成练习id
            stopWatch.start("保存未完成答题卡");
            practiceCardService.addCustomizesUnfinishedIdV2(pointId, practiceCard, modeEnum);
            stopWatch.stop();
            AnswerCardUtil.fillIdStr(practiceCard);
            return practiceCard;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            logger.info(stopWatch.prettyPrint());
        }
        throw new BizException(ErrorResult.create(1000001, "抽题失败"));

    }

    /**
     * 错题重练
     *
     * @param pointId  知识点Id
     * @param terminal 终端
     * @param token
     * @param size     抽题量
     * @param flag     1抽题模式2背题模式
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "errors", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object errorTrain(@RequestParam(required = false, defaultValue = "-1") int pointId,
                             @RequestHeader int terminal,
                             @RequestHeader(required = false) String token,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "1") int flag,
                             @RequestHeader(defaultValue = "-1") int subject,
                             HttpServletRequest httpServletRequest) throws BizException {
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        logger.info("url={},params={}", httpServletRequest.getRequestURL().toString(), JsonUtil.toJson(parameterMap));
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        int finalSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
        int errorQcount = size;
        if (errorQcount < 0) {
            errorQcount = 10;
        } else if (errorQcount > errorMaxCount) {
            errorQcount = errorMaxCount;
        }
        CustomizeEnum.ModeEnum modeEnum = CustomizeEnum.ModeEnum.create(flag);
        String name = modeEnum == CustomizeEnum.ModeEnum.Look?"错题重练(背题模式)":"错题模式(做题模式)";
        final PracticePaper practicePaper = practiceService.createErrorQuestionPaperWithFlag(pointId, userId, finalSubject, errorQcount, flag, name);
        int cardType = modeEnum == CustomizeEnum.ModeEnum.Write ? AnswerCardType.WRONG_PAPER
                : AnswerCardType.WRONG_PAPER_RECITE;
        final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, cardType, userId);
        return practiceCard;
    }

}
