package com.huatu.ztk.paper.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.bo.AnswerInfoBo;
import com.huatu.ztk.paper.common.*;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.PracticePointsSummaryDao;
import com.huatu.ztk.paper.enums.CustomizeEnum;
import com.huatu.ztk.paper.service.v4.HandlerMetaService;
import com.huatu.ztk.paper.service.v4.impl.AnswerCardUtil;
import com.huatu.ztk.paper.service.v4.impl.ComputeScoreUtil;
import com.huatu.ztk.paper.vo.PeriodTestSubmitlPayload;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionCorrectType;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dubbo.UserDubboService;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 试卷答题卡service层
 * Created by shaojieyue
 * Created time 2016-05-03 13:55
 */
@Service
public class PaperAnswerCardService {
    private static final Logger logger = LoggerFactory.getLogger(PaperAnswerCardService.class);
    public static final int MAX_ANSWER_TIME = 3 * 60;//最大答题时间

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private PracticePointsSummaryDao practicePointsSummaryDao;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    @Autowired
    private UserDubboService userDubboService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private PracticeCardService practiceCardService;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private PaperAnswerCardUtilComponent utilComponent;

    @Autowired
    private HandlerMetaService handlerMetaService;

    Map<Integer, Integer> categoryHashMap = Maps.newHashMap();

    /**
     * 创建答题卡
     *
     * @param paper
     * @param subject
     * @param userId
     * @param terminal
     * @return
     * @throws WaitException
     * @throws BizException
     */
    public StandardCard create(final Paper paper, int subject, long userId, int terminal) throws WaitException, BizException {
        StandardCard standardCard = null;
        /**
         * 取消对模考大赛类型试卷的限制，如果是模考大赛试卷，通过这边创建的答题卡，是往期模考历史答题卡
         */
        if (paper == null) {
            return standardCard;
        }
        if (paper.getType() == PaperType.MATCH) {
            paper.setType(PaperType.MATCH_AFTER);
        }
        return createAnswerCard(paper, subject, userId, terminal);
    }


    private int getCategory(int subject) {
        if (categoryHashMap.containsKey(subject)) {
            return categoryHashMap.get(subject);
        }
        int category = subjectDubboService.getCatgoryBySubject(subject);
        categoryHashMap.put(subject, category);
        return category;
    }

    public StandardCard createAnswerCard(final Paper paper, int subject, long userId, int terminal) throws WaitException, BizException {
//        if (subject < 0) {
        subject = paper.getCatgory();
//        }
        StandardCard standardCard = StandardCard.builder().build();
        final int qcount = paper.getQcount();
        standardCard.setPaper(paper);
        standardCard.setUserId(userId);
        //final long id = IdClient.getClient().nextCommonId();
        final long id = Long.valueOf(String.valueOf(System.nanoTime()) + String.valueOf(System.currentTimeMillis()).substring(11));

        standardCard.setId(id);
        standardCard.setPaper(paper);
        standardCard.setDifficulty(paper.getDifficulty());

        int[] intAnswers = new int[qcount];
        standardCard.setAnswers(Arrays.stream(intAnswers).mapToObj(String::valueOf).toArray(String[]::new));
        standardCard.setCatgory(getCategory(subject));
        standardCard.setSubject(subject); //注意此处用的是用户当前设置的科目，而不是试卷的科目
        standardCard.setCreateTime(System.currentTimeMillis());
        standardCard.setCardCreateTime(System.currentTimeMillis());

        standardCard.setExpendTime(0);
        standardCard.setName(paper.getName());
        standardCard.setRcount(0);//正确数量
        standardCard.setWcount(0);//错误数量
        standardCard.setUcount(qcount);//未做数量
        standardCard.setCorrects(new int[qcount]);
        standardCard.setStatus(AnswerCardStatus.CREATE);
        standardCard.setTerminal(terminal);
        standardCard.setTimes(new int[qcount]);
        standardCard.setExpendTime(0);

        standardCard.setRemainingTime(paper.getTime());//设置剩余时间
        standardCard.setDoubts(new int[qcount]);

        //模考和估分的试卷，答题卡类型
        switch (paper.getType()) {
            case PaperType.CUSTOM_PAPER:
                standardCard.setType(AnswerCardType.SIMULATE);
                break;

            case PaperType.TRUE_PAPER:
                standardCard.setType(AnswerCardType.TRUE_PAPER);
                break;

            case PaperType.MATCH:
                standardCard.setType(AnswerCardType.MATCH);
                break;

            case PaperType.ESTIMATE_PAPER:
                standardCard.setType(AnswerCardType.ESTIMATE);
                break;

            case PaperType.FORMATIVE_TEST_ESTIMATE:
                standardCard.setType(AnswerCardType.FORMATIVE_TEST_ESTIMATE);
                break;

            case PaperType.MATCH_AFTER:
                standardCard.setType(AnswerCardType.MATCH_AFTER);
                break;
            case PaperType.SMALL_ESTIMATE:
                standardCard.setType(AnswerCardType.SMALL_ESTIMATE);
                break;
            case PaperType.APPLETS_PAPER:
                standardCard.setType(AnswerCardType.APPLETS_PAPER);
        }
        /**
         * 初始化各个模块的完成情况，0初始化，1进行中 2结束
         * （暂时只有招警机考需要维护该状态）
         */
        HashMap<Integer, Integer> modulesStatus = new HashMap<>();
        HashMap<Integer, Long> moduleCreateTime = new HashMap<>();

        if (CollectionUtils.isNotEmpty(paper.getModules())) {
            paper.getModules().forEach(i -> {
                modulesStatus.put(i.getCategory(), ModuleAnswerStatus.INIT);
                moduleCreateTime.put(i.getCategory(), -1L);
            });

        }
        standardCard.setModuleStatus(modulesStatus);
        standardCard.setModuleCreateTime(moduleCreateTime);

        answerCardDao.saveWithReflectQuestion(standardCard);
        standardCard.setIdStr(id + "");
        standardCard.setCurrentTime(System.currentTimeMillis());
        return standardCard;
    }

    /**
     * 提交答案（更新mongo答题卡数据，如果第一次答卷，发送统计相关的消息队列对答题行为进行统计）
     *
     * @param practiceId 练习id
     * @param userId     用户id
     * @param answers    用户答案
     * @param area
     * @return
     * @throws BizException
     * @Param lastIndex 用户最后答题位置
     */
    public AnswerCard submitAnswers(long practiceId, long userId, Collection<Answer> answers, int area, boolean isCop) throws BizException {
        //logger.info("查询答题卡信息 =》》{}", practiceId);
        final AnswerCard answerCard = answerCardDao.findById(practiceId);
        //logger.info("答题卡信息类别是 ：{},科目是：{}", answerCard.getType(),answerCard.getCatgory());

        if (answerCard == null) {//答题卡未找到
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND, "答题卡不存在");
        }
        /**
         * 如果是模考大赛，限定最后提交答案期限，超过期限的请求都不做处理
         */
        if (answerCard.getType() == AnswerCardType.MATCH) {
            StandardCard standardCard = (StandardCard) answerCard;
            Paper paper = standardCard.getPaper();
            if (null != paper) {
                EstimatePaper estimatePaper = (EstimatePaper) paper;
                long current = System.currentTimeMillis();
                long deadline = estimatePaper.getEndTime() + TimeUnit.MINUTES.toMillis(10);
                if (current > deadline) {
                    logger.error("提交答案的时间超出期限：{}", practiceId);
                    return answerCard;
                }
            }
        }

        //矫正是否是招警机考
        int subject = answerCard.getSubject();
        if (subject == 100100173) {
            isCop = true;
        }

        //logger.info("userId = {},dbUserId = {},result = {]",userId,answerCard.getUserId(),answerCard.getUserId() != userId);
        return handlerAnswerCardInfo(userId, answers, area, isCop, answerCard);
    }

    /**
     * @param userId
     * @param answers
     * @param area
     * @param isCop      设定分数规则88
     * @param answerCard
     * @return
     * @throws BizException
     */
    public AnswerCard handlerAnswerCardInfo(long userId, Collection<Answer> answers, int area, boolean isCop, AnswerCard answerCard) throws BizException {
        if (answerCard.getUserId() != userId) {//不是该用户的答题
            throw new BizException(CommonErrors.PERMISSION_DENIED);
        }
        long practiceId = answerCard.getId();

        /**
         * 这种情况只可能出现在Android最后交卷下，ios和pc最后交卷都是全量交卷，即使没有答题，也不会是空字符串
         */
        if (CollectionUtils.isEmpty(answers)) {//没有新提交的答案,则直接返回
            return answerCard;
        }

        /**
         * 这个防止一道题的形成两个答案
         * 去掉重复的答案
         */
        final HashMap<Integer, Answer> hashMap = Maps.newHashMap();
        answers.forEach(answer -> {
            hashMap.put(answer.getQuestionId(), answer);
        });
        //去重后的答案（用户最新提交的答案）
        answers = hashMap.values();
        List<Integer> questions = null;
        //对mongo中存储的用户答题卡进行判断，获取考试试题数组
        if (answerCard instanceof PracticeCard) {
            questions = ((PracticeCard) answerCard).getPaper().getQuestions();
        } else if (answerCard instanceof StandardCard) {
            questions = ((StandardCard) answerCard).getPaper().getQuestions();
        }
        //用户的原有答案
        final String[] answersArray = answerCard.getAnswers();
        //每道题正确错误的标识
        final int[] corrects = answerCard.getCorrects();
        //用户已经在各个道题上消耗的时间
        final int[] times = answerCard.getTimes();
        //用户之前每道题题的疑问标识
        final int[] doubts = answerCard.getDoubts();
        //用户新提交的有效答案
        final ArrayList<Answer> newAnswers = Lists.newArrayListWithCapacity(answers.size());
        for (Answer answer : answers) {//遍历提交的答案,过滤掉重复提交的答案和无效的答案，校验用时，让用时合理化
            final int questionId = answer.getQuestionId();
            //查询出试题所在索引位置
            final int index = questions.indexOf(questionId);

            if (index < 0) {//说明该试题不存在
                logger.error("questionId={} not in practiceId={}", questionId, practiceId);
                throw new BizException(PracticeErrors.SUBMIT_ANSWER_QUESTION_NO_EXIST);
            }
            //先修正时间
            if (answer.getTime() > MAX_ANSWER_TIME) {//检查答题时间是否超越了最大值
                answer.setTime(MAX_ANSWER_TIME);
            } else if (answer.getTime() < 1 && !"0".equals(answer.getAnswer())) { //不合理的时间设置为50秒
                answer.setTime(50);
            }
            // 如果是客户端传过来的是空 则默认为未作答
            if (StringUtils.isEmpty(answer.getAnswer())) {
                answer.setAnswer("0");
            }
            //如果该答案和已有的不一样,说明是新答案,否则说明是重复提交的
            //logger.info("answer={},is equal 0:{}", answer, answer.getAnswer().equals("0"));
            if (answer.getAnswer().equals("0")) {
                continue;
            }

            //重复提交答案（两次答案一样）则不进行处理,未作答（答案为0）的也不进行处理
            if (answersArray[index].equals(answer.getAnswer())) {
                continue;
            }

            //添加到有效答案列表
            newAnswers.add(answer);
        }

        //用户最后做到的题的索引处,默认做到第一题
        int lastIndex = -1;
        /**
         * update bu lijun 2018-02-26
         * for (Answer answer : answers) ->  for (Answer answer : newAnswers)
         * 上面部分逻辑实现了 重复答案过滤 和 未做答案过滤,此处却使用了未过滤之前的数组
         */
        for (Answer answer : newAnswers) {//遍历上传的答题结果
            final int questionId = answer.getQuestionId();
            //查询出试题所在索引位置
            final int index = questions.indexOf(questionId);
            //答题记录
            answersArray[index] = answer.getAnswer();

            //是否正确
            corrects[index] = answer.getCorrect();
            //答题时间（这里对有效的答案给予新的用时）
            times[index] = answer.getTime();
            //答题,答到的最大的索引
            //lastIndex = Math.max(lastIndex,index);
        }
        /**
         * 矫正 未答的时间问题
         */
        for (Answer answer : answers) {
            final int questionId = answer.getQuestionId();
            //查询出试题所在索引位置
            final int index = questions.indexOf(questionId);
            //跟上面的有效试题的时间统计，整合起来便是所有新提交答案的题目，都得用最新的时间
            //用户重复提交的试题（之前只处理了答案不同的，答案相同的也要处理）或者答题卡上没有答案的试题（第一次交卷或者一直没有答案的题目，如果有时间消耗也要计算）
            if (answersArray[index].equals(answer.getAnswer()) || answersArray[index].equals("0")) {
                times[index] = answer.getTime();
            }
            //疑问
            if (doubts != null) {
                doubts[index] = answer.getDoubt();
            }
        }
        //答题总耗时
        int elapsedTime = Arrays.stream(times).sum();
        //标准答题卡需要处理剩余时间
        if (answerCard.getRemainingTime() > 0) {//说明是倒计时类答题卡
            //答题总时间
            int allTime = answerCard.getExpendTime() + answerCard.getRemainingTime();
            //则设置剩余时间,防止出现<0的情况
            answerCard.setRemainingTime(Math.max(allTime - elapsedTime, 0));
        }

        answerCard.setExpendTime(elapsedTime);

        int rcount = 0;//答题正确数量
        int wcount = 0;//答题错误数量
        int ucount = 0;//未做答题数量
        for (int correct : corrects) {
            if (correct == QuestionCorrectType.RIGHT) {
                rcount++;
            } else if (correct == QuestionCorrectType.WRONG) {
                wcount++;
            } else if (correct == QuestionCorrectType.UNDO
                    || correct == QuestionCorrectType.CANNOT_ANSWER) {
                //未做或者不能作答
                ucount++;
            } else {//传入非法的数据,也视为答错
                wcount++;
            }
        }

        answerCard.setRcount(rcount);
        answerCard.setWcount(wcount);
        answerCard.setUcount(ucount);
        //在设置了做题数量后,才能设置分数
        if (isCop) {
            answerCard.setScore(ComputeScoreUtil.computeScoreCop(answerCard));
        } else {
            answerCard.setScore(ComputeScoreUtil.computeScore(answerCard));
        }

        /**
         * 如果lastIndex = -1 说明用户此次提交没有新的答案,但是可能已经有了答题信息,这样在第二次进入的时候初始答题位置会出现错误
         * 如果lastIndex > -1,表示用户没有完成，并且保存答案,需要给答题卡设置lastIndex,表示用户打开试卷,该做哪一道题
         * 如果做到最后一道题,则索引位置为最后一道题的,防止访问时数组越界
         * 此处代码删除了原本的 lastIndex 计算逻辑
         * add by lijun 2018-03-23
         */
        if (ArrayUtils.isNotEmpty(answersArray)) {
            //矫正 lastIndex 定位到最后一道已答的下一题
            for (int index = answersArray.length - 1; index >= 0; index--) {
                if (StringUtils.isNoneBlank(answersArray[index])
                        && !answersArray[index].equals("0")) {
                    lastIndex = index + 1;
                    break;
                }
            }
            lastIndex = Integer.min(lastIndex, questions.size() - 1);
        }

        answerCard.setLastIndex(Integer.max(0, lastIndex));

        /**
         * 计算平均答题速度
         */
        final int answerCount = (int) Arrays.stream(times).filter(time -> time > 0).count();
        int speed = 0;
        if (answerCount > 0) {
            speed = answerCard.getExpendTime() / answerCount;
        }
        answerCard.setSpeed(speed);
        //知识点汇总
        //将答题状态置为未做完状态（如果是完成状态重新计算分数，更新mongo数据，但不更改redis信息）
        if (answerCard.getStatus() != AnswerCardStatus.FINISH) {
            answerCard.setStatus(AnswerCardStatus.UNDONE);
        }
        //设置交卷时间
        answerCard.setCreateTime(System.currentTimeMillis());
        answerCardDao.save(answerCard);
        //如果已经交卷，则直接返回结果 by 周威 2017-12-21 18:50:35   1小时之内仍然接收答案  答题记录不处理了
        if (answerCard.getStatus() == AnswerCardStatus.FINISH) {
            //   logger.info("提交答案答题卡: saveAnswers answerCard={}",answerCard);
            return answerCard;
        }
        final UserDto userDto = userDubboService.findById(userId);

        if (userDto == null) {
            logger.warn("missed user id={}", userId);
        }

//        logger.info("提交答案答题卡 -2 : saveAnswers answerCard={}",answerCard);
//        logger.info("提交答题卡 saveAnswers userDto =  {}",userDto);
        //机器人的答案不进行处理
        if (CollectionUtils.isNotEmpty(newAnswers) && !(userDto != null && userDto.isRobot())) {
            //此处只发送新增加的答案,防止统计重复
            final UserAnswers userAnswers = UserAnswers.builder()
                    .uid(userId)
                    .practiceId(practiceId)
                    .area(area)
                    .subject(answerCard.getSubject())
                    .catgory(answerCard.getCatgory())
                    .submitTime(System.currentTimeMillis())
                    .answers(newAnswers)
                    .build();

            //发送提交答案的事件
            logger.info("统计信息 : ={}", userAnswers);
            //三个队列都会接收到这个消息，分别在knowledge，arena,question三个项目被消费
            //knowledge统计智能刷题缓存，用户做题记录，错题记录缓存
            //question 统计试题的准确率等数据
            //arena 晋级赛场推送用户成绩给同赛场的其他用户
            rabbitTemplate.convertAndSend(RabbitMqConstants.SUBMIT_ANSWERS, "", userAnswers);

            /**
             * 7.0版本 课堂练习发送答题数据到数据中心(随堂练习)
             */
            if (answerCard instanceof PracticeCard && ((PracticeCard) answerCard).getPaper() instanceof PracticeForCoursePaper) {
                PracticeForCoursePaper practiceForCoursePaper = (PracticeForCoursePaper) ((PracticeCard) answerCard).getPaper();
                //如果是带随堂练的录播提交答案需要统计数据 type=1为随堂练 coursetype=1为录播
                if (practiceForCoursePaper.getType() == 1
                        && practiceForCoursePaper.getCourseType() == CourseType.RECORD) {
                    //返送统计信息队列消息
                    courseAnswerCardSend(newAnswers, userId,
                            practiceForCoursePaper.getCourseId(), practiceForCoursePaper.getCourseType(), practiceForCoursePaper.getType());
                    logger.info("用户:{},随堂练录播统计数据", answerCard.getUserId());
                    HashOperations<String, Object, String> opsForHash = redisTemplate.opsForHash();
                    String practiceKey = PaperRedisKeys.getPracticeCourseIdAndCourseTypeKey(
                            practiceForCoursePaper.getCourseId(), practiceForCoursePaper.getCourseType());
                    opsForHash.put(practiceKey, String.valueOf(answerCard.getId()),
                            JSONObject.toJSONString(AnswerInfoBo.builder().speed(speed).rcount(rcount).build()));
                }

            }

        }
        return answerCard;
    }

    /**
     * 大数据消费（用户的答题行为数据）
     *
     * @param practiceId
     */
    private void sendUserAnswerCardInfo(long practiceId) {
        AnswerCard answerCard = answerCardDao.findById(practiceId);
        int status = answerCard.getStatus();
        if (status != AnswerCardStatus.FINISH) {
            return;
        }
        int[] times = answerCard.getTimes();
        int[] corrects = answerCard.getCorrects();
        HashMap<Object, Object> mapData = Maps.newHashMap();
        mapData.put("times", times);
        if (answerCard instanceof StandardCard) {
            List<Integer> ids = ((StandardCard) answerCard).getPaper().getQuestions();
            mapData.put("questions", ids.toArray());
        } else if (answerCard instanceof PracticeCard) {
            List<Integer> ids = ((PracticeCard) answerCard).getPaper().getQuestions();
            if (ids != null) {
                mapData.put("questions", ids.toArray());
            }
        }
        mapData.put("corrects", corrects);
        mapData.put("userId", answerCard.getUserId());
        mapData.put("subject", answerCard.getSubject());
        mapData.put("createTime", answerCard.getCreateTime());
        logger.info("send answer-card message:{}", JsonUtil.toJson(mapData));
        try {
            rabbitTemplate.convertAndSend("answer-card", "", mapData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 提交答题卡
     * 所有交卷逻辑共用的方法
     *
     * @param practiceId
     * @param userId
     * @param answers
     * @param area
     * @return
     * @throws BizException
     */
    public AnswerCard submitPractice(long practiceId, long userId, List<Answer> answers, int area, int terminal, String cv) throws BizException {
        //先提交答案
        AnswerCard answerCard = submitAnswers(practiceId, userId, answers, area, false);
        /**
         * updateBy lijun 2018-02-26
         * 原始代码 返回的 answerCard 有 final 修饰符
         * 此处返回的 AnswerCard 与缓存值可能不一样,从数据库重新获取 进行矫正
         */
        if (answerCard == null) {//答题卡未找到
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND, "答题卡不存在");
        }
        //
        answerCard = answerCardDao.findById(answerCard.getId());
        //试卷作答总量统计
        handlerPaperSubmit(answerCard);
        System.out.println("answerCard = " + JsonUtil.toJson(answerCard));
        //flag=true不是第一次提交flag=false是第一次提交
        Boolean flag = answerCard.getStatus() == AnswerCardStatus.FINISH;
        System.out.println("flag = " + flag);
        List<Integer> questions = null;
        if (answerCard instanceof PracticeCard) {
            questions = ((PracticeCard) answerCard).getPaper().getQuestions();
        } else if (answerCard instanceof StandardCard) {
            questions = ((StandardCard) answerCard).getPaper().getQuestions();
        }
        /**
         * updateBy lijun 2018-03-27
         * 修改了答题卡中正确率统计的情况
         * old code : questionPointDubboService.questionPointSummary()
         */
        //知识点统计汇总（查看报告中知识点树形结构展示答题情况用到）
        final List<QuestionPointTree> pointTrees = questionPointDubboService.questionPointSummaryWithTotalNumber(questions, answerCard.getCorrects(), answerCard.getTimes());
        PracticePointsSummary pointsSummary = PracticePointsSummary.builder()
                .practiceId(practiceId)
                .points(pointTrees)
                .build();


        switch (answerCard.getType()) {
            case AnswerCardType.SIMULATE:
            case AnswerCardType.TRUE_PAPER:
            case AnswerCardType.ESTIMATE:
            case AnswerCardType.MATCH_AFTER:
            case AnswerCardType.SMALL_ESTIMATE:
            case AnswerCardType.APPLETS_PAPER:
                setCardMeta(answerCard);
                //添加完成的练习
                paperUserMetaService.addFinishPractice(userId, ((StandardCard) answerCard).getPaper().getId(), practiceId);
                break;
            case AnswerCardType.MATCH:
                //将成绩存到redis中，总体累加分也是
                setCardMeta(answerCard);
                //地区排名等数据的统计
                matchService.setMatchCardMeta((StandardCard) answerCard);
                //如果模考大赛包括申论，将行测成绩在存一份，跟申论试卷对应，方便申论成绩出来后，计算模考大赛总分
                matchService.setRedisMetaWithEssay((StandardCard) answerCard);
                //模考大赛在完成后reids不保留答题卡的id(删除)
                matchService.removeFromMatchPracticeSet((StandardCard) answerCard);
                //将完成的实体练习（有真实试卷）的答题记录保存下来
                paperUserMetaService.addFinishPractice(userId, ((StandardCard) answerCard).getPaper().getId(), practiceId);
                break;

            case AnswerCardType.CUSTOMIZE_PAPER:
                practiceCardService.updateUnfinishedPointList(answerCard, CustomizeEnum.ModeEnum.Write);
            case AnswerCardType.CUSTOMIZE_PAPER_RECITE:
                practiceCardService.updateUnfinishedPointList(answerCard, CustomizeEnum.ModeEnum.Look);
                break;
        }

        try {
            //插入知识点汇总记录记录（mysql）
            practicePointsSummaryDao.insert(pointsSummary);
        } catch (DuplicateKeyException e) {//key冲突说明已经插入过,直接返回最新数据即可
            logger.warn("ex", e);
            return findById(practiceId, userId);
        }

        answerCard.setPoints(pointsSummary.getPoints());
        //只有这里可以将提交的答题卡转为完成状态
        answerCard.setStatus(AnswerCardStatus.FINISH);
        final Update update = Update.update("status", AnswerCardStatus.FINISH);
        //更新答题卡的状态
        answerCardDao.update(answerCard.getId(), update);
        //发送用户试题作答情况
        sendUserAnswerCardInfo(answerCard.getId());
        Map data = new HashMap<>(4);
        data.put("id", answerCard.getId());
        data.put("type", answerCard.getType());
        if (!AnswerCardUtil.judgeUserCv(terminal, cv)) {
            AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transInt);
        } else {
            AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transDouble);
        }
        long addGiftInfoForEstimateAnswerCardBeginTime = System.currentTimeMillis();
        answerCard = utilComponent.addGiftInfoForEstimateAnswerCard(answerCard);
        logger.info(">>>>>>处理活动大礼包时间 = {}", System.currentTimeMillis() - addGiftInfoForEstimateAnswerCardBeginTime);
        if (flag) {
            return answerCard;
        }
        //发送提交试卷的事件
        try {
            rabbitTemplate.convertAndSend(RabbitMqConstants.SUBMIT_PRACTICE_EXCHANGE, "", data);
            System.out.println("send message :" + RabbitMqConstants.SUBMIT_PRACTICE_EXCHANGE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return answerCard;
    }

    /**
     * 统计试卷被做过的次数
     *
     * @param answerCard
     */
    private void handlerPaperSubmit(AnswerCard answerCard) {
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            if (null != paper) {
                String paperSubmitKey = PaperRedisKeys.getPaperSubmitKey(paper.getId(), answerCard.getType());
                SetOperations setOperations = redisTemplate.opsForSet();
                setOperations.add(paperSubmitKey, answerCard.getId() + "");
            }
        }
    }


    /**
     * 模考估分，真题演练，模考大赛，添加统计处理：如全站平均分，击败比例
     *
     * @param answerCard
     */
    public void setCardMeta(AnswerCard answerCard) {
        final StandardCard standardCard = (StandardCard) answerCard;

        int qcount = standardCard.getPaper().getQcount();
        final int paperId = standardCard.getPaper().getId();
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final ValueOperations valueOperations = redisTemplate.opsForValue();
        //未做数量=题目数量时,不添加到排名统计中去
        if (qcount != standardCard.getUcount()) {

            String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
            String paperScoreSum = PaperRedisKeys.getPaperScoreSum(paperId);
            if (answerCard.getType() == AnswerCardType.MATCH_AFTER) {
                paperPracticeIdSore = PaperRedisKeys.getKeyWithType(paperPracticeIdSore, AnswerCardType.MATCH_AFTER);
                paperScoreSum = PaperRedisKeys.getKeyWithType(paperScoreSum, AnswerCardType.MATCH_AFTER);
            }
            /**
             * updateBy lijun
             * 2018-03-09
             * 如果提交两次,修复后续在总积分中计算两次的情况
             */
            Double score = zSetOperations.score(paperPracticeIdSore, standardCard.getId() + "");
            if (null == score) {
                score = 0D;
            }
            //将练习id和其score写入zset
            zSetOperations.add(paperPracticeIdSore, standardCard.getId() + "", standardCard.getScore());

            //累加分数,用于计算平均分
            valueOperations.increment(paperScoreSum, standardCard.getScore() - score);
        }

        CardUserMeta cardUserMeta = handlerMetaService.getCardUserMeta(standardCard);
        standardCard.setCardUserMeta(cardUserMeta);
    }


    /**
     * 根据跟定参数查询对应的答题卡列表,
     * 主要用于"答题历史"功能
     *
     * @param userId
     * @param cursor
     * @param size
     * @param cardType
     * @param cardTime
     * @return
     */
    public List<AnswerCard> getAnswerCards(long userId, long cursor, int size, int cardType, String cardTime) {
        return answerCardDao.findAnserCards(userId, cursor, size, cardType, cardTime);
    }

    /**
     * 根据id查看用户试卷信息
     *
     * @param id
     * @param uid
     * @return
     * @throws BizException
     */
    public AnswerCard findById(long id, long uid) throws BizException {
        AnswerCard answerCard = answerCardDao.findById(id);
        if (answerCard == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        if (answerCard.getUserId() != uid) {
            throw new BizException(CommonErrors.PERMISSION_DENIED);
        }
        if (answerCard.getStatus() == AnswerCardStatus.FINISH) {//已经结束的,则设置知识点汇总
            answerCard = utilComponent.addGiftInfoForEstimateAnswerCard(answerCard);
            //logger.info("gift 完成");
            //查询设置知识点汇总
            final PracticePointsSummary practicePointsSummary = practicePointsSummaryDao.findByPracticeId(answerCard.getId());
            if (practicePointsSummary != null) {
                answerCard.setPoints(practicePointsSummary.getPoints());
            }
        } else {
            answerCard.setPoints(new ArrayList<>());
        }

        //如果是标准答题卡,则设置排名统计信息
        if (answerCard instanceof StandardCard) {
            StandardCard standardCard = (StandardCard) answerCard;
            final CardUserMeta cardUserMeta = handlerMetaService.getCardUserMeta(standardCard);
            //logger.info("cardUserMeta 完成");
            standardCard.setCardUserMeta(cardUserMeta);

            filterMatch(standardCard);
            //logger.info("filterMatch 完成");
        }
        //这个代码是为了处理脏数据（lastIndex=-1）
        answerCard.setLastIndex(Integer.max(0, answerCard.getLastIndex()));

        return answerCard;
    }

    /**
     * 对模考大赛答题卡单独的处理
     *
     * @param card
     */
    private void filterMatch(StandardCard card) {
        if (card.getType() != AnswerCardType.MATCH) {
            return;
        }

        if (card.getStatus() == AnswerCardStatus.FINISH) { //职位排名统计信息
            card.setMatchMeta(matchService.findMatchCardUserMeta(card));
        } else { //答题卡未提交
            EstimatePaper paper = (EstimatePaper) card.getPaper();
            long endTime = paper.getEndTime();
            long currentTime = System.currentTimeMillis();
            long startTime = paper.getStartTime();

            //计算剩余时间
            if (startTime <= currentTime && currentTime < endTime) {
                int remainingTime = (int) (endTime - currentTime) / 1000;
                card.setRemainingTime(Math.max(0, remainingTime));
            }
        }
    }

    /**
     * 根据答题卡id删除答题记录
     *
     * @param id
     */
    public void deleteById(long id, long uid) throws BizException {
        AnswerCard answerCard = answerCardDao.findById(id);
        boolean isSuccess = answerCardDao.delete(id, uid);
        if (isSuccess) {
            logger.info("single answerCardRecord deleted success,id={}", id);
            int subject = answerCard.getSubject();
            clearUpForPointTree(uid, subject);
        } else {
            logger.info("single answerCardRecord deleted fail,id={}", id);
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

    }

    /**
     * 清理已被删除的未完成答题卡（专项训练上的）
     *
     * @param userId
     * @param subject
     */
    private void clearUpForPointTree(long userId, int subject) {
        ListOperations<String, String> opsForList = redisTemplate.opsForList();
        String unfinishedPointKey = RedisKnowledgeKeys.getUnfinishedPointListKey(userId, subject);
        List<String> unfinishedList = opsForList.range(unfinishedPointKey, 0, -0);
        if (CollectionUtils.isNotEmpty(unfinishedList)) {
            //知识点id_练习id
            unfinishedList.stream().filter(last -> {
                long practiceId = Long.valueOf(last.split("_")[1]);
                AnswerCard card = answerCardDao.findById(practiceId);
                return null == card || card.getStatus() == AnswerCardStatus.DELETED;
            }).forEach(i -> opsForList.remove(unfinishedPointKey, 0, i));
        }
    }


    /**
     * 模考估分试卷的交卷
     *
     * @param practiceId
     * @param userId
     * @param answers
     * @param area
     * @param uname
     * @param cardType
     * @return
     * @throws BizException
     */
    public Object submitEstimateAnswers(long practiceId, long userId, List<Answer> answers, int area, String uname, int cardType, int terminal, String cv) throws BizException {
        if (cardType == AnswerCardType.MATCH) {
            //模考大赛交卷逻辑都在这里
            matchService.submitMatchesAnswers(practiceId, userId, answers, area, uname);
            return SuccessMessage.create("请在本次模考活动结束后查看报告");
        }

        //交卷 TODO
        AnswerCard answerCard = submitPractice(practiceId, userId, answers, area, terminal, cv);

        StandardCard standardCard = (StandardCard) answerCard;
        EstimatePaper paper = (EstimatePaper) standardCard.getPaper();

        //对于考试时间结束后才能查看解析的试卷
        int mark = paper.getLookParseTime();
        if (mark == LookParseStatus.AFTER_END && paper.getEndTime() > System.currentTimeMillis()) {
            return SuccessMessage.create("请在本次模考活动结束后查看报告");
        }

        if (!AnswerCardUtil.judgeUserCv(terminal, cv)) {
            AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transInt);
        } else {
            AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transDouble);
        }
        return answerCard;
    }

    /**
     * 查询用户模考大赛中行测和申论总分的统计信息
     *
     * @param id
     * @param uid
     * @return
     */
    public AnswerCard findUserMateWithEssayById(long id, long uid) throws BizException {
        final AnswerCard answerCard = answerCardDao.findById(id);
        if (answerCard == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        //未结束的答题卡不能做统计
        if (answerCard.getStatus() != AnswerCardStatus.FINISH) {
            return answerCard;
        }
        if (answerCard.getUserId() != uid) {
            throw new BizException(CommonErrors.PERMISSION_DENIED);
        }
        //是否是联合考试

        //如果是标准答题卡,则设置排名统计信息
        if (answerCard instanceof StandardCard) {
            final StandardCard standardCard = (StandardCard) answerCard;
            final Match currentMatch = matchService.findCurrentMatch(standardCard.getPaper().getId());
            if (currentMatch != null && currentMatch.getEssayPaperId() > 0) {
                return countMatchWithEssay(standardCard, currentMatch);
            }
        }

        return answerCard;
    }

    /**
     * 查看 模考大赛、往期模考报告，带申论数据
     *
     * @param paperId 试卷ID
     * @param userId  用户ID
     * @return
     * @throws BizException
     */
    public AnswerCard getUserMatchAnswerCardMetaInfo(int paperId, long userId) throws BizException {
        MatchUserMeta userMeta = matchService.findMatchUserMeta(userId, paperId);
        if (userMeta == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        AnswerCard answerCard = findUserMateWithEssayById(userMeta.getPracticeId(), userId);
        /**
         * 该方法是新接口实现方法，只支持double类型
         */
//        BigDecimal bigDecimal = new BigDecimal(answerCard.getScore());
//        double score = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//        answerCard.setScore(score);
        AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transDouble);
        return answerCard;
    }


    /**
     * 查询统计信息
     * update by lizhenjuan  2019-04-29
     * 修改内容：原来学员分数,平均分,最高分,模考大赛职位最高分,职位平均分全都展示为整数,现在全都改为展示为小数
     *
     * @throws BizException
     */
    public AnswerCard findAnswerCardDetail(long id, long uid, int terminal, String cv) throws BizException {
        logger.info("查询试卷统计信息 ：{}，{}，{},{}", id, uid, terminal, cv);
        AnswerCard answerCard = findById(id, uid);
        if (!AnswerCardUtil.judgeUserCv(terminal, cv)) {
            AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transInt);
        } else {
            AnswerCardUtil.handlerDoubleScore(answerCard, AnswerCardUtil.transDouble);
        }
        sortPointTree(answerCard);

        //如果是招警机考的话
        if (answerCard.getSubject() == 100100173) {
            if (answerCard instanceof StandardCard) {
                ((StandardCard) answerCard).setIdStr(answerCard.getId() + "");
                ((StandardCard) answerCard).setCurrentTime(System.currentTimeMillis());
            }
            //重置剩余时间(距离创建时间超过两分钟)
            if (answerCard.getCardCreateTime() != null && answerCard.getType() != AnswerCardType.MATCH) {

                long l = System.currentTimeMillis() - answerCard.getCardCreateTime();
                if (l > 2 * 60 * 1000) {
                    answerCard.setRemainingTime(answerCard.getRemainingTime() - 2 * 60);
                } else {
                    answerCard.setRemainingTime(answerCard.getRemainingTime() - (int) l / 1000);

                }
            }

        }
        //小程序添加字段
        if (terminal == TerminalType.WEI_XIN_APPLET) {
			if (answerCard instanceof StandardCard) {
				logger.info("进入小程序补充数据");
                Paper paper = ((StandardCard) answerCard).getPaper();
                //地区
                int areaId = paper.getArea();
                String areaName = AreaConstants.getFullAreaNmae(areaId);
                if (StringUtils.isEmpty(areaName)) {
                    areaName = "国考";
                }

                //考试类型
                int categoryId = subjectDubboService.getCatgoryBySubject(paper.getCatgory());
                String categoryName = subjectDubboService.getCategoryNameById(categoryId);
                //考试科目名称
                String subjectName = "";
                List<SubjectTree> subjectTreeList = subjectDubboService.getSubjectTree();

                if (CollectionUtils.isNotEmpty(subjectTreeList)) {
                    Optional<SubjectTree> subjectTreeOptional = subjectTreeList.stream().flatMap(categoryTree ->
                            categoryTree.getChildrens().stream().filter(subjectTree -> subjectTree.getId() == paper.getCatgory())
                    ).findAny();
                    if (subjectTreeOptional.isPresent()) {
                        subjectName = subjectTreeOptional.get().getName();
                    }
                }
                StringBuilder paperNameBuilder = new StringBuilder();
                String paperName = paperNameBuilder.append(categoryName)
                        .append("-")
                        .append(subjectName)
                        .append("-")
                        .append(areaName).toString();

                ((StandardCard) answerCard).getPaper().setPaperName(paperName);
                ((StandardCard) answerCard).getPaper().setCategoryId(categoryId);
            }
        }
        return answerCard;
    }

    /**
     * 此处处理答题卡信息 中如果有知识点信息 且有试题列表的情况下,知识点信息和试题列表中一级标题顺序不一致
     * add by lijun 2018-03-24
     */
    public static void sortPointTree(AnswerCard answerCard) {
        BiFunction<QuestionPointTree, List<Module>, Integer> getIndex = (point, moduleList) -> {
            int index = 0;
            for (; index < moduleList.size(); index++) {
                if (point.getName().equals(moduleList.get(index).getName())) {
                    break;
                }
            }
            return index;
        };

        BiFunction<List<QuestionPointTree>, List<Module>, List<QuestionPointTree>> sortList = (points, moduleList) -> {
            if (CollectionUtils.isNotEmpty(moduleList) && CollectionUtils.isNotEmpty(points)) {
                //试题模块信息  试题节点信息都不为空
                //矫正试题统计顺序
                points.sort(Comparator.comparingInt(p -> getIndex.apply(p, moduleList)));
            }
            return points;
        };

        if (answerCard instanceof StandardCard) {
            StandardCard standardCard = (StandardCard) answerCard;

            List<QuestionPointTree> points = standardCard.getPoints();
            List<Module> moduleList = standardCard.getPaper().getModules();
            List<QuestionPointTree> pointTrees = sortList.apply(points, moduleList);

            answerCard.setPoints(pointTrees);
        }

        if (answerCard instanceof PracticeCard) {
            PracticeCard practiceCard = (PracticeCard) answerCard;

            List<QuestionPointTree> points = practiceCard.getPoints();
            List<Module> moduleList = practiceCard.getPaper().getModules();
            List<QuestionPointTree> pointTrees = sortList.apply(points, moduleList);

            answerCard.setPoints(pointTrees);
        }


    }

    /**
     * 计算所有联合模考大赛的成绩
     *
     * @param standardCard
     * @param currentMatch
     */
    private AnswerCard countMatchWithEssay(StandardCard standardCard, Match currentMatch) {
        //申论试卷还没结束，不能展示该信息
        if (System.currentTimeMillis() < currentMatch.getEssayEndTime()) {
            return null;
        }
        CompositeCard compositeCard = filterMatchWithEssay(currentMatch, standardCard);
        setCardMetaWithEssay(currentMatch, compositeCard);
        if (currentMatch.getEssayPaperId() != 0) {
            compositeCard.setEssayPaperId(currentMatch.getEssayPaperId());
        }
        return compositeCard;
    }

    /**
     * 用户总体分数地区排名信息
     *
     * @param currentMatch
     * @param card
     */
    private CompositeCard filterMatchWithEssay(Match currentMatch, StandardCard card) {
        if (card.getType() != AnswerCardType.MATCH) {
            return null;
        }
        EstimatePaper paper = (EstimatePaper) card.getPaper();
        long endTime = currentMatch.getEssayEndTime();
        long currentTime = System.currentTimeMillis();
        long startTime = paper.getStartTime();

        //计算剩余时间
        if (startTime <= currentTime && currentTime < endTime) {
            int remainingTime = (int) (endTime - currentTime) / 1000;
            card.setRemainingTime(Math.max(0, remainingTime));
        } else if (card.getStatus() == AnswerCardStatus.FINISH) {
            //职位排名统计信息
            card.setMatchMeta(matchService.findMatchCardUserMetaWithEssay(currentMatch, card));
        }
        CompositeCard compositeCard = new CompositeCard();
        BeanUtils.copyProperties(card, compositeCard);
        return compositeCard;
    }

    /**
     * 统计用户总分
     *
     * @param currentMatch
     * @param standardCard
     */
    private void setCardMetaWithEssay(Match currentMatch, CompositeCard standardCard) {
        final int paperId = standardCard.getPaper().getId();
        final long essayPaperId = currentMatch.getEssayPaperId();
        //联合考试用户总分统计
        final String paperPracticeIdSoreWithEssay = RedisKeyConstant.getMockUserTotalScoreKey(essayPaperId);
        //模考用户总分累加
        final String essayPaperScoreSum = RedisKeyConstant.getMockScoreSumKey(essayPaperId);

        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        final ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        //查询用户联合考试总分数
        Double score = zSetOperations.score(paperPracticeIdSoreWithEssay, standardCard.getUserId() + "");
        if (score == null) {
            score = 0D;
        }
        BigDecimal bigDecimal = new BigDecimal(score);
        double mScore = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        standardCard.setScore(mScore);
        Long total = zSetOperations.size(paperPracticeIdSoreWithEssay);//总记录数
        boolean totalFlag = true;  //有排名成绩
        if (total == null || total == 0) {//不存在总记录数，正常来说应该不存在
            total = 1L;
            totalFlag = false;
        }

        //本答题卡排名,redis rank命令,从0开始,也就是第一名的rank值为0
        Long rank = zSetOperations.reverseRank(paperPracticeIdSoreWithEssay, standardCard.getUserId() + "");//排名,按照分数倒排
        if (rank == null) {//排名不存在
            rank = total;//不存在说明是最后一名
        } else {
            rank = rank + 1;//排名是从0开始的所以要+1才是真的名次
        }

        final String essayScoreStr = valueOperations.get(essayPaperScoreSum);//该试卷总得分
        //如果缓存中娶不到总分，而zset中有值，则通过计算获得总分，并存入缓存
        Double sumScore = 0D;
        if (StringUtils.isBlank(essayScoreStr) && totalFlag) {
            Set<ZSetOperations.TypedTuple<String>> withScores =
                    zSetOperations.rangeWithScores(paperPracticeIdSoreWithEssay, 0, Long.MAX_VALUE);
            if (CollectionUtils.isNotEmpty(withScores)) {
                for (ZSetOperations.TypedTuple<String> source : withScores) {
                    sumScore += source.getScore();
                }
            }
            if (sumScore.intValue() != 0) {
                //总成绩存入缓存
                valueOperations.set(essayPaperScoreSum, sumScore + "");
            }
        }
        double average = 60;//默认值

        if (essayScoreStr != null && total > 0) {
            double allScore = Double.parseDouble(essayScoreStr);//答题卡所有分数和
            average = allScore / total;//平均分
        } else if (sumScore > 0) {
            double allScore = sumScore;//答题卡所有分数和
            average = allScore / total;//平均分
        }
        final int beatRate = (int) ((total - rank) * 100 / total);

        Double max = 0d;
        if (total != 0) {
            Set<ZSetOperations.TypedTuple<String>> withScores =
                    zSetOperations.reverseRangeWithScores(paperPracticeIdSoreWithEssay, 0, 0);

            if (CollectionUtils.isNotEmpty(withScores)) {
                max = new ArrayList<>(withScores).get(0).getScore();
            }
        }
        BigDecimal b = new BigDecimal(average);
        double averageVO = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        CardUserMeta cardUserMeta = CardUserMeta.builder()
                .average(averageVO)
                .beatRate(beatRate)
                .rank(rank.intValue())
                .total(total.intValue())
                .max(max)
                .build();
        standardCard.setCardUserMeta(cardUserMeta);
        String lineTestScoreKey = RedisKeyConstant.getUserPracticeScoreKey(essayPaperId);
        Double lineScore = zSetOperations.score(lineTestScoreKey, standardCard.getUserId() + "");
        if (lineScore == null) {
            lineScore = 0D;
        }
        BigDecimal lineDecimal = new BigDecimal(lineScore);
        double lScore = lineDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        standardCard.setLineTestScore(lScore);
        standardCard.setEssayScore((mScore - lineScore < 0) ? 0 : mScore - lScore);
    }

    /**
     * 发送课程答题统计信息
     *
     * @param list           试题信息  questionId、time(做题时长)、correct(是否正确)、subjectId(科目)、knowledgePoint(所属知识点)
     * @param userId         用户ID
     * @param courseWareId   课件ID
     * @param courseWareType 课件类型
     * @param questionSource 试题来源（课中练习：1、课后练习：2）
     */
    private void courseAnswerCardSend(List<Answer> list, final long userId,
                                      final long courseWareId, final int courseWareType, final int questionSource) {
        List<Integer> questionIdList = list.stream()
                .map(Answer::getQuestionId)
                .collect(Collectors.toList());
        List<Question> questionDataList = questionDubboService.findBath(questionIdList);
        if (CollectionUtils.isNotEmpty(questionDataList)) {
            List<HashMap<String, Object>> sendDataList = questionDataList.stream()
                    .map(question -> {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("questionId", question.getId());
                        map.put("userId", userId);
                        //获取答案信息
                        Answer answerData = list.stream().filter(answer -> answer.getQuestionId() == question.getId())
                                .findFirst()
                                .orElseGet(() -> {
                                    Answer answer = new Answer();
                                    answer.setTime(0);
                                    answer.setAnswer("0");
                                    answer.setCorrect(QuestionCorrectType.UNDO);
                                    return answer;
                                });
                        map.put("time", answerData.getTime());
                        map.put("correct", answerData.getCorrect());
                        map.put("knowledgePoint", question instanceof GenericQuestion ? ((GenericQuestion) question).getPoints().get(2) : -1);
                        map.put("questionSource", questionSource);
                        map.put("courseWareId", courseWareId);
                        map.put("courseWareType", courseWareType);
                        map.put("submitTime", System.currentTimeMillis());
                        map.put("subjectId", question.getSubject());
                        //阶段
                        map.put("step", -1);
                        //是否听过课 //1听过，0没听过
                        map.put("listened", -1);
                        map.put("answer", answerData.getAnswer());
                        return map;
                    })
                    .collect(Collectors.toList());
            //TODO: 正式环境取消
            logger.info("答题卡信息统计发送 ======》{}", sendDataList);
            rabbitTemplate.convertAndSend("", RabbitMqConstants.COURSE_BREAKPOINT_PRACTICE_SAVE_DB_QUEUE, JSONObject.toJSONString(sendDataList));
        }
    }

    public Object upModuleStatus(long id, long userId, int category, int status) throws BizException {
        AnswerCard answerCard = findById(id, userId);
        logger.info("答题卡ID是:{},userId是:{},category:{},status:{},答题卡状态:{}", id, userId, category, status);
        Map<Integer, Integer> moduleStatus = answerCard.getModuleStatus();
        Integer oldStatus = moduleStatus.getOrDefault(category, 0);
        logger.info("原始状态是:{}", oldStatus);
        moduleStatus.put(category, status);
        Map<Integer, Long> moduleCreateTime = answerCard.getModuleCreateTime() == null ? new HashMap<Integer, Long>() : answerCard.getModuleCreateTime();
        if (status == ModuleAnswerStatus.ANSWERING && oldStatus != ModuleAnswerStatus.ANSWERING) {
            logger.info("模块状态更新");
            moduleCreateTime.put(category, System.currentTimeMillis());
        }
        answerCardDao.save(answerCard);
        return null;
    }


    /**
     * 提交答案（更新mongo答题卡数据，如果第一次答卷，发送统计相关的消息队列对答题行为进行统计）
     *
     * @param practiceId 练习id
     * @param userId     用户id
     * @param answers    用户答案
     * @param area
     * @return
     * @throws BizException
     * @Param lastIndex 用户最后答题位置
     */
    public AnswerCard periodTestsubmitAnswers(long practiceId, long userId, Collection<Answer> answers, int area) throws BizException {
        final AnswerCard answerCard = answerCardDao.findById(practiceId);

        if (answerCard == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND, "答题卡不存在");
        }
        //TODO 校验是否可以提交答案

        return handlerAnswerCardInfo(userId, answers, area, false, answerCard);
    }


    /**
     * 阶段测试答题卡交卷
     *
     * @param practiceId
     * @param userId
     * @param answers
     * @param area
     * @return
     * @throws BizException
     */
    public Object submitPeriodTestAnswerCard(long practiceId, long userId, List<Answer> answers, int area,
                                             long syllabusId, String uName) throws BizException {
        AnswerCard answerCard = null;
        try {
            answerCard = submitPeriodTestPractice(practiceId, userId, answers, area, syllabusId);
            StandardCard standardCard = (StandardCard) answerCard;
            EstimatePaper paper = (EstimatePaper) standardCard.getPaper();

            // 将zset中的答题卡删除
            removeAnswerCardFromRedis(answerCard, syllabusId);

            // 发送阶段测试完成事件给php
            PeriodTestSubmitlPayload payload = PeriodTestSubmitlPayload.builder().isFinish(1).syllabusId(syllabusId)
                    .userName(uName).papeId(paper.getId()).userId(userId).build();
            rabbitTemplate.convertAndSend("", RabbitMqConstants.PERIOD_TEST_SUBMIT_CARD_INFO, JsonUtil.toJson(payload));
            if (paper.getStartTimeIsEffective() == 1) {
                // 对于考试时间结束后才能查看解析的试卷
                int mark = paper.getLookParseTime();
                if (mark == LookParseStatus.IMMEDIATELY && paper.getEndTime() > System.currentTimeMillis()) {
                    return SuccessMessage.create("请在本次阶段测试活动结束后查看报告");
                }
            }
        } catch (Exception e) {
            logger.error("阶段测试答题卡交卷,报错, practiceId:{}, error:{}", practiceId, e.getMessage());
            e.printStackTrace();
        }
        return answerCard;
    }


    /**
     * 根据id查看用户试卷信息
     *
     * @param practiceId
     * @param uid
     * @return
     * @throws BizException
     */
    public AnswerCard findPeriodTestAnswerCardById(long practiceId, long uid) throws BizException {
        AnswerCard answerCard = answerCardDao.findById(practiceId);
        if (answerCard == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        if (answerCard.getUserId() != uid) {
            throw new BizException(CommonErrors.PERMISSION_DENIED);
        }
        if (answerCard.getStatus() == AnswerCardStatus.FINISH) {//已经结束的,则设置知识点汇总
            //查询设置知识点汇总
            final PracticePointsSummary practicePointsSummary = practicePointsSummaryDao.findByPracticeId(answerCard.getId());
            answerCard.setPoints(practicePointsSummary.getPoints());
        } else {
            answerCard.setPoints(new ArrayList<>());
        }

        //如果是标准答题卡,则设置排名统计信息
        if (answerCard instanceof StandardCard) {
            StandardCard standardCard = (StandardCard) answerCard;
            final CardUserMeta cardUserMeta = handlerMetaService.getCardUserMeta(standardCard);
            standardCard.setCardUserMeta(cardUserMeta);
        }
        //这个代码是为了处理脏数据（lastIndex=-1）
        answerCard.setLastIndex(Integer.max(0, answerCard.getLastIndex()));

        return answerCard;
    }

    /**
     * 将阶段测试得分和得分人数放入redis hash
     *
     * @param answerCard
     */
    public void setScoreAndCountByPaperId(AnswerCard answerCard, long syllabusId) {
        // 阶段测试交卷时分数存redis
        if (answerCard.getStatus() != AnswerCardStatus.FINISH) {
            StandardCard standardCard = (StandardCard) answerCard;
            Double score = ComputeScoreUtil.computeScore(answerCard);
            int userScore = Double.valueOf(score).intValue();
            HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
            if (standardCard.getPaper() != null) {
                String scoreKey = PeriodTestRedisKey.getPeriodTestPaper(standardCard.getPaper().getId(), syllabusId);
                opsForHash.increment(scoreKey, String.valueOf(userScore), 1);
            }
        }
    }

    /**
     * 将答题卡从zset中删除
     *
     * @param answerCard
     */
    public void removeAnswerCardFromRedis(AnswerCard answerCard, long syllabusId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String setValueString = answerCard.getId() + "_" + syllabusId;
        zSetOperations.remove(PeriodTestRedisKey.getPeriodTestAnswerCardUnfinshKey(), setValueString);
    }

    /**
     * 阶段测试提交答题卡
     * 所有交卷逻辑共用的方法（由于阶段测试需要大纲id单独的方法）
     *
     * @param practiceId
     * @param userId
     * @param answers
     * @param area
     * @return
     * @throws BizException
     */
    public AnswerCard submitPeriodTestPractice(long practiceId, long userId, List<Answer> answers, int area, long syllabusId) throws BizException {
        //先提交答案
        AnswerCard answerCard = submitAnswers(practiceId, userId, answers, area, false);
        /**
         * updateBy lijun 2018-02-26
         * 原始代码 返回的 answerCard 有 final 修饰符
         * 此处返回的 AnswerCard 与缓存值可能不一样,从数据库重新获取 进行矫正
         */
        if (answerCard == null) {//答题卡未找到
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND, "答题卡不存在");
        }
        //
        answerCard = answerCardDao.findById(answerCard.getId());
        //试卷作答总量统计
        handlerPaperSubmit(answerCard);

        //flag=true不是第一次提交flag=false是第一次提交
        Boolean flag = answerCard.getStatus() == AnswerCardStatus.FINISH;

        List<Integer> questions = null;
        if (answerCard instanceof PracticeCard) {
            questions = ((PracticeCard) answerCard).getPaper().getQuestions();
        } else if (answerCard instanceof StandardCard) {
            questions = ((StandardCard) answerCard).getPaper().getQuestions();
        }
        /**
         * updateBy lijun 2018-03-27
         * 修改了答题卡中正确率统计的情况
         * old code : questionPointDubboService.questionPointSummary()
         */
        //知识点统计汇总（查看报告中知识点树形结构展示答题情况用到）
        final List<QuestionPointTree> pointTrees = questionPointDubboService.questionPointSummaryWithTotalNumber(questions, answerCard.getCorrects(), answerCard.getTimes());
        PracticePointsSummary pointsSummary = PracticePointsSummary.builder()
                .practiceId(practiceId)
                .points(pointTrees)
                .build();


        switch (answerCard.getType()) {
            case AnswerCardType.FORMATIVE_TEST_ESTIMATE:
                //setCardMeta(answerCard);
                //自定义计算答题卡id和得分zset
                setPeriodCardMeta(answerCard, syllabusId);
                //添加完成的练习
                paperUserMetaService.addPeriodTestFinishPractice(userId, ((StandardCard) answerCard).getPaper().getId(), practiceId, syllabusId);
                break;
        }

        try {
            //插入知识点汇总记录记录（mysql）
            practicePointsSummaryDao.insert(pointsSummary);
        } catch (DuplicateKeyException e) {//key冲突说明已经插入过,直接返回最新数据即可
            logger.warn("ex", e);
            return findById(practiceId, userId);
        }

        answerCard.setPoints(pointsSummary.getPoints());

        if (answerCard.getStatus() != AnswerCardStatus.FINISH) {
            StandardCard standardCard = (StandardCard) answerCard;
            EstimatePaper paper = (EstimatePaper) standardCard.getPaper();
            if (paper.getStartTimeIsEffective() == 1) {
                if (paper.getEndTime() > answerCard.getCardCreateTime()) {
                    //将试题所得分数存redis key 试卷Id+大纲Id hashkey score count
                    setScoreAndCountByPaperId(answerCard, syllabusId);
                    //统计试卷正确率存zset key 试卷Id+大纲Id hashkey 正确率区间  人数
                    setPeriodTestAccuracyNum(answerCard, syllabusId);
                    //统计平均正确率
                    setPeriodTestQuestionAccuracy(answerCard, syllabusId);
                }
            } else {
                //将试题所得分数存redis key 试卷Id+大纲Id hashkey score count
                setScoreAndCountByPaperId(answerCard, syllabusId);
                //统计试卷正确率存zset key 试卷Id+大纲Id hashkey 正确率区间  人数
                setPeriodTestAccuracyNum(answerCard, syllabusId);
                //统计平均正确率
                setPeriodTestQuestionAccuracy(answerCard, syllabusId);

            }
        }

        //只有这里可以将提交的答题卡转为完成状态
        answerCard.setStatus(AnswerCardStatus.FINISH);

        final Update update = Update.update("status", AnswerCardStatus.FINISH);
        //更新答题卡的状态
        answerCardDao.update(answerCard.getId(), update);
        //发送用户试题作答情况
        sendUserAnswerCardInfo(answerCard.getId());
        Map data = new HashMap<>(4);
        data.put("id", answerCard.getId());
        data.put("type", answerCard.getType());
        if (answerCard instanceof StandardCard) {
            StandardCard standardCard = (StandardCard) answerCard;
            CardUserMeta cardUserMeta = standardCard.getCardUserMeta();
            if (cardUserMeta != null) {
                cardUserMeta.setAverage(new Double(cardUserMeta.getAverage()).intValue());
                cardUserMeta.setMax(new Double(cardUserMeta.getMax()).intValue());
            }
            MatchCardUserMeta matchMeta = standardCard.getMatchMeta();
            if (matchMeta != null) {
                matchMeta.setPositionAverage(new Double(matchMeta.getPositionAverage()).intValue());
                matchMeta.setPositionMax(new Double(matchMeta.getPositionMax()).intValue());
            }
        }
        long addGiftInfoForEstimateAnswerCardBeginTime = System.currentTimeMillis();
        answerCard = utilComponent.addGiftInfoForEstimateAnswerCard(answerCard);
        logger.info(">>>>>>处理活动大礼包时间 = {}", System.currentTimeMillis() - addGiftInfoForEstimateAnswerCardBeginTime);
        if (flag) {
            return answerCard;
        }
        //发送提交试卷的事件
        rabbitTemplate.convertAndSend(RabbitMqConstants.SUBMIT_PRACTICE_EXCHANGE, "", data);

        return answerCard;
    }

    private void setPeriodCardMeta(AnswerCard answerCard, long syllabusId) {
        final StandardCard standardCard = (StandardCard) answerCard;

        int qcount = standardCard.getPaper().getQcount();
        final int paperId = standardCard.getPaper().getId();
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final ValueOperations valueOperations = redisTemplate.opsForValue();
        // 未做数量=题目数量时,不添加到排名统计中去
        if (qcount != standardCard.getUcount()) {
            String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId) + "_" + syllabusId;
            String paperScoreSum = PaperRedisKeys.getPaperScoreSum(paperId) + "_" + syllabusId;
            paperScoreSum = PaperRedisKeys.getKeyWithType(paperScoreSum, AnswerCardType.FORMATIVE_TEST_ESTIMATE);

            /**
             *  如果提交两次,修复后续在总积分中计算两次的情况
             */
            Double score = zSetOperations.score(paperPracticeIdSore, standardCard.getId() + "");
            if (null == score) {
                score = 0D;
            }
            // 分数+时间戳 排名
            long updateTime = System.currentTimeMillis();
            double timeRank = standardCard.getScore() + 1 - updateTime / Math.pow(10, (int) Math.log10(updateTime) + 1);
            logger.info("userid:{} socre:{}", answerCard.getUserId(), timeRank);
            // 将练习id和其score写入zset
            zSetOperations.add(paperPracticeIdSore, standardCard.getId() + "", timeRank);

            // 累加分数,用于计算平均分
            valueOperations.increment(paperScoreSum, standardCard.getScore() - score);
        }

        CardUserMeta cardUserMeta = handlerMetaService.getCardUserMeta(standardCard);
        standardCard.setCardUserMeta(cardUserMeta);
    }

    /**
     * 统计阶段测试正确率区间人数
     *
     * @param answerCard
     */
    public void setPeriodTestAccuracyNum(AnswerCard answerCard, long syllabusId) {
        // 阶段测试交卷时正确率存redis
        if (answerCard.getStatus() != AnswerCardStatus.FINISH) {
            StandardCard standardCard = (StandardCard) answerCard;
            int rcount = standardCard.getRcount();
            int qcount = standardCard.getPaper().getQcount();
            double accuracy = 0d;
            if (qcount != 0) {
                accuracy = rcount / qcount * 100;
            }
            long rate = Math.round(accuracy);
            String type = PeriodTestConstant.LTFIFTY;
            ;
            if (rate < 50) {
                type = PeriodTestConstant.LTFIFTY;
            } else if (rate <= 80) {
                type = PeriodTestConstant.LTEIGHTY;
            } else if (rate <= 100) {
                type = PeriodTestConstant.GTEIGHTY;
            }

            HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
            if (standardCard.getPaper() != null) {
                String scoreKey = PeriodTestRedisKey.getPeriodTestAccuracyNum(standardCard.getPaper().getId(), syllabusId);
                opsForHash.increment(scoreKey, type, 1);
            }
        }
    }

    /**
     * 统计阶段测试的正确率
     *
     * @param answerCard
     */
    public void setPeriodTestQuestionAccuracy(AnswerCard answerCard, long syllabusId) {
        // 阶段测试交卷时正确率存redis
        if (answerCard.getStatus() != AnswerCardStatus.FINISH) {
            StandardCard standardCard = (StandardCard) answerCard;
            int rcount = standardCard.getRcount();
            int qcount = standardCard.getPaper().getQcount();
            double accuracy = 0d;
            if (qcount > 0) {
                accuracy = rcount / qcount * 100;
            }
            long rate = Math.round(accuracy);

            HashOperations<String, Object, Object> opsForHash = redisTemplate.opsForHash();
            if (standardCard.getPaper() != null) {
                String scoreKey = PeriodTestRedisKey.getPeriodTestQuestionAccuracy(standardCard.getPaper().getId(), syllabusId);
                opsForHash.increment(scoreKey, String.valueOf(standardCard.getPaper().getId()), rate);
            }
        }
    }

    public List<StandardCard> findAnswerCardByUidAndType(long userId, int subjectId, int type) {
        return answerCardDao.findAnswerCardByUidAndType(userId, subjectId, type);
    }

    public List<AnswerCard> findByIds(List<Long> practiceIds) {
        return answerCardDao.findByIds(practiceIds);
    }

    /**
     * 查询未完成答题卡
     */
    public AnswerCard findUndoCard(long userId, int paperId) {
        PaperUserMeta userMeta = paperUserMetaService.findById(userId, paperId);
        if (null == userMeta) {
            return null;
        }
        long currentPracticeId = userMeta.getCurrentPracticeId();
        AnswerCard answerCard = answerCardDao.findById(currentPracticeId);
        if (null != answerCard && AnswerCardStatus.FINISH != answerCard.getStatus()) {
            //未做完
            return answerCard;
        }
        return null;
    }
}
