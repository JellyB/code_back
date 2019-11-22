package com.huatu.ztk.paper.controller.v3;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.TerminalType;
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
import com.huatu.ztk.paper.service.v4.impl.AnswerCardUtil;
import com.huatu.ztk.paper.util.TeacherSubjectManager;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/v3/practices", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PracticeControllerV3 {
    private static final Logger logger = LoggerFactory.getLogger(PracticeControllerV3.class);

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private CustomizeService customizeService;

    @Autowired
    private PracticeCardService practiceCardService;

    private static final int errorMaxCount = 100;
    private static final String UN_PRACTICE = "(背题模式)";

    private static final boolean customizeFlag = false;


    /**
     * 用于华图在线新版app
     * 查询用户设置的考试科目下的答题记录
     *
     * @param token
     * @param terminal
     * @param cursor
     * @param cardType
     * @param cardTime
     * @param removeEstimate 是否移除模考估分的答题记录
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object queryCards(@RequestHeader(required = false) String token,
                             @RequestHeader int terminal,
                             @RequestParam(defaultValue = Long.MAX_VALUE + "") long cursor,
                             @RequestParam(defaultValue = "0") int cardType,
                             @RequestParam(defaultValue = "") String cardTime,
                             @RequestParam(defaultValue = "true") boolean removeEstimate,
                             @RequestHeader(defaultValue = "-1") int subject) throws WaitException, BizException {

        userSessionService.assertSession(token);

        if (cursor < 1) {//说明查询第一页，那么，cursor设置为最大值
            cursor = Long.MAX_VALUE;
        }
        //用户id
        long userId = userSessionService.getUid(token);

        int headerSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);

        List<Integer> subjectIds = Lists.newArrayList();
        subjectIds.add(headerSubject);
        int newSubject = userSessionService.convertChildSubjectToParentSubject(headerSubject);
        if (subject != newSubject) {
            subjectIds.add(newSubject);
        }
        TeacherSubjectManager.fillTeacherSubject(subjectIds);
        logger.info("用户答题记录,subjectIds={}", subjectIds);
        final PageBean pageBean = practiceService.findCards(userId, null,
                cursor, 20, cardType, cardTime, removeEstimate, subjectIds.stream().distinct().collect(Collectors.toList()));

        //小程序 && 精准估分,需要添加估分人数 和 icon
        if (terminal == TerminalType.WEI_XIN_APPLET && cardType == AnswerCardType.ESTIMATE) {
            return practiceService.dealSmallRoutLine(pageBean, subject);
        }
        return pageBean;
    }

    /**
     * 用于pc端的分页查询
     * 查询用户设置的考试科目下的答题记录
     *
     * @param token
     * @param terminal
     * @param page
     * @param cardType
     * @param cardTime
     * @param removeEstimate 是否移除模考估分的答题记录
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @RequestMapping(value = "page", method = RequestMethod.GET)
    public Object queryCardsByPage(@RequestHeader(required = false) String token,
                                   @RequestHeader int terminal,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "16") int size,
                                   @RequestParam(defaultValue = "0") int cardType,
                                   @RequestParam(defaultValue = "") String cardTime,
                                   @RequestParam(defaultValue = "-1") int status,
                                   @RequestParam(defaultValue = "true") boolean removeEstimate) throws WaitException, BizException {

        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);


        final int subject = userSessionService.getSubject(token);

        final PageBean pageBean = practiceService.findCardsByPage(userId, null,
                page, size, cardType, cardTime, removeEstimate, subject, status);

        return pageBean;
    }

    @RequestMapping(value = "cardType", method = RequestMethod.GET)
    public Object getAnswerCardType(@RequestHeader(required = false) String token,
                                    @RequestHeader(defaultValue = "-1") int subject) throws BizException {
        userSessionService.assertSession(token);
        int finalSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
        Function<List<Integer>, List<Map>> fun = type -> {
            List<Map> result = Lists.newArrayList();
            for (Integer id : type) {
                Map mapData = Maps.newHashMap();
                mapData.put("typeId", id);
                mapData.put("typeName", AnswerCardType.getSelectName(id));
                result.add(mapData);
            }
            return result;
        };
        if (finalSubject == SubjectType.GWY_XINGCE) {     //公务员行测
            return fun.apply(Arrays.asList(AnswerCardType.SELECT_GWX));
        } else if (finalSubject == SubjectType.SYDW_GONGJI) {         //事业单位公基
            return fun.apply(Arrays.asList(AnswerCardType.SELECT_SYDW));
        } else if (finalSubject == SubjectType.SYDW_XINGCE) {
            return fun.apply(Arrays.asList(AnswerCardType.SELECT_SYDW_ZC));
        } else {                                      //其他类型
            return fun.apply(Arrays.asList(AnswerCardType.SELECT_DEF));
        }
    }

    /**
     * 专项训练（添加抽题模式，和根据抽题量，做抽题逻辑）
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
        try {
            stopWatch.start("用户校验");
            logger.info("url={},params={}", httpServletRequest.getRequestURL().toString(), JsonUtil.toJson(parameterMap));
            userSessionService.assertSession(token);
            CustomizeEnum.ModeEnum modeEnum = CustomizeEnum.ModeEnum.create(flag);
            //用户id
            long userId = userSessionService.getUid(token);
            int headerSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
            stopWatch.stop();
            if (modeEnum == CustomizeEnum.ModeEnum.Write) {
                stopWatch.start("用户做题模式未完成答题卡");
                AnswerCard unFinishedCard = practiceService.findUnFinishedCard(pointId, userId, headerSubject, size);
                stopWatch.stop();
                if (null != unFinishedCard) {
                    return unFinishedCard;
                }
            }

            if (customizeFlag) {
                stopWatch.start("创建试卷（新）");
                practicePaper = customizeService.createPracticePaper(pointId, size, userId, headerSubject, modeEnum);
                stopWatch.stop();
            } else {
                stopWatch.start("创建试卷（新）");
                practicePaper = practiceService.createPracticePaper(pointId, size, userId, headerSubject);
                stopWatch.stop();
            }
            if (practicePaper == null) {//没有查到
                return CommonErrors.RESOURCE_NOT_FOUND;
            }
            if (modeEnum == CustomizeEnum.ModeEnum.Write) {
                stopWatch.start("创建答题卡");
                final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.CUSTOMIZE_PAPER, userId);
                stopWatch.stop();
                //添加未完成练习id
                stopWatch.start("保存未完成答题卡");
                practiceCardService.addCustomizesUnfinishedId(pointId, practiceCard);
                stopWatch.stop();
                AnswerCardUtil.fillIdStr(practiceCard);
                return practiceCard;
            }
            practicePaper.setName(practicePaper.getName() + UN_PRACTICE);
            return practicePaper;
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
        final PracticePaper practicePaper = practiceService.createErrorQuestionPaperWithFlag(pointId, userId, finalSubject,
                errorQcount,
                flag,
                AnswerCardType.recordType.get(AnswerCardType.WRONG_PAPER));
        if (1 == flag) {
            final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.WRONG_PAPER, userId);
            return practiceCard;
        }
        practicePaper.setName(practicePaper.getName() + UN_PRACTICE);
        return practicePaper;
    }
}
