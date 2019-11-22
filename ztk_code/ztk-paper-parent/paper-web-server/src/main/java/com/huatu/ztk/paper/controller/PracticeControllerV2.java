package com.huatu.ztk.paper.controller;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.enums.CoursePaperType;
import com.huatu.ztk.paper.service.*;
import com.huatu.ztk.paper.service.v4.PracticeCourseService;
import com.huatu.ztk.paper.service.v4.impl.AnswerCardUtil;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.huatu.ztk.paper.service.PaperAnswerCardService.sortPointTree;

/**
 * 练习控制层v2
 */
@RestController
@RequestMapping(value = "/v2/practices", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PracticeControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(PracticeControllerV2.class);

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private PracticeCardService practiceCardService;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private PracticeCourseService practiceCourseService;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    @Autowired
    private PaperService paperService;

    /**
     * 查询我的练习记录v2
     * 与v1不同的是catgorys参数
     *
     * @param token
     * @param terminal
     * @param cursor
     * @param cardType
     * @param cardTime
     * @param catgorys 考试类型,用","隔开
     * @return
     * @throws WaitException
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object queryCards(@RequestHeader(required = false) String token, @RequestHeader int terminal,
                             @RequestParam(defaultValue = Long.MAX_VALUE + "") long cursor,
                             @RequestParam(defaultValue = "0") int cardType,
                             @RequestParam(defaultValue = "") String cardTime,
                             @RequestParam String catgorys) throws WaitException, BizException {

        userSessionService.assertSession(token);

        if (cursor < 1) {//说明查询第一页，那么，cursor设置为最大值
            cursor = Long.MAX_VALUE;
        }

        //用户id
        long userId = userSessionService.getUid(token);

        //获取科目
        String[] idArray = catgorys.split(",");
        List catgoryList = new ArrayList();
        for (String str : idArray) {
            Integer id = Ints.tryParse(str);
            if (id == null) {//id列表转换错误
                return CommonErrors.INVALID_ARGUMENTS;
            }
            catgoryList.add(id);
        }

        final PageBean pageBean = practiceService.findCards(userId, catgoryList, cursor, 20,
                cardType, cardTime, false, Lists.newArrayList(-1));
        return pageBean;
    }


    /**
     * 专项训练
     *
     * @param pointId 知识点id
     * @return
     */
    @RequestMapping(value = "customizes", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object customizes(@RequestParam Integer pointId,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestHeader int terminal,
                             @RequestHeader(required = false) String token) throws WaitException, BizException {
        userSessionService.assertSession(token);
        //用户id
        long userId = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        AnswerCard unFinishedCard = practiceService.findUnFinishedCard(pointId, userId, subject, size);
        if (null != unFinishedCard) {
            return unFinishedCard;
        }
        final PracticePaper practicePaper = practiceService.createPracticePaper(pointId, size, userId, subject);
        if (practicePaper == null) {//没有查到
            return CommonErrors.RESOURCE_NOT_FOUND;
        }

        final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.CUSTOMIZE_PAPER, userId);
        AnswerCardUtil.fillIdStr(practiceCard);
        logger.info(JsonUtil.toJson(practiceCard));

        //添加未完成练习id
        practiceCardService.addCustomizesUnfinishedId(pointId, practiceCard);
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
        int errorQcount = Math.max(10, userSessionService.getErrorQcount(token));

        final PracticePaper practicePaper = practiceService.createErrorQuestionPaper(pointId, userId, subject, errorQcount);
        final PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, AnswerCardType.WRONG_PAPER, userId);
        return practiceCard;
    }

    /**
     * 根据用户行测答题卡id，获取总体排名曲线和成绩
     *
     * @param token
     * @param paperId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/total/{paperId}", method = RequestMethod.GET)
    public Object getTotal(@RequestHeader(required = false) String token,
                           @PathVariable int paperId) throws BizException {
        logger.info("token={},paperId={}", token, paperId);
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);
        return paperAnswerCardService.getUserMatchAnswerCardMetaInfo(paperId, uid);
    }

    /**
     * 根据试卷id查询答题卡信息
     *
     * @param token
     * @param paperId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/{paperId}", method = RequestMethod.GET)
    public Object get(@RequestHeader(required = false) String token,
                      @PathVariable int paperId,
                      @RequestHeader(defaultValue = "1") int terminal,
                      @RequestHeader(defaultValue = "1.0") String cv) throws BizException {
        logger.info("token={},paperId={}", token, paperId);
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);
        logger.info("userId={}", uid);
        MatchUserMeta userMeta = matchService.findMatchUserMeta(uid, paperId);
        if (userMeta == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        AnswerCard answerCard = paperAnswerCardService.findById(userMeta.getPracticeId(), uid);
        if (!AnswerCardUtil.judgeUserCv(terminal, cv)) {
            AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transInt);
        } else {
            AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transDouble);
        }
        /**
         * 此处处理答题卡信息 中如果有知识点信息 且有试题列表的情况下,知识点信息和试题列表中一级标题顺序不一致
         * update by lijun 2018-05-30
         */
        sortPointTree(answerCard);
        return answerCard;
    }

    @RequestMapping(value = "createCourseExercisesPracticeCard", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createCourseExercisesPracticeCard(
            @RequestParam("terminal") Integer terminal,
            @RequestParam("userId") Integer uid,
            @RequestParam("name") String name,
            @RequestParam("courseType") Integer courseType,
            @RequestParam("courseId") Long courseId,
            @RequestParam("questionId") String questionId,
            @RequestParam("subject") Integer subject
    ) throws BizException {
        return practiceService.createCoursePracticeCard(terminal, uid, name, questionId, subject, courseId, courseType, 2, AnswerCardType.COURSE_EXERCISE, null);
    }


    @RequestMapping(value = "obtainCourseExercisesPracticeCard", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object obtainCourseExercisesPracticeCard(
            @RequestParam("userId") Integer uid,
            @RequestParam("courseType") Integer courseType,
            @RequestParam("courseId") Long courseId
    ) throws BizException {
        return practiceService.obtainCoursePracticeCard(uid, courseId, courseType, 2);
    }


    /**
     * 创建课程 课中练习
     *
     * @throws BizException
     */
    @RequestMapping(value = "createCourseBreakPointPracticeCard", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createCourseBreakPointPracticeCard(
            @RequestParam("terminal") Integer terminal,
            @RequestParam("userId") Integer uid,
            @RequestParam("name") String name,
            @RequestParam("courseType") Integer courseType,
            @RequestParam("courseId") Long courseId,
            @RequestParam("questionId") String questionId,
            @RequestParam("subject") Integer subject,
            @RequestBody List<Object> breakPointInfo
    ) throws BizException {
        return practiceService.createCoursePracticeCard(terminal, uid, name, questionId, subject, courseId, courseType, 1, AnswerCardType.COURSE_BREAKPOINT, breakPointInfo);
    }

    /**
     * 获取课后练习的答题卡信息  courseType,courseId
     */
    @RequestMapping(value = "/{userId}/getCourseExercisesCardInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getCourseExercisesCardInfo(
            @RequestHeader long userId,
            @RequestBody List<HashMap<String, Object>> paramsList
    ) {
        //logger.info("参数信息，userId = {},paramsList = {}", userId, paramsList);
        return practiceService.getCoursePracticeCardInfo(paramsList, userId, CoursePaperType.COURSE_EXERCISE);
    }

    /**
     * 批量获取课后练习的答题卡信息
     */
    @RequestMapping(value = "/getCourseExercisesCardInfoV2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getCourseExercisesCardInfoV2(@RequestBody List<Long> cardIds) {
        return practiceService.getCoursePracticeCardInfoV2(cardIds);
    }

    /**
     * 获取指定用户所有的课后练习答题卡信息
     */
    @RequestMapping(value = "/{userId}/getCourseExercisesAllCardInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getCourseExercisesAllCardInfo(
            @RequestHeader long userId
    ) {
        return practiceService.getCourseExercisesAllCardInfo(userId, CoursePaperType.COURSE_EXERCISE);
    }

    /**
     * 获取课中练习的答题卡信息  courseType,courseId
     */
    @RequestMapping(value = "/{userId}/getCourseBreakPointCardInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getCourseBreakPointCardInfo(
            @RequestHeader long userId,
            @RequestBody List<HashMap<String, Object>> paramsList
    ) {
        //logger.info("参数信息，userId = {},paramsList = {}", userId, paramsList);
        return practiceService.getCoursePracticeCardInfo(paramsList, userId, CoursePaperType.COURSE_BREAKPOINT);
    }


    /**
     * 随堂练创建答题卡并保存答题
     *
     * @param uid
     * @param name
     * @param courseType
     * @param courseId
     * @param questionIds
     * @param answers
     * @param corrects
     * @param times
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/createAndSaveAnswerCoursePracticeCard", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createAndSaveAnswerCoursePracticeCard(@RequestParam("userId") Integer uid,
                                                        @RequestParam("name") String name, @RequestParam("courseType") Integer courseType,
                                                        @RequestParam("courseId") Long courseId, @RequestParam("questionIds") String questionIds,
                                                        @RequestParam("answers") String[] answers, @RequestParam("corrects") int[] corrects,
                                                        @RequestParam("times") int[] times) throws BizException {
        // type 1随堂练 2课后练习
        logger.info("createAndSaveAnswerCoursePracticeCard param courseType:{},courseId:{},questionIds:{}", courseType,
                courseId, questionIds);
        return practiceService.createAndSaveAnswerCoursePracticeCard(1, uid, name, questionIds, 1,
                courseId, courseType, 1, AnswerCardType.COURSE_BREAKPOINT, null, answers, corrects, times);
    }

    /**
     * 批量查询课后作业答题卡
     *
     * @param ids
     * @return
     */
    @RequestMapping(value = "getCourseExercisesCardInfoBatch", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Object getCourseExercisesCardInfoBatch(@RequestParam(value = "ids") String ids) {
        if (StringUtils.isBlank(ids)) {
            return Lists.newArrayList();
        }
        List<Long> practiceIds = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList());
        List<AnswerCard> answerCards = practiceService.findCardsByIds(practiceIds);
        if (CollectionUtils.isEmpty(answerCards)) {
            return Lists.newArrayList();
        }
        return answerCards.stream().map(AnswerCardUtil::transCourseExercisesCardMap).collect(Collectors.toList());
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
    public Object create(@RequestParam int id, @RequestHeader(required = false) String token, @RequestHeader int terminal) throws WaitException, BizException {
        userSessionService.assertSession(token);

        //用户id
        long userId = userSessionService.getUid(token);
        //知识点类目
        int subject = userSessionService.getSubject(token);
        AnswerCard undoCard = paperAnswerCardService.findUndoCard(userId, id);
        if (null != undoCard) {
            return undoCard;
        }
        final Paper paper = paperService.findById(id);
        final StandardCard practicePaper = paperAnswerCardService.create(paper, subject, userId, terminal);

        if (practicePaper == null) {//资源不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        paperUserMetaService.addUndoPractice(userId, id, practicePaper.getId());
        return practicePaper;
    }


    /**
     * 广告跳转做题页面(单独兼容app广告跳转)
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/advertPapers", method = RequestMethod.POST)
    public Object createAdvertPapers(@RequestParam int id, @RequestHeader(required = false) String token, @RequestHeader int terminal) throws WaitException, BizException {
        userSessionService.assertSession(token);

        HashMap map = new HashMap();
        //用户id
        long userId = userSessionService.getUid(token);
        //知识点类目
        int subject = userSessionService.getSubject(token);
        AnswerCard undoCard = paperAnswerCardService.findUndoCard(userId, id);
        if (null != undoCard) {
            map.put("practiceId", undoCard.getId());
            return map;
        }
        final Paper paper = paperService.findById(id);
        final StandardCard practicePaper = paperAnswerCardService.create(paper, subject, userId, terminal);

        if (practicePaper == null) {//资源不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        paperUserMetaService.addUndoPractice(userId, id, practicePaper.getId());
        map.put("practiceId", practicePaper.getId());
        return map;

    }

}
