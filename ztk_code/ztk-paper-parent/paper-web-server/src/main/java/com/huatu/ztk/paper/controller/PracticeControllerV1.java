package com.huatu.ztk.paper.controller;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.common.SmartErrors;
import com.huatu.ztk.paper.service.*;
import com.huatu.ztk.paper.service.v4.impl.AnswerCardUtil;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huatu.ztk.paper.service.PaperAnswerCardService.sortPointTree;

/**
 * 练习控制层/模考大赛交卷
 * Created by shaojieyue
 * Created time 2016-04-29 14:09
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/v1/practices", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PracticeControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(PracticeControllerV1.class);

    @Autowired
    private PaperService paperService;

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private PracticeCardService practiceCardService;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private DayTrainService dayTrainService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private PaperRewardService paperRewardService;

    @Autowired
    private SensorsService sensorsService;


    /**
     * 查询我的练习记录
     *
     * @param token
     * @param terminal
     * @param cursor
     * @param cardType
     * @param cardTime
     * @return
     * @throws WaitException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object queryCards(@RequestHeader(required = false) String token, @RequestHeader int terminal,
                             @RequestParam(defaultValue = Long.MAX_VALUE + "") long cursor,
                             @RequestParam(defaultValue = "0") int cardType,
                             @RequestParam(defaultValue = "") String cardTime) throws WaitException, BizException {

        userSessionService.assertSession(token);

        if (cursor < 1) {//说明查询第一页，那么，cursor设置为最大值
            cursor = Long.MAX_VALUE;
        }

        //用户id
        long userId = userSessionService.getUid(token);
        //获取科目
        final int catgory = userSessionService.getCatgory(token);
        final PageBean pageBean = practiceService.findCards(userId, Arrays.asList(catgory), cursor, 20,
                cardType, cardTime, false, Lists.newArrayList(-1));

        return pageBean;
    }

    /**
     * 根据id查询答题卡信息
     * update by lizhenjuan
     *
     * @param token
     * @param id    练习id
     * @return
     * @throws BizException postman
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Object get(@RequestHeader(required = false) String token,
                      @RequestHeader(required = false) int terminal,
                      @RequestHeader(defaultValue = "1") String cv,
                      @PathVariable long id) throws BizException {
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);
        return paperAnswerCardService.findAnswerCardDetail(id, uid, terminal, cv);
    }

    /**
     * 删除单条答题记录
     *
     * @param token
     * @param id    练习id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Object delete(@RequestHeader(required = false) String token,
                         @PathVariable long id) throws BizException {
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);
        paperAnswerCardService.deleteById(id, uid);
        return SuccessMessage.create("删除成功");
    }

    @RequestMapping(value = "userMeta/{id}", method = RequestMethod.DELETE)
    public Object userMetaDelete(@PathVariable String id) throws BizException {
        paperUserMetaService.delete(id);
        return SuccessMessage.create("删除成功");
    }

    /**
     * 真题模考
     * 专项模考 精准估分  真题试卷 创建答题卡接口
     * 2019-04-21 添加小程序试卷的答题卡创建逻辑
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/papers", method = RequestMethod.POST)
    public Object create(@RequestParam int id,
                         @RequestHeader(required = false) String token,
                         @RequestHeader int terminal,
                         @RequestHeader(defaultValue = "-1") int subject) throws WaitException, BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);
        //知识点类目
        int headerSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
        final Paper paper = paperService.findById(id);
        logger.info("创建答题卡,科目是:{}", headerSubject);
        final StandardCard practicePaper = paperAnswerCardService.create(paper, headerSubject, userId, terminal);

        if (practicePaper == null) {//资源不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        paperUserMetaService.addUndoPractice(userId, id, practicePaper.getId());
        return practicePaper;
    }

    /**
     * 智能出题
     *
     * @return
     */
    @RequestMapping(value = "smarts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object smarts(@RequestParam(defaultValue = "10") int size,
                         @RequestHeader int terminal,
                         @RequestHeader(required = false) String token,
                         @RequestHeader(defaultValue = "-1") int subject) throws WaitException, BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);

        int headerSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);

        int finalSubject = userSessionService.convertChildSubjectToParentSubject(headerSubject);
        logger.info("智能刷题科目是:{}", finalSubject);
        PracticePaper practicePaper = practiceService.createSmartPaper(size, userId, finalSubject);
        if (practicePaper == null) {//没有查到
            return SmartErrors.NO_QUESTION;
        }
        final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.SMART_PAPER, userId);
        return practiceCard;
    }

    /**
     * 专项训练
     *
     * @param pointId 知识点id
     * @return
     */
    @RequestMapping(value = "customizes", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object customizes(@RequestParam Integer pointId, @RequestParam(defaultValue = "10") int size,
                             @RequestHeader int terminal, @RequestHeader(required = false) String token) throws WaitException, BizException {
        long stime = System.currentTimeMillis();
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        logger.info("customizes， user sesseion time={},token={}", System.currentTimeMillis() - stime, token);

        long stime1 = System.currentTimeMillis();

        //事业单位,使用用户设置的专项练习题量
        int qcount = userSessionService.getQcount(token);
        //考试类型-->app类型
        int catgory = subjectDubboService.getCatgoryBySubject(subject);
        if (catgory == CatgoryType.SHI_YE_DAN_WEI && qcount > 0) {
            size = qcount;
        }
        AnswerCard unFinishedCard = practiceService.findUnFinishedCard(pointId, userId, subject, size);
        if (null != unFinishedCard) {
            return unFinishedCard;
        }
        final PracticePaper practicePaper = practiceService.createPracticePaper(pointId, size, userId, subject);
        logger.info("create customizes paper time={},token={}", System.currentTimeMillis() - stime1, token);
        if (practicePaper == null) {//没有查到
            return CommonErrors.RESOURCE_NOT_FOUND;
        }

        long t1 = System.currentTimeMillis();
        final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.CUSTOMIZE_PAPER, userId);
        logger.info("create customizes practiceCard expend time={},token={}", System.currentTimeMillis() - t1, token);
        logger.info(JsonUtil.toJson(practiceCard));
        AnswerCardUtil.fillIdStr(practiceCard);
        return practiceCard;
    }

    /**
     * 每日训练
     *
     * @param pointId 知识点id
     * @return
     */
    @RequestMapping(value = "daytrain", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object daytrain(@RequestParam Integer pointId, @RequestHeader int terminal, @RequestHeader(required = false) String token) throws WaitException, BizException {
        long stime0 = System.currentTimeMillis();
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        logger.info("daytrain, user sesseion time={},token={}", System.currentTimeMillis() - stime0, token);
        //查询用户当天的训练点

        subject = subjectDubboService.getBankSubject(subject);

        final DayTrain dayTrain = dayTrainService.findCurrent(userId, subject);
        long stime = System.currentTimeMillis();
        final PracticePaper practicePaper = practiceService.createDayTrainPaper(userId, pointId, dayTrain.getQuestionCount(), subject);
        logger.info("create daytrain PracticePaper time={},token={}", System.currentTimeMillis() - stime, token);
        if (practicePaper == null) {//没有查到
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        long stime2 = System.currentTimeMillis();
        final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.DAY_TRAIN, userId);
        logger.info("create daytrain practiceCard time={},token={}", System.currentTimeMillis() - stime2, token);
        //更新训练点

        long stime3 = System.currentTimeMillis();
        dayTrainService.addTrainPractice(userId, pointId, practiceCard.getId(), subject);
        logger.info("addTrainPractice time={},token={}", System.currentTimeMillis() - stime3, token);
        return practiceCard;
    }

    /**
     * 错题重练
     *
     * @param pointId  知识点id
     * @param terminal 终端
     * @param token
     * @return
     */
    @RequestMapping(value = "errors", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object errorTrain(@RequestParam(required = false, defaultValue = "-1") int pointId,
                             @RequestHeader int terminal,
                             @RequestHeader(required = false) String token) throws WaitException, BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        final PracticePaper practicePaper = practiceService.createErrorQuestionPaper(pointId, userId, subject, 10);
        final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.WRONG_PAPER, userId);
        return practiceCard;
    }

    /**
     * 收藏练习
     *
     * @param pointId
     * @param terminal
     * @param token
     * @return
     * @throws WaitException
     */
    @RequestMapping(value = "collects", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object collectTrain(@RequestParam(required = false, defaultValue = "-1") int pointId, @RequestHeader int terminal, @RequestHeader(required = false) String token) throws WaitException, BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        final PracticePaper practicePaper = practiceService.createCollectQuestionPaper(pointId, userId, subject);
        final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.COLLECT_TRAIN, userId);
        return practiceCard;
    }

    /**
     * 全部做题的保存答案接口 5题提交一次
     *
     * @param practiceId 答题卡Id
     * @param answers
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "{practiceId}/answers", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object submitAnswers(@RequestHeader(required = false) String token, @PathVariable long practiceId,
                                @RequestBody List<Answer> answers,
                                @RequestParam(defaultValue = "-1") int time,
                                @RequestHeader int terminal,
                                @RequestHeader(defaultValue = "-1") int subject) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int area = userSessionService.getArea(token);
        paperAnswerCardService.submitAnswers(practiceId, userId, answers, area, false);


        Map map = new HashMap(4);
        map.put("message", "答案保存成功");
        return map;
    }

    /**
     * 提交答题卡（专项练习，错题练习，每日特训，智能出题，真题试卷）
     *
     * @param practiceId
     * @param answers
     * @return
     * @throws BizException
     */
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(value = "/{practiceId}", method = RequestMethod.PUT, consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object submitPractice(@RequestHeader(required = false) String token,
                                 @PathVariable long practiceId,
                                 @RequestBody List<Answer> answers,
                                 @RequestParam(defaultValue = "-1") int time,
                                 @RequestHeader int terminal,
                                 @RequestHeader(defaultValue = "1") String cv) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int area = userSessionService.getArea(token);
        final AnswerCard answerCard = paperAnswerCardService.submitPractice(practiceId, userId, answers, area, terminal, cv);


        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            int paperTime = paper.getTime();
            //学员答题用时，为各题答题时间累计求和
            int[] times = answerCard.getTimes();
            time = 0;
            for (int index = 0; index < times.length; index++) {
                time += times[index];
            }
            //如果总用时大于试卷总时长，修正答题时间为试卷总时长
            if (time > paperTime) {
                time = paperTime;
            }
            answerCard.setExpendTime(time);
            answerCard.setRemainingTime(paperTime - time);
        }
        paperRewardService.sendSubmitPracticeMsg(userId, userSessionService.getUname(token), answerCard);
        return answerCard;
    }


    /**
     * 模考估分、大赛 交卷
     * lzj 安卓提交精准估分答题
     *
     * @param token
     * @param practiceId
     * @param answers
     * @param terminal
     * @param cardType   答题卡类型
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "estimate/{practiceId}", method = RequestMethod.PUT, consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object submitEstimatePaper(@RequestHeader(required = false) String token,
                                      @PathVariable long practiceId,
                                      @RequestBody List<Answer> answers,
                                      @RequestHeader int terminal,
                                      @RequestHeader(defaultValue = "1") String cv,
                                      @RequestParam(defaultValue = "-1") int time,
                                      @RequestParam(defaultValue = "-1") int cardType) throws BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        final int area = userSessionService.getArea(token);
        String uname = userSessionService.getUname(token);

        Object result = paperAnswerCardService.submitEstimateAnswers(practiceId, userId, answers, area, uname, cardType, terminal, cv);
        /**
         * pc端总用时使用time字段
         */
        if (-1 != time && result instanceof StandardCard) {
            int expendTime = ((StandardCard) result).getExpendTime();
            int remainingTime = ((StandardCard) result).getRemainingTime();
            int totalTime = expendTime + remainingTime;
            ((StandardCard) result).setExpendTime(time);
            ((StandardCard) result).setRemainingTime(totalTime - time);
        }
        /**
         * 此处处理答题卡信息 中如果有知识点信息 且有试题列表的情况下,知识点信息和试题列表中一级标题顺序不一致
         * update by lijun 2018-05-30
         */
        if (null != result && result instanceof AnswerCard) {
            sortPointTree((AnswerCard) result);
        }
        if (cardType == AnswerCardType.MATCH && result instanceof StandardCard) {
            // add sensors analytics
            // sensorsService.submitEstimatePaperAnalytics(token, practiceId, userId, area, uname, cardType,
            // (int) SensorsUtils.getMessage().get("paperId"), ((StandardCard) result).getExpendTime(), terminal);
        }
        return result;
    }

    /**
     * 微信小程序获取知识点下试题
     *
     * @param size
     * @param pointId
     * @return
     */
    @RequestMapping(value = "smallRoutine", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getQuestionByPointId(@RequestParam(defaultValue = "10") int size, @RequestParam int pointId) {
        return practiceService.getQuestionByPointId(size, pointId);
    }

    /**
     * 创建模考大赛答题卡
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/matches", method = RequestMethod.POST)
    public Object createMatchAnswerCard(@RequestParam int id, @RequestHeader(required = false) String token, @RequestHeader int terminal) throws WaitException, BizException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        logger.info("【REQUEST】招警机考创建答题卡。userId={},subject={},terminal={},paperId={}", userId, subject, terminal, id);

//        if(100100173 == subject){
//            try {
//                Thread.sleep(22*1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        StandardCard practice = matchService.createPractice(id, subject, userId, terminal);
        logger.info("【RESPONSE】招警机考创建答题卡。接口响应用时={}, practice={}", String.valueOf(stopwatch.stop()), practice);
        // add sensors analytics
        // sensorsService.createMatchAnswerCardAnalytics(token, id, subject, terminal, practice);
        return practice;

    }


    /**
     * 更新用户各模块作答情况，返回答题卡详情
     *
     * @param id
     * @param category
     * @param status   对应的各个状态详见 ModuleAnswerStatus
     * @param token
     * @param terminal
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/module", method = RequestMethod.POST)
    public Object upModuleStatus(@RequestParam long id,
                                 @RequestParam String category,
                                 @RequestParam int status,
                                 @RequestHeader(required = false) String token,
                                 @RequestHeader int terminal,
                                 @RequestHeader(defaultValue = "1") String cv) throws BizException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);

        logger.info("【REQUEST】招警机考更新模块状态。userId={},terminal={},practiceId={},module={},status={}", userId, terminal, id, category, status);

        //1。更新答题卡各模块完成情况
        paperAnswerCardService.upModuleStatus(id, userId, Integer.parseInt(category), status);
        //2。返回答题卡详情
        AnswerCard answerCardDetail = paperAnswerCardService.findAnswerCardDetail(id, userId, terminal, cv);
        logger.info("【RESPONSE】招警机考更新模块状态。接口响应用时={}, answerCardDetail={}", String.valueOf(stopwatch.stop()), answerCardDetail);
        return answerCardDetail;
    }


    /**
     * 全部做题的保存答案接口 5题提交一次
     * 修改原因：招警机考web端不支持超过19位的整型数据（将答题卡id用String接收）
     *
     * @param practiceId 答题卡Id
     * @param answers
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "pc/{practiceId}/answers", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object pcSubmitAnswers(@RequestHeader(required = false) String token,
                                  @PathVariable String practiceId,
                                  @RequestBody List<Answer> answers,
                                  @RequestParam(defaultValue = "-1") int time,
                                  @RequestHeader int terminal) throws BizException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        logger.info("【REQUEST】招警机考5题保存。userId={},terminal={},practiceId={},answers={},time={}", userId, terminal, practiceId, answers, time);

        final int area = userSessionService.getArea(token);
        paperAnswerCardService.submitAnswers(Long.parseLong(practiceId), userId, answers, area, true);

        logger.info("【RESPONSE】招警机考5题保存。接口响应用时={}。", String.valueOf(stopwatch.stop()));

        Map map = new HashMap(4);
        map.put("message", "答案保存成功");
        return map;
    }


    /**
     * 模考估分、大赛 交卷
     * 修改原因：web端不支持超过19位的整型数据（将答题卡id用String接收）
     *
     * @param token
     * @param practiceId
     * @param answers
     * @param terminal
     * @param cardType   答题卡类型
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "pc/estimate/{practiceId}", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object pcSubmitEstimatePaper(@RequestHeader(required = false) String token,
                                        @RequestHeader(defaultValue = "1") String cv,
                                        @PathVariable String practiceId,
                                        @RequestBody List<Answer> answers,
                                        @RequestHeader int terminal,
                                        @RequestParam(defaultValue = "-1") int time,
                                        @RequestParam(defaultValue = "-1") int cardType) throws BizException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        logger.info("【REQUEST】招警机考交卷。userId={},terminal={},practiceId={},answers={},time={},cardType={}", userId, terminal, practiceId, answers, time, cardType);


        final int area = userSessionService.getArea(token);
        String uname = userSessionService.getUname(token);

        Object result = paperAnswerCardService.submitEstimateAnswers(Long.parseLong(practiceId), userId, answers, area, uname, cardType, terminal, cv);
        /**
         * pc端总用时使用time字段
         */
        if (-1 != time && result instanceof StandardCard) {
            int expendTime = ((StandardCard) result).getExpendTime();//耗时
            int remainingTime = ((StandardCard) result).getRemainingTime();//剩余时间
            int totalTime = expendTime + remainingTime;
            ((StandardCard) result).setExpendTime(time);
            ((StandardCard) result).setRemainingTime(totalTime - time);
        }
        /**
         * 此处处理答题卡信息 中如果有知识点信息 且有试题列表的情况下,知识点信息和试题列表中一级标题顺序不一致
         * update by lijun 2018-05-30
         */
        if (null != result && result instanceof AnswerCard) {
            sortPointTree((AnswerCard) result);
        }
        logger.info("【RESPONSE】招警机考交卷。接口响应用时={}。answerCard={}", String.valueOf(stopwatch.stop()), result);

        return result;
    }


    /**
     * 根据id查询答题卡信息
     *
     * @param token
     * @param id    练习id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "pc/{id}", method = RequestMethod.GET)
    public Object getForPc(@RequestHeader(required = false) String token,
                           @RequestHeader(required = false) int terminal,
                           @PathVariable String id,
                           @RequestHeader(defaultValue = "1") String cv) throws BizException {
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);

        return paperAnswerCardService.findAnswerCardDetail(Long.parseLong(id), uid, terminal, cv);
    }
}
