package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.activity.Estimate;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.*;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.PracticePointsSummaryDao;
import com.huatu.ztk.paper.enums.CoursePaperType;
import com.huatu.ztk.paper.enums.CustomizeEnum;
import com.huatu.ztk.paper.service.v4.impl.ComputeScoreUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionCorrectType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组卷服务层
 * Created by shaojieyue
 * Created time 2016-04-29 14:56
 */

@Service
public class PracticeService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeService.class);

    public static final String CUSTOMIZE_PRACTICE = "专项练习(做题模式)";
    public static final String CUSTOMIZE_RECITE_PRACTICE = "专项练习(背题模式)";
    public static final String SMART_PRACTICE = "智能刷题";
    public static final String DAY_TRAIN_PRACTICE = "每日特训";

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;
    @Autowired
    private QuestionStrategyDubboService questionStrategyDubboService;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    @Autowired
    private PracticeCardService practiceCardService;

    @Autowired
    private PracticePointsSummaryDao practicePointsSummaryDao;
    @Autowired
    private BigBagUsedSubjectConfig bigBagUsedSubjectConfig;
    @Autowired
    private PaperAnswerCardUtilComponent utilComponent;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 每日特训,组卷接口
     *
     * @param userId
     * @param pointId 知识点id
     * @param size
     * @return
     */
    public PracticePaper createDayTrainPaper(long userId, Integer pointId, int size, int subject) throws BizException {
        long stime = System.currentTimeMillis();
        final QuestionPoint questionPoint = questionPointDubboService.findById(pointId);
        logger.info("DayTrainPaper, find questionPoint,time={},uid={},pointId={}", System.currentTimeMillis() - stime, userId, pointId);
        return combinePracticePaper(DAY_TRAIN_PRACTICE, size, questionPoint, userId, subject);
    }

    /**
     * 根据id组卷
     *
     * @param pointId 知识点id
     * @param size
     * @return
     */
    public PracticePaper createPracticePaper(Integer pointId, int size, long uid, int subject) throws BizException {
        long stime = System.currentTimeMillis();
        final QuestionPoint questionPoint = questionPointDubboService.findById(pointId);
        logger.info("find questionPoint,time={},uid={},pointId={}", System.currentTimeMillis() - stime, uid, pointId);
        return combinePracticePaper(CUSTOMIZE_PRACTICE, size, questionPoint, uid, subject);
    }

    /**
     * 专项训练组卷(做题和背题模式)
     *
     * @param pointId
     * @param size
     * @param uid
     * @param subject
     * @return
     * @throws BizException
     */
    public PracticePaper createPracticePaperV2(Integer pointId, int size, long uid, int subject,
                                               CustomizeEnum.ModeEnum modeEnum) throws BizException {
        long stime = System.currentTimeMillis();
        final QuestionPoint questionPoint = questionPointDubboService.findById(pointId);
        logger.info("find questionPoint,time={},uid={},pointId={}", System.currentTimeMillis() - stime, uid, pointId);
        String papaerName = modeEnum == CustomizeEnum.ModeEnum.Write ? CUSTOMIZE_PRACTICE : CUSTOMIZE_RECITE_PRACTICE;
        return combinePracticePaper(papaerName, size, questionPoint, uid, subject);
    }

    /**
     * 查询当前知识点下是否绑定未完成的答题卡，如果绑定，则直接返回绑定的答题卡，这个逻辑在课后作业练一练中用到
     *
     * @param pointId
     * @param uid
     * @param subject
     * @param size
     * @return
     */
    public AnswerCard findUnFinishedCard(Integer pointId, long uid, int subject, int size) {
        Map<Integer, Long> unfinishedPointMap = practiceCardService.getUnfinishedPointMap(uid, subject);
        Long practiceId = unfinishedPointMap.getOrDefault(pointId, -1L);
        if (practiceId > -1) {
            AnswerCard answerCard = answerCardDao.findById(practiceId);
            if (answerCard.getStatus() != AnswerCardStatus.FINISH &&
                    answerCard.getCorrects().length <= size) {          //答题卡试题数量不超过需要的试题数量，则直接返回答题卡（针对课后作业练一练，创建答题卡要求10道试题）
                return answerCard;
            }
        }
        return null;
    }

    /**
     * 查询当前知识点下是否绑定未完成的答题卡(区别做题模式和背题模式)
     *
     * @param pointId
     * @param uid
     * @param subject
     * @param size
     * @param modeEnum
     * @return
     */
    public AnswerCard findUnFinishedCardV2(Integer pointId, long uid, int subject, int size, CustomizeEnum.ModeEnum modeEnum) {
        Map<Integer, Long> unfinishedPointMap = practiceCardService.getUnfinishedPointMapV2(uid, subject, modeEnum);
        Long practiceId = unfinishedPointMap.getOrDefault(pointId, -1L);
        if (practiceId > -1) {
            AnswerCard answerCard = answerCardDao.findById(practiceId);
            if (answerCard.getStatus() != AnswerCardStatus.FINISH &&
                    answerCard.getCorrects().length <= size) {          //答题卡试题数量不超过需要的试题数量，则直接返回答题卡（针对课后作业练一练，创建答题卡要求10道试题）
                return answerCard;
            }
        }
        return null;
    }

    /**
     * 智能出题
     *
     * @param size
     * @param uid
     * @param subject
     * @return
     */
    public PracticePaper createSmartPaper(int size, long uid, int subject) {
        logger.info("size={},uid={},subject={}", size, uid, subject);
        QuestionStrategy questionStrategy;

        //为了兼容目前已有科目有智能刷题功能,但是科目内无题 的情况,走随机刷题方法
        if (subject != SubjectType.GWY_XINGCE || subject != SubjectType.SYDW_GONGJI) {
            questionStrategy = questionStrategyDubboService.randomStrategy(uid, subject, -1, size);
            logger.info("非行测,智能刷题跳转随机刷题方法");
        } else {
            try {
                questionStrategy = questionStrategyDubboService.smartStrategy(uid, subject, size);
            } catch (Exception e) {
                questionStrategy = questionStrategyDubboService.randomStrategy(uid, subject, -1, size);
            }
        }
        if (questionStrategy == null) {
            return null;
        }
        final PracticePaper practicePaper = toPracticePaper(questionStrategy, subject);
        practicePaper.setName(getPracticeName(SMART_PRACTICE, ""));
        return practicePaper;
    }

    public PracticePaper combinePracticePaper(String name, int size, QuestionPoint questionPoint, long uid, int subject) throws BizException {
        if (questionPoint == null) {
            return null;
        }
        long stime = System.currentTimeMillis();
        QuestionStrategy questionStrategy = questionStrategyDubboService.randomStrategy(uid, subject, questionPoint.getId(), size);
        logger.info("get questionStrategy time={},uid={},pointId={}", System.currentTimeMillis() - stime, uid, questionPoint.getId());

        if (CollectionUtils.isEmpty(questionStrategy.getQuestions())) {
            throw new BizException(PracticeErrors.QUESTION_COUNT_NOT_ENOUGH);
        }
        if (DAY_TRAIN_PRACTICE.equals(name) && questionStrategy.getQuestions().size() < 5) {
            throw new BizException(PracticeErrors.QUESTION_COUNT_NOT_ENOUGH);
        }
        long stime2 = System.currentTimeMillis();
        final PracticePaper practicePaper = toPracticePaper(questionStrategy, subject);
        practicePaper.setName(getPracticeName(name, questionPoint.getName()));
        logger.info("to practice time={},uid={}", System.currentTimeMillis() - stime2, uid);
        return practicePaper;
    }

    /**
     * 组装练习名称
     *
     * @param typeName
     * @param pointName
     * @return
     */
    public String getPracticeName(String typeName, String pointName) {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append(typeName).append("-").append(pointName).append("-").append(DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmm")).toString();
    }


    /**
     * 分页查询做题记录
     *
     * @param userId         用户id
     * @param cursor         游标
     * @param size           每页大小
     * @param cardType       答题卡类型
     * @param cardTime       答题时间
     * @param removeEstimate 是否移除模考估分的答题记录
     * @return
     */
    public PageBean findCards(long userId, List<Integer> catgoryList, long cursor, int size,
                              int cardType, String cardTime, boolean removeEstimate, List<Integer> subject) {
        List<AnswerCard> results = answerCardDao.findForPage(userId, catgoryList, cursor, size,
                cardType, cardTime, removeEstimate, subject);
        Optional.ofNullable(results)
                .ifPresent(i -> i.forEach(a -> {
                    if (a instanceof StandardCard) {
                        ((StandardCard) a).setIdStr(a.getId() + "");
                        a.setScoreStr(String.valueOf(a.getScore()));
                    } else if (a instanceof PracticeCard) {
                        ((PracticeCard) a).setIdStr(a.getId() + "");
                        a.setScoreStr(String.valueOf(a.getScore()));
                    }
                }));

        /**
         * @update huangqp
         * 将游标下移的逻辑移到上面来，避免该页的数据如果全是不符合要求的数据时，游标不移动，则之后的数据都没法查到
         */
        long newCursor = cursor;
        if (results.size() > 0) {//最晚的一条练习的id作为下次请求的游标
            newCursor = results.get(results.size() - 1).getId();
        }
        //去除考试时间结束后才能查看解析的试卷
        results.removeIf(card -> {
            if (card.getType() == AnswerCardType.SIMULATE ||
                    card.getType() == AnswerCardType.ESTIMATE ||
                    card.getType() == AnswerCardType.MATCH) {
                //取答题卡的试卷信息
                StandardCard scard = (StandardCard) card;
                EstimatePaper paper = (EstimatePaper) scard.getPaper();

                if (paper.getLookParseTime() == LookParseStatus.AFTER_END
                        && paper.getEndTime() > System.currentTimeMillis()) {
                    return true;
                }
            }
            return false;
        });
        PageBean pageBean = new PageBean(results, newCursor, -1);
        return pageBean;
    }

    /**
     * 网页分页查询答题记录
     *
     * @param userId         用户id
     * @param catgoryList    考试类型
     * @param page           页数
     * @param size           每页大小
     * @param cardType       答题卡类型
     * @param cardTime       答题时间
     * @param removeEstimate 是否移除模考估分的答题记录
     * @param subject        科目id
     * @param status
     * @return
     */
    public PageBean findCardsByPage(long userId, List<Integer> catgoryList, int page, int size, int cardType, String cardTime, boolean removeEstimate, int subject, int status) {
        return answerCardDao.findByPage(userId, catgoryList, page, size, cardType, cardTime, removeEstimate, subject, status);
    }

    /**
     * 错题练习组卷接口
     *
     * @param pointId 知识点id
     * @param userId
     * @return
     */
    public PracticePaper createErrorQuestionPaper(Integer pointId, long userId, int subject, int size) {
        final QuestionStrategy questionStrategy = questionStrategyDubboService.randomErrorStrategy(userId, pointId, subject, size);
        final PracticePaper practicePaper = toPracticePaper(questionStrategy, subject);
        practicePaper.setName(getPracticeName("错题重练", ""));
        return practicePaper;
    }

    /**
     * 收藏的试题练习
     *
     * @param pointId
     * @param userId
     * @param subject
     * @return
     */
    public PracticePaper createCollectQuestionPaper(int pointId, long userId, int subject) {
        final QuestionStrategy questionStrategy = questionStrategyDubboService.randomCollectStrategy(userId, pointId, subject, 10);
        final PracticePaper practicePaper = toPracticePaper(questionStrategy, subject);
        practicePaper.setName(getPracticeName("收藏练习", ""));
        return practicePaper;
    }

    public PracticePaper toPracticePaper(QuestionStrategy questionStrategy, int subject) {
        final PracticePaper practicePaper = PracticePaper.builder().build();
        practicePaper.setQuestions(questionStrategy.getQuestions());
        practicePaper.setDifficulty(questionStrategy.getDifficulty());
        practicePaper.setQcount(questionStrategy.getQuestions().size());
        practicePaper.setModules(questionStrategy.getModules());
        practicePaper.setSubject(subject);
        practicePaper.setCatgory(subjectDubboService.getCatgoryBySubject(subject));
        return practicePaper;
    }

    /**
     * 获取试题
     *
     * @param size
     * @param pointId
     * @return
     */
    public Object getQuestionByPointId(int size, int pointId) {
        long stime = System.currentTimeMillis();
        final QuestionPoint questionPoint = questionPointDubboService.findById(pointId);
        logger.info("DayTrain, find questionPoint,time={},pointId={}", System.currentTimeMillis() - stime, pointId);
        if (questionPoint == null) {
            return null;
        }
        final QuestionStrategy questionStrategy = questionStrategyDubboService.randomStrategy(0, CatgoryType.GONG_WU_YUAN, questionPoint.getId(), size);
        logger.info("get questionStrategy time={},pointId={}", System.currentTimeMillis() - stime, questionPoint.getId());

        long stime2 = System.currentTimeMillis();
        List<Question> questionList = questionDubboService.findBath(questionStrategy.getQuestions());
        logger.info("to practice time={}", System.currentTimeMillis() - stime2);
        return questionList;
    }


    /**
     * 创建课后练习 答题卡信息
     *
     * @param terminal       设备类型
     * @param uid            用户ID
     * @param name           答题卡名称
     * @param questionId     试题合集
     * @param subject        科目
     * @param answerCardType 答题卡类型
     * @return
     * @throws BizException
     */
    public PracticeCard createCoursePracticeCard(
            Integer terminal, Integer uid, String name, String questionId, Integer subject,
            Long courseId, Integer courseType, Integer type, Integer answerCardType,
            List<Object> breakPointInfo
    ) throws BizException {
        AnswerCard courseAnswerCard = answerCardDao.findCourseAnswerCard(uid, courseId, courseType, type);
        if (null != courseAnswerCard) {
            return (PracticeCard) courseAnswerCard;
        }

        List<Integer> questionIdList = Arrays.asList(questionId.split(","))
                .stream().map(Integer::valueOf)
                .collect(Collectors.toList());
        PracticeForCoursePaper practicePaper = new PracticeForCoursePaper();
        practicePaper.setQuestions(questionIdList);
        practicePaper.setDifficulty(5.0);
        practicePaper.setQcount(questionIdList.size());
        practicePaper.setSubject(subject);
        practicePaper.setCatgory(subjectDubboService.getCatgoryBySubject(subject));
        practicePaper.setName(name);
        practicePaper.setCourseType(courseType);
        practicePaper.setCourseId(courseId);
        practicePaper.setType(type);
        if (null != breakPointInfo) {
            practicePaper.setBreakPointInfoList(breakPointInfo);
        }
        //生成答题卡
        PracticeCard practiceCard = practiceCardService.create(practicePaper, terminal, answerCardType, uid);

        return practiceCard;
    }

    /**
     * 获取课后练习 答题卡信息
     *
     * @param uid        用户ID
     * @param courseId
     * @param courseType
     * @param type
     * @return
     * @throws BizException
     */
    public PracticeCard obtainCoursePracticeCard(Integer uid, Long courseId, Integer courseType, Integer type) {
        AnswerCard courseAnswerCard = answerCardDao.findCourseAnswerCard(uid, courseId, courseType, type);
        if (null != courseAnswerCard) {
            return (PracticeCard) courseAnswerCard;
        } else {
            return PracticeCard.builder().build();
        }
    }

    /**
     * 批量查询答题卡信息
     *
     * @param paramList       参数合集
     * @param userId          用户Id
     * @param coursePaperType 试卷类型
     * @return
     */
    public List<HashMap<String, Object>> getCoursePracticeCardInfo(List<HashMap<String, Object>> paramList, long userId, CoursePaperType coursePaperType) {
        //答题卡查询
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("获取课后练习的答题卡信息");
        long t1 = System.currentTimeMillis();
        List<AnswerCard> courseAnswerCardListInfo = answerCardDao.findCourseAnswerCardListInfo(paramList, userId, coursePaperType);
        logger.info("获取课后练习答题卡耗时:{},userId:{},请求 size:{}", System.currentTimeMillis() - t1, userId, paramList.size());
        List<HashMap<String, Object>> collect = courseAnswerCardListInfo.stream()
                //筛选课程相关答题卡
                .filter(answerCard -> answerCard instanceof PracticeCard && ((PracticeCard) answerCard).getPaper() instanceof PracticeForCoursePaper)

                .map(answerCard -> {
                    HashMap<String, Object> data = new HashMap<>();
                    PracticeForCoursePaper practiceForCoursePaper = (PracticeForCoursePaper) ((PracticeCard) answerCard).getPaper();
                    Long courseId = practiceForCoursePaper.getCourseId();
                    int courseType = practiceForCoursePaper.getCourseType();
                    data.put("courseId", courseId);
                    data.put("courseType", courseType);
                    data.put("status", answerCard.getStatus());
                    data.put("rcount", answerCard.getRcount());
                    data.put("wcount", answerCard.getWcount());
                    data.put("ucount", answerCard.getUcount());
                    data.put("id", answerCard.getId());
                    //logger.info("answerCard = {}",answerCard);
                    //logger.info("data = {}",data);
                    return data;
                })
                .collect(Collectors.toList());
        stopWatch.stop();
        logger.info("获取课后练习的答题卡信息,参数信息，userId = {},paramsList = {}, result = {}, 耗时:{}", userId, paramList, collect, stopWatch.prettyPrint());
        logger.info("获取课后练习的答题卡信息耗时，userId = {}, paramsList.size:{}, 耗时 = {}", userId, paramList.size(), stopWatch.prettyPrint());
        return collect;
    }

    /**
     * 批量查询答题卡信息V2
     *
     * @param carIds 答题卡id
     * @return
     */
    public List<HashMap<String, Object>> getCoursePracticeCardInfoV2(List<Long> carIds) {
        //答题卡查询
        if (CollectionUtils.isEmpty(carIds)) {
            return Lists.newArrayList();
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("获取课后练习的答题卡信息V2");
        List<AnswerCard> courseAnswerCardListInfo = findCardsByIds(carIds);
        stopWatch.stop();
        logger.info("获取课后练习的答题卡信息V2:{},courseAnswerCardListInfo:{}", courseAnswerCardListInfo);
        List<HashMap<String, Object>> collect = courseAnswerCardListInfo.stream()
                //筛选课程相关答题卡
                .filter(answerCard -> answerCard instanceof PracticeCard && ((PracticeCard) answerCard).getPaper() instanceof PracticeForCoursePaper)
                .map(answerCard -> {
                    HashMap<String, Object> data = new HashMap<>();
                    PracticeForCoursePaper practiceForCoursePaper = (PracticeForCoursePaper) ((PracticeCard) answerCard).getPaper();
                    Long courseId = practiceForCoursePaper.getCourseId();
                    int courseType = practiceForCoursePaper.getCourseType();
                    data.put("courseId", courseId);
                    data.put("courseType", courseType);
                    data.put("status", answerCard.getStatus());
                    data.put("rcount", answerCard.getRcount());
                    data.put("wcount", answerCard.getWcount());
                    data.put("ucount", answerCard.getUcount());
                    data.put("id", answerCard.getId());
                    return data;
                })
                .collect(Collectors.toList());
        logger.info("获取课后练习的答题卡信息V2,carIds.size:{}, collect.size:{},耗时:{}", carIds.size(), collect.size(), stopWatch.prettyPrint());
        return collect;
    }

    /**
     * 获取指定用户所有的课后练习或课中练习答题卡信息
     *
     * @param userId          用户Id
     * @param coursePaperType 试卷类型
     * @return
     */
    public List<HashMap<String, Object>> getCourseExercisesAllCardInfo(long userId, CoursePaperType coursePaperType) {
        //答题卡查询
        StopWatch stopWatch = new StopWatch("获取指定用户所有的课后练习答题卡");
        stopWatch.start("getCourseExercisesAllCardInfo");
        List<AnswerCard> courseAnswerCardListInfo = answerCardDao.findCourseAnswerCardAllInfo(userId, coursePaperType);
        stopWatch.stop();
        List<HashMap<String, Object>> collect = courseAnswerCardListInfo.stream()
                //筛选课程相关答题卡
                .filter(answerCard -> answerCard instanceof PracticeCard && ((PracticeCard) answerCard).getPaper() instanceof PracticeForCoursePaper)

                .map(answerCard -> {
                    HashMap<String, Object> data = new HashMap<>();
                    PracticeForCoursePaper practiceForCoursePaper = (PracticeForCoursePaper) ((PracticeCard) answerCard).getPaper();
                    Long courseId = practiceForCoursePaper.getCourseId();
                    int courseType = practiceForCoursePaper.getCourseType();
                    data.put("courseId", courseId);
                    data.put("courseType", courseType);
                    data.put("status", answerCard.getStatus());
                    data.put("rcount", answerCard.getRcount());
                    data.put("wcount", answerCard.getWcount());
                    data.put("ucount", answerCard.getUcount());
                    data.put("id", answerCard.getId());
                    return data;
                })
                .collect(Collectors.toList());
        logger.info("获取指定用户所有的课后练习答题卡,耗时, ----- userId:{}, size:{},耗时:{}", userId, courseAnswerCardListInfo.size(), stopWatch.prettyPrint());
        return collect;
    }


    public PracticePaper createErrorQuestionPaperWithFlag(int pointId, long userId, int subject, int size, int flag, String name) {
        final QuestionStrategy questionStrategy = questionStrategyDubboService.randomErrorStrategyWithFlag(userId, pointId, subject, size, flag);
        final PracticePaper practicePaper = toPracticePaper(questionStrategy, subject);
        practicePaper.setName(getPracticeName(name, ""));
        return practicePaper;
    }


    /**
     * 创建并且保存随堂练答题卡
     *
     * @param terminal
     * @param uid
     * @param name
     * @param subject
     * @param courseId
     * @param courseType
     * @param type
     * @param answerCardType
     * @param breakPointInfo
     * @return
     * @throws BizException
     */
    public PracticeCard createAndSaveAnswerCoursePracticeCard(
            Integer terminal, Integer uid, String name, String questionIds, Integer subject,
            Long courseId, Integer courseType, Integer type, Integer answerCardType,
            List<Object> breakPointInfo, String[] answers, int[] corrects, int[] times
    ) throws BizException {
        AnswerCard courseAnswerCard = answerCardDao.findCourseAnswerCard(uid, courseId, courseType, type);
        if (null != courseAnswerCard) {
            return (PracticeCard) courseAnswerCard;
        }

        List<Integer> questionIdList = Arrays.asList(questionIds.split(",")).stream().map(Integer::valueOf)
                .collect(Collectors.toList());
        PracticeForCoursePaper practicePaper = new PracticeForCoursePaper();
        practicePaper.setQuestions(questionIdList);
        practicePaper.setDifficulty(5.0);
        practicePaper.setQcount(questionIdList.size());
        practicePaper.setSubject(subject);
        practicePaper.setCatgory(subjectDubboService.getCatgoryBySubject(subject));
        practicePaper.setName(name);
        practicePaper.setCourseType(courseType);
        practicePaper.setCourseId(courseId);
        practicePaper.setType(type);
        if (null != breakPointInfo) {
            practicePaper.setBreakPointInfoList(breakPointInfo);
        }
        // 生成答题卡

        PracticeCard practiceCard = PracticeCard.builder().build();
        final int qcount = practicePaper.getQcount();// 题量
        long id = 0;

        id = Long.valueOf(String.valueOf(System.nanoTime()) + String.valueOf(System.currentTimeMillis()).substring(11));

        if (id < 1) {// 获取id失败
            throw new BizException(CommonErrors.SERVICE_INTERNAL_ERROR);
        }

        int rcount = 0;// 答题正确数量
        int wcount = 0;// 答题错误数量
        int ucount = 0;// 未做答题数量
        for (int correct : corrects) {
            if (correct == QuestionCorrectType.RIGHT) {
                rcount++;
            } else if (correct == QuestionCorrectType.WRONG) {
                wcount++;
            } else if (correct == QuestionCorrectType.UNDO || correct == QuestionCorrectType.CANNOT_ANSWER) {
                // 未做或者不能作答
                ucount++;
            } else {// 传入非法的数据,也视为答错
                wcount++;
            }
        }

        int elapsedTime = Arrays.stream(times).sum();// 总耗时

        practiceCard.setId(id);
        practiceCard.setUserId(uid);
        practiceCard.setPaper(practicePaper);
        practiceCard.setDifficulty(practicePaper.getDifficulty());
        practiceCard.setAnswers(answers);
        practiceCard.setCreateTime(System.currentTimeMillis());
        practiceCard.setExpendTime(0);
        practiceCard.setName(practicePaper.getName());
        practiceCard.setRcount(rcount);// 正确数量
        practiceCard.setWcount(wcount);// 错误数量
        practiceCard.setUcount(ucount);// 未做数量
        practiceCard.setCorrects(corrects);
        practiceCard.setStatus(AnswerCardStatus.FINISH);
        practiceCard.setCatgory(subjectDubboService.getCatgoryBySubject(practicePaper.getSubject()));
        practiceCard.setSubject(practicePaper.getSubject());
        practiceCard.setTerminal(terminal);
        practiceCard.setTimes(times);
        practiceCard.setType(answerCardType);
        practiceCard.setRemainingTime(0);// 答题剩余时间
        practiceCard.setDoubts(new int[qcount]);
        practiceCard.setRecommendedTime(0);
        practiceCard.setExpendTime(elapsedTime);// 设置总耗时

        // 计算做题速度
        final int answerCount = (int) Arrays.stream(times).filter(time -> time > 0).count();
        int speed = 0;
        if (answerCount > 0) {
            speed = practiceCard.getExpendTime() / answerCount;
        }
        practiceCard.setSpeed(speed);
        // 设置分数
        practiceCard.setScore(ComputeScoreUtil.computeScore(practiceCard));

        answerCardDao.saveWithReflectQuestion(practiceCard);

        //知识点汇总
        savekonwledgeSummary(practiceCard);

        return practiceCard;
    }

    /**
     * 保存知识点
     *
     * @param practiceCard 答题卡
     */
    private void savekonwledgeSummary(PracticeCard practiceCard) {
        final List<QuestionPointTree> pointTrees = questionPointDubboService.questionPointSummaryWithTotalNumber(practiceCard.getPaper().getQuestions(), practiceCard.getCorrects(), practiceCard.getTimes());
        PracticePointsSummary pointsSummary = PracticePointsSummary.builder()
                .practiceId(practiceCard.getId())
                .points(pointTrees)
                .build();
        //插入知识点汇总记录记录（mysql）
        practicePointsSummaryDao.insert(pointsSummary);
    }


    public List<AnswerCard> findCardsByIds(List<Long> practiceIds) {
        return answerCardDao.findByIds(practiceIds);
    }


    public PageBean dealSmallRoutLine(PageBean pageBean, int subject) {
        List answerCardList = pageBean.getResutls();
        if (CollectionUtils.isNotEmpty(answerCardList)) {
            Boolean bagFlag = bigBagUsedSubjectConfig.isEnabledUserSubject(subject);

            answerCardList.stream().forEach(card -> {

                if (card instanceof StandardCard) {
                    Paper paper = ((StandardCard) card).getPaper();
                    if (paper instanceof EstimatePaper) {
                        //参加人数
                        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
                        String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paper.getId());
                        long cardCounts = zSetOperations.zCard(paperPracticeIdSore);
                        PaperMeta paperMeta = PaperMeta.builder()
                                .cardCounts(Integer.valueOf(cardCounts + "")).build();
                        paper.setPaperMeta(paperMeta);
                        //icon
                        String iconUrl = "";
                        if (bagFlag) {
                            Estimate estimate = utilComponent.getEstimateGiftInfoHash(paper.getId());
                            if (null != estimate) {
                                iconUrl = estimate.getIconUrl();
                            }
                        }
                        ((EstimatePaper) paper).setIconUrl(iconUrl);
                    }
                }
            });
        }
        pageBean.setResutls(answerCardList);
        return pageBean;
    }

}
