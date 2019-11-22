package com.huatu.ztk.knowledge.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.bean.*;
import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
import com.huatu.ztk.knowledge.common.PointStatus;
import com.huatu.ztk.knowledge.common.QuestionPointLevel;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.constant.CustomizeEnum;
import com.huatu.ztk.knowledge.service.v2.CustomizeStrategyServiceV2;
import com.huatu.ztk.knowledge.util.GeneticAlgorithmUtil;
import com.huatu.ztk.knowledge.util.GeneticSupportUtil;
import com.huatu.ztk.question.common.DifficultGrade;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 抽题策略dubbo服务 实现
 * Created by shaojieyue
 * Created time 2016-05-18 18:25
 */
public class QuestionStrategyDubboServiceImpl implements QuestionStrategyDubboService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionStrategyDubboServiceImpl.class);
    public static final int MAX_POINT = 3;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private QuestionPointService questionPointService;

    @Autowired
    private PoxyUtilService poxyUtilService;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Resource(name = "redisObjectTemplate")
    private RedisTemplate<String, QuestionGeneticBean> redisTemplate2;

    @Autowired
    private ModuleDubboService moduleDubboService;

    @Autowired
    private CustomizeStrategyServiceV2 customizeStrategyService;

    /**
     * 最大的组卷试题数量
     */
    public static final int MAX_PRACTICE_QUESTION_SIZE = 500;


    /**
     * 随机获取组卷策略
     *
     * @param uid
     * @param subject
     * @param size    试题数量  @return
     */
    public QuestionStrategy randomStrategy(long uid, int subject, int size) {
        return randomStrategy(uid, subject, -1, size);
    }

    /**
     * 随机获取组卷策略
     *
     * @param uid
     * @param subject
     * @param size    试题数量  @return
     */
    public QuestionStrategy smartStrategy(long uid, int subject, int size) {
        long startTime = System.currentTimeMillis();
        logger.info("传输进来的参数：uid={},subject={},size={}", uid, subject, size);
        List<Integer> questionIds = new ArrayList<>();
        List<QuestionGeneticBean> questionLast = new ArrayList<>();
        //事业单位去掉科目转换
       /* if (subject == 3) {
            subject = 1;
        }*/
        List<QuestionPointTree> pointsNew = questionPointService.questionPointTree(uid, subject, CustomizeEnum.ModeEnum.Write);
        long stime1 = System.currentTimeMillis();
        logger.info("step1用时={}", stime1 - startTime);
        //按照准确率排序
        List<QuestionPointTree> points = pointsNew.stream().sorted(Comparator.comparing(QuestionPointTree::getAccuracy)).collect(Collectors.toList());
        long stime2 = System.currentTimeMillis();
        logger.info("step2用时={}", stime2 - stime1);
        int allQnumUser = 0;
        int rightQnum = 0;
        Random rand = new Random();

        double difficulty = 6.0;//初始设置为6，即为中等难度
        List<QuestionGroup> questionGroups = new ArrayList<>();
        List<QuestionGroup> questionGroupsAll = new ArrayList<>();
        List<Integer> modules = new ArrayList<>();
        List<Integer> modulesQnum = new ArrayList<>();//每个模块有多少题，总量小于size
        int moduleLen = 3;//选择module个数
        int alreadyQnum = 0;//每个模块的题个数随机，已经用掉多少个

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int year = c.get(Calendar.YEAR);//初始化年份要求为系统当年时间
        int maxYear = 0;//试题中最大年份

        final String finishedSmartKey = RedisKnowledgeKeys.getFinishedSmartKey(uid, subject);
        Set<String> questionSmartFinished = redisTemplate.opsForZSet().range(finishedSmartKey, 0, -1);//用户已经做过的试题
        long stime3 = System.currentTimeMillis();
        logger.info("step3用时={}", stime3 - stime2);
        int allSelectQnum = 0;//所有选择的题目数量
        int allQum = 0;//所有题目

        //计算总共做了多少题，用户多少做对多少道题,选择出准确率低的模块及该模块下的试题
        for (int i = 0, len = points.size(); i < len; i++) {

            QuestionPointTree questionPointTree = points.get(i);
            allQnumUser += questionPointTree.getRnum() + questionPointTree.getWnum();
            rightQnum += questionPointTree.getRnum();
            if (moduleLen > 0) {
                int module = questionPointTree.getId();//module设置第一级知识点
                int yearLen = 10;//取十年的题
                boolean moduleSign = false;//标记该模块有可选择的试题，false为没有，true为有
                int moduleAllQnum = 0;//该模块下共可以用的试题总数

                for (int j = 0; j < yearLen; j++) {
                    final String key = RedisKnowledgeKeys.getYearModuleQuestionsV3(year - j, module, subject);
                    long jStime1 = System.currentTimeMillis();
                    Set<QuestionGeneticBean> questionGeneticBeen = redisTemplate2.opsForSet().members(key);
                    allQum += questionGeneticBeen.size();
                    List<QuestionGeneticBean> questions = delDuplicate(questionGeneticBeen, questionSmartFinished);
                    long jStime2 = System.currentTimeMillis();

                    int qNum = questions.size();
                    logger.info("第几个={}，区数据用时={},qNum={}，questionGeneticBeen={},key={}", j, jStime2 - jStime1, qNum, questionGeneticBeen.size(), key);
                    if (qNum > 10) {
                        QuestionGroup questionGroup = QuestionGroup.builder()
                                .moduleId(module)
                                .qNum(qNum)
                                .year(year - j)
                                .questions(questions)
                                .build();
                        questionGroups.add(questionGroup);
                        if ((year - j) > maxYear) {
                            maxYear = year - j;
                        }
                        moduleSign = true;
                        moduleAllQnum += qNum;
                        allSelectQnum += qNum;
                        if (moduleAllQnum > 1000) {
                            break;
                        }
                    }
                    //所有题都记录下来，防止没有题
                    List<QuestionGeneticBean> questionsNotDel = new ArrayList<>();//未去除已做过的题
                    questionsNotDel.addAll(questionGeneticBeen);
                    QuestionGroup questionGroupNotDel = QuestionGroup.builder()
                            .moduleId(module)
                            .qNum(questionsNotDel.size())
                            .year(year - j)
                            .questions(questionsNotDel)
                            .build();
                    questionGroupsAll.add(questionGroupNotDel);
                }

                if (moduleSign == true) {
                    modules.add(module);
                    int moduleQnum = 0;
                    if (moduleLen == 1) {
                        moduleQnum = size - alreadyQnum;
                    } else {
                        moduleQnum = rand.nextInt(Math.min(size - alreadyQnum - moduleLen, moduleAllQnum)) + 1;
                    }
                    modulesQnum.add(moduleQnum);
                    moduleLen--;
                    alreadyQnum += moduleQnum;
                }
            } else {
                break;
            }
        }
        long stime4 = System.currentTimeMillis();
        logger.info("step4用时={}", stime4 - stime3);


        //题已经不够选择产生一套试卷
        if (allSelectQnum < size) {
            redisTemplate.opsForZSet().removeRange(finishedSmartKey, 0, 2000);
            return randomSmart(modulesQnum, questionGroupsAll, modules);
        }

        //若可供选择的题少于1000道，直接随机产生试题
        if (allSelectQnum < 1000 && allSelectQnum >= size) {
            return randomSmart(modulesQnum, questionGroups, modules);
        }


        //用户做题量不为0，且正确的题量不为0的情况下
        if (allQnumUser != 0 && rightQnum != 0) {
            difficulty = 10.0 * rightQnum / allQnumUser;
        }

        PaperRequire paperRequire = PaperRequire.builder()
                .qNum(size)
                .difficulty(difficulty)
                .year(maxYear)
                .moduleIds(modules)
                .eachTypeCount(modulesQnum)
                .build();

        GeneticAlgorithmUtil algorithm = new GeneticAlgorithmUtil(0.1, 0.3, 0.6);
        GeneticSupportUtil geneticSupportUtil = new GeneticSupportUtil();
        //迭代次数计数器
        int runCount = 100;

        //适应度期望值
        double expand = 0.85;
        long stime5 = System.currentTimeMillis();
        logger.info("step5用时={}", stime5 - stime4);
        List<PaperUnit> unitList = new ArrayList<>();
        unitList.addAll(algorithm.initialPopulation(10, paperRequire, questionGroups));
        logger.info("\n\n      -------遗传算法组卷系统(砖题库华丽丽的飘过)---------\n\n");
        logger.info("-----------------------迭代开始------------------------");
        List<PaperUnit> resultUnits = geneticSupportUtil.resultUnit(unitList, expand);
        long stime6 = System.currentTimeMillis();
        logger.info("step6用时={}", stime6 - stime5);
        if (resultUnits.size() > 0) {
            PaperUnit unit = resultUnits.get(0);
            questionLast.addAll(unit.getQuestions());
            logger.info("第几套={},题目数量={},知识点分布={}难度系数={},年份={},适应度={}",
                    unit.getId(), unit.getQNum(), unit.getModuleCoverage(), unit.getDifficulty(), unit.getYear(), unit.getAdaptationDegree());
        } else {
            for (int i = 0; i < runCount; i++) {
                long istime = System.currentTimeMillis();

                logger.info("在第 " + i + " 代未得到结果");
                //选择
                unitList = algorithm.selectPopulation(unitList, 5);
                long istime1 = System.currentTimeMillis();
                logger.info("选择种群用时={}，第几次={}", istime1 - istime, i);
                //交叉
                unitList = algorithm.crossPopulation(unitList, 10, paperRequire);
                logger.info("交叉后的种群={}");
                resultUnits = geneticSupportUtil.resultUnit(unitList, expand);
                if (resultUnits.size() > 0) {
                    PaperUnit unit = resultUnits.get(0);
                    questionLast.addAll(unit.getQuestions());
                    logger.info("第几套={},题目数量={},知识点分布={}难度系数={},年份={},适应度={}",
                            unit.getId(), unit.getQNum(), unit.getModuleCoverage(), unit.getDifficulty(), unit.getYear(), unit.getAdaptationDegree());
                    break;
                }
                long istime2 = System.currentTimeMillis();
                logger.info("交叉种群用时={}，第几次={}", istime2 - istime1, i);
                //变异
                unitList = algorithm.changePopulation(unitList, questionGroups, paperRequire);
                long istime3 = System.currentTimeMillis();
                logger.info("变异种群操作用时={}，第几次={}", istime3 - istime2, i);
                logger.info("变异后的种群={}");
                resultUnits = geneticSupportUtil.resultUnit(unitList, expand);
                if (resultUnits.size() > 0) {
                    PaperUnit unit = resultUnits.get(0);
                    questionLast.addAll(unit.getQuestions());
                    logger.info("第几套={},题目数量={},知识点分布={}难度系数={},年份={},适应度={}",
                            unit.getId(), unit.getQNum(), unit.getModuleCoverage(), unit.getDifficulty(), unit.getYear(), unit.getAdaptationDegree());
                    break;
                }
                long istime4 = System.currentTimeMillis();
                logger.info("变异种群用时={}，第几次={}", istime4 - istime3, i);
                long etime = System.currentTimeMillis();
                logger.info("第几次迭代={}，用时={}", i, etime - istime);
            }
        }
        long stime7 = System.currentTimeMillis();
        logger.info("step7用时={}", stime7 - stime6);

        //若没有迭代出符合条件的试卷，那么，最后一次迭代中，适应度最高的试卷为最终试卷
        if (resultUnits.size() == 0) {
            System.out.println("计算 " + runCount + " 代仍没有结果，请重新设计条件！");
            PaperUnit unit = new PaperUnit();
            for (PaperUnit u : unitList) {
                //System.out.println("u.getAdaptationDegree():"+u.getAdaptationDegree()+"  unit.getAdaptationDegree():"+unit.getAdaptationDegree());
                if (u.getAdaptationDegree() > unit.getAdaptationDegree()) {
                    unit = u;
                }
            }
            questionLast.addAll(unit.getQuestions());
            System.out.println();
            logger.info("第几套={},题目数量={},知识点分布={}难度系数={},年份={},适应度={}", unit.getId(), unit.getQNum(), unit.getModuleCoverage(), unit.getDifficulty(), unit.getYear(), unit.getAdaptationDegree());
        }

        QuestionStrategy questionStrategy = getSmartQuestionStrategy(questionLast);
        logger.info("试卷要求={},allQnumUser={},rightQnum={},组卷结果={},size={}", paperRequire, allQnumUser, rightQnum, questionStrategy.getQuestions().size(), size);
        return questionStrategy;
    }

    /**
     * 试题不够，采用随机获取
     *
     * @param modulesQnum
     * @param questionGroups
     * @param modules
     * @return
     */
    private QuestionStrategy randomSmart(List<Integer> modulesQnum, List<QuestionGroup> questionGroups, List<Integer> modules) {
        List<QuestionGeneticBean> questionLast = new ArrayList<>();
        for (int i = 0, mlen = modulesQnum.size(); i < mlen; i++) {
            List<QuestionGeneticBean> moduleQuestions = new ArrayList<>();
            GeneticSupportUtil geneticSupportUtil = new GeneticSupportUtil();
            List<QuestionGroup> moduleQuestionGroups = geneticSupportUtil.selectQuestionGroups(questionGroups, modules.get(i));
            int len = modulesQnum.get(i);
            for (int j = 0, qglen = moduleQuestionGroups.size(); j < qglen; j++) {
                List<QuestionGeneticBean> moduleAllQuestions = moduleQuestionGroups.get(j).getQuestions();
                if (moduleQuestions.size() < len && moduleAllQuestions.size() >= len - moduleQuestions.size()) {
                    moduleQuestions.addAll(moduleAllQuestions.subList(0, len - moduleQuestions.size()));
                    break;
                } else {
                    moduleQuestions.addAll(moduleAllQuestions);
                    continue;
                }
            }
            questionLast.addAll(moduleQuestions);
        }
        return getSmartQuestionStrategy(questionLast);
    }


    private QuestionStrategy getSmartQuestionStrategy(List<QuestionGeneticBean> questionGeneticBeen) {
        //模块试题对应关系,用array list 来保证试题的顺序，这里主要是解决复合题之间的连续问题
        final ArrayListMultimap<Integer, Integer> multimap = ArrayListMultimap.create();
        double difficultSum = 0;
        int size = questionGeneticBeen.size();

        for (QuestionGeneticBean question : questionGeneticBeen) {
            if (question == null) {//防止出现空指针
                continue;
            }
            multimap.put(question.getModuleId(), question.getId());
            difficultSum += question.getDifficulty();
        }

        List<Module> modules = new ArrayList<>();
        List<Integer> questions = new ArrayList<>();
        //遍历多值map,组装QuestionStrategy
        for (Integer moduleId : multimap.keySet()) {
            final QuestionPoint point = questionPointDubboService.findById(moduleId);
            final List<Integer> ids = multimap.get(moduleId);
            final Module module = Module.builder().category(point.getId())
                    .name(point.getName())
                    .qcount(ids.size()).build();
            modules.add(module);
            questions.addAll(ids);
        }

        //计算难度,保证一位小数点
        BigDecimal difficulty = new BigDecimal(6);

        if (size > 0) {
            difficultSum /= size;
        }
        final QuestionStrategy questionStrategy = QuestionStrategy.builder().modules(modules)
                .questions(questions)
                .difficulty(difficultSum).build();

        return questionStrategy;
    }


    /**
     * 去除用户已经做过的题
     *
     * @param questionGeneticBeen
     * @param questionSmartFinished
     * @return
     */
    public List<QuestionGeneticBean> delDuplicate(Set<QuestionGeneticBean> questionGeneticBeen, Set<String> questionSmartFinished) {
        List<QuestionGeneticBean> questions = new ArrayList<>();
        for (QuestionGeneticBean question : questionGeneticBeen
        ) {
            if (!questionSmartFinished.contains(String.valueOf(question.getId()))) {
                questions.add(question);
            }
        }
        return questions;
    }

    private List<Integer> randomSinglePoints(long uid, int subject, int pointId, int size) {
        return randomSinglePointsV1(uid, size, pointId, this::getQuestionsByPoint);
    }


    /**
     * 获取知识点对应的试题id列表
     * 这里的数据可能会比预期值多 原因在 question_id hash 缺少值
     *
     * @param pointId
     * @return
     * @update by lijun 2018-02-05 获取的所有考题进行redis 数据过滤 保证后续考题过滤时有数据
     */
    private List<String> getQuestionsByPoint(int pointId) {
        return poxyUtilService.getQuestionPointService().getQuestionIds(pointId);
    }

    @Override
    public QuestionStrategy randomStrategy(long uid, int subject, int pointId, int size) {
        if (size > MAX_PRACTICE_QUESTION_SIZE || size < 1) {//不能超过最大值
            size = MAX_PRACTICE_QUESTION_SIZE;
        }
        long start = System.currentTimeMillis();
        //随机选取知识点
        List<QuestionPoint> finalPointIds = new ArrayList<>();
        Map<Integer, Integer> leftQuestionCount = Maps.newHashMap();
        if (pointId < 1) {//小于0,说明随机取知识点

            List<Integer> moduleIds = moduleDubboService.findSubjectModules(subject).stream()
                    .map(m -> m.getId())
                    .collect(Collectors.toList());

            logger.info("uid={},pointId={},subject={},moduleIds={}", uid, pointId, subject, JsonUtil.toJson(moduleIds));
            for (Integer moduleId : moduleIds) {
//                final List<QuestionPoint> points = questionPointDubboService.randomPoint(moduleId, 3);
                final List<QuestionPoint> points = getTopLeftPoint(moduleId, 3, uid, leftQuestionCount);
                finalPointIds.addAll(points);
            }
        } else {
            logger.info("uid={},pointId={},subject={}", uid, pointId, subject);
//            finalPointIds.addAll(questionPointDubboService.randomPoint(pointId, 10));
            finalPointIds.addAll(getTopLeftPoint(pointId, 10, uid, leftQuestionCount));
        }
        //重新打乱排序
        Collections.shuffle(finalPointIds);
        logger.info("finalPointIds：{}, cost time1={}, uid = {}, subject = {}, pointId = {}, size = {}",
                finalPointIds, System.currentTimeMillis() - start, uid, subject, pointId, size);
        List<Integer> questionIds = null;
        start = System.currentTimeMillis();
        if (finalPointIds.size() > 1) {
//            questionIds = randomMultiplePoints(uid, subject, size, finalPointIds);
            questionIds = randomMultiplePointsV1(uid, size, finalPointIds, leftQuestionCount);
        } else {
            questionIds = randomSinglePoints(uid, subject, finalPointIds.get(0).getId(), size);
        }

        Collections.shuffle(questionIds);
        logger.info("get questionIds, cost time2={}, uid = {}, subject = {}, pointId = {}, questionIds = {}",
                System.currentTimeMillis() - start, uid, subject, pointId, questionIds);

        start = System.currentTimeMillis();
        QuestionStrategy questionStrategy = getRamdomQuestionStrategy(questionIds, size);
        logger.info("get questionStrategy,cost time3={}, uid = {}, subject = {}, pointId = {}, questionStrategy_questionId={}",
                System.currentTimeMillis() - start, uid, subject, pointId, questionStrategy.getQuestions());
        return questionStrategy;
    }

    private List<Integer> randomMultiplePointsV1(long uid, int size, List<QuestionPoint> finalPointIds, Map<Integer, Integer> leftQuestionCount) {
        StopWatch stopWatch = new StopWatch("randomMultiplePointsV1:" + uid + ":" + size + ":" + finalPointIds.stream().map(QuestionPoint::getId).map(String::valueOf).collect(Collectors.joining(",")));
        try {
            stopWatch.start("getQuestionByPoint");
            int sum = finalPointIds.stream().mapToInt(i -> leftQuestionCount.getOrDefault(i.getId(), 0)).sum();
            List<Integer> result = Lists.newArrayList();
            final Map<Integer, List<String>> questionMap = finalPointIds.stream().collect(Collectors.toMap(i -> i.getId(), i -> {
                List<String> questionIds = getQuestionsByPoint(i.getId());
                if (CollectionUtils.isEmpty(questionIds)) {
                    return Lists.newArrayList();
                }
                return questionIds;
            }));
            stopWatch.stop();
            Function<Integer, List<String>> getQuestionIds = (id -> questionMap.getOrDefault(id, Lists.newArrayList()));
            if (sum < size) {     //如果题量小于需要的题量
                stopWatch.start("randomSinglePointsV1");
                for (QuestionPoint finalPointId : finalPointIds) {
                    Integer leftCount = leftQuestionCount.getOrDefault(finalPointId.getId(), 0);
                    if (leftCount > 0) {
                        List<Integer> tempList = randomSinglePointsV1(uid, leftCount, finalPointId.getId(), getQuestionIds);
                        result.addAll(tempList);
                    }
                }
                stopWatch.stop();
                stopWatch.start("fillQuestionId");
                List<String> tempList = Lists.newArrayList();
                for (List<String> value : questionMap.values()) {
                    tempList.addAll(value);
                }
                tempList.removeIf(i -> result.contains(Integer.parseInt(i)));
                int i = size - result.size();
                Collections.shuffle(tempList);
                List<Integer> collect = tempList.subList(0, Math.min(i, tempList.size())).stream().map(Integer::parseInt).collect(Collectors.toList());
                result.addAll(collect);
                stopWatch.stop();
            } else {      //剩余题量大于所需题量，按照剩余题量比例分配
                stopWatch.start("randomSinglePointsV1");
                for (QuestionPoint finalPointId : finalPointIds) {
                    Integer leftCount = leftQuestionCount.getOrDefault(finalPointId.getId(), 0);
                    if (leftCount > 0) {
                        List<Integer> tempList = randomSinglePointsV1(uid, Math.min((leftCount * size / sum + 1), leftCount), finalPointId.getId(), getQuestionIds);       //至少取一道题目
                        result.addAll(tempList);
                    }
                }
                stopWatch.stop();
            }
            return result.subList(0, Math.min(result.size(), size));
        } finally {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            logger.info(stopWatch.prettyPrint());
        }
    }

    private List<Integer> randomSinglePointsV1(long uid, int size, int pointId, Function<Integer, List<String>> getQuestionIds) {
        logger.info(">>randomSinglePointsV1:{},{},{}", uid, size, pointId);
        Set<String> questionIds = Sets.newHashSet();
        /**
         * note by lijun 不能在此处进行是否存在于redis过滤，全量遍历question 资源消耗过大。
         */
        final List<String> questions = getQuestionIds.apply(pointId);
        logger.info("qustions={}", questions);
        if (CollectionUtils.isEmpty(questions)) {//该知识点没有试题
            return new ArrayList<>();
        }

        if (questions.size() <= size) {//题量小,则直接返回
            return questions.stream().map(q -> Ints.tryParse(q)).collect(Collectors.toList());
        }

        int questionsSize = questions.size();
        /**
         * note by lijun 此处存在bug:剩余的题目量大于size,依旧不会全部出现
         */
        for (int i = 0; i < 3; i++) {
            Set<String> tmp = Sets.newHashSet();
            for (int j = 0; j < size; j++) {//尝试3遍,保证能获取到足量的试题

                //update  by lijun 2018-02-06
                //原代码中  没有fromRedis 判断
                String questionId = questions.get(RandomUtils.nextInt(0, questionsSize));
                boolean fromRedis = filterQuestionIdFromRedis(questionId);
                if (fromRedis) {
                    //随机生成本知识点需要返回的试题个数
                    //随机取出指定个数的试题id,该命令会产生重复的id,所以用set
                    tmp.add(questionId);
                }
                //update finish

            }
            questionIds.addAll(filterFinished(tmp, uid, pointId));
            //已经取够了足量的试题数
            if (questionIds.size() >= size) {
                break;
            }
        }
        logger.info("questionIds={}", questionIds);
        //logger.info("随机组卷未完成试题信息1：{}", questionIds.stream().collect(Collectors.joining(",")));
        /**
         * updateby lijun 随机函数的补偿策略
         */
        if (questionIds.size() < size && size < questionsSize) {
            //1.获取所有的未完成的试题
            Collection<String> noFinishedQuestionId = filterFinished(new HashSet<>(questions), uid, pointId);
            List<String> list = ListUtils.removeAll(noFinishedQuestionId, questionIds);
            logger.info("noFinishedQuestionId:{}", list);
            List<String> idList = list.stream()
                    .filter(id -> filterQuestionIdFromRedis(id))
                    .collect(Collectors.toList());
            Collections.shuffle(idList);
            logger.info("idList={}", idList);
            int questionIdSize = questionIds.size();
            List<String> tempList = idList.subList(0, Math.min(size - questionIdSize, idList.size()));
            questionIds.addAll(tempList);
        }
        logger.info("随机组卷未完成试题信息-补偿后：{}", questionIds);

        if (questionIds.size() < size) {
            for (int i = 0; i < 3 * size; i++) {
                //update  by lijun 2018-02-06
                //原代码中  没有fromRedis 判断
                String questionId = questions.get(RandomUtils.nextInt(0, questionsSize));
                boolean fromRedis = filterQuestionIdFromRedis(questionId);
                if (fromRedis) {
                    //随机生成本知识点需要返回的试题个数
                    //随机取出指定个数的试题id,该命令会产生重复的id,所以用set
                    questionIds.add(questionId);
                }
                //update finish

                //已经取够了足量的试题数
                if (questionIds.size() >= size) {
                    break;
                }
            }
        }
        Collection<String> strings = filterFinished(questionIds, uid, pointId);
        if (strings.size() < questionIds.size()) {
            logger.info("存在已完成的试题，pointId={}", pointId);
        }
        //logger.info("随机组卷试题最终信息：{}", questionIds.stream().collect(Collectors.joining(",")));
        return questionIds.stream().map(q -> Ints.tryParse(q)).collect(Collectors.toList());
    }

    /**
     * 获取剩余题量最多的几个知识点
     *
     * @param pointId
     * @param count
     * @param uid
     * @param leftQuestionCount
     * @return
     */
    private List<QuestionPoint> getTopLeftPoint(Integer pointId, int count, long uid, Map<Integer, Integer> leftQuestionCount) {
        StopWatch stopWatch = new StopWatch();
        try {
            BiFunction<Long, QuestionPoint, Integer> getUnFinishCount = ((userId, questionPoint) -> {
                int i = poxyUtilService.getQuestionFinishService().count(userId, questionPoint) - poxyUtilService.getQuestionPointService().count(questionPoint.getId());
                return Math.max(0, i);
            });
            List<QuestionPoint> results = new ArrayList<>();
            stopWatch.start("questionPointDubboService.findById");
            final QuestionPoint questionPoint = questionPointDubboService.findById(pointId);
            stopWatch.stop();
            if (questionPoint == null) {
                return results;
            }
            if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_THREE) {
                results.add(questionPoint);
                leftQuestionCount.put(questionPoint.getId(), getUnFinishCount.apply(uid, questionPoint));
                return results;
            }

            if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_TWO) {//二级列表
                //批量查询,需要去掉已经删除掉的知识点
                stopWatch.start("questionPointDubboService.findBath+LEVEL_TWO");
                results = questionPointDubboService.findBath(questionPoint.getChildren()).stream().filter(point1 -> point1.getStatus() != PointStatus.DELETED).collect(Collectors.toList());
                stopWatch.stop();
            }

            if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_ONE) {//一级节点
                //批量查询 此处是二级节点
                stopWatch.start("questionPointDubboService.findBath+LEVEL_ONE");
                results = questionPointDubboService.findBath(questionPoint.getChildren()).stream().filter(point1 -> point1.getStatus() != PointStatus.DELETED).collect(Collectors.toList());
                List<QuestionPoint> tmp = new ArrayList<>();
                for (QuestionPoint result : results) {
                    //此处是3级节点,需要去掉已经删除掉的知识点
                    final List<QuestionPoint> questionPoints = questionPointDubboService.findBath(result.getChildren()).stream().filter(point1 -> point1.getStatus() != PointStatus.DELETED).collect(Collectors.toList());
                    tmp.addAll(questionPoints);
                }
                results = tmp;
                stopWatch.stop();
            }
            stopWatch.start("poxyUtilService.getQuestionPointService().countAll()");
            Map<Integer, Integer> questionPointMap = poxyUtilService.getQuestionPointService().countAll();
            stopWatch.stop();
            stopWatch.start("poxyUtilService.getQuestionFinishService().countAll(uid)");
            Map<Integer, Integer> finishMap = poxyUtilService.getQuestionFinishService().countAll(uid);
            stopWatch.stop();
            Map<Integer, Integer> collect = results.stream().collect(Collectors.toMap(i -> i.getId(),
                    i -> Math.max(questionPointMap.getOrDefault(i.getId(), 0) - finishMap.getOrDefault(i.getId(), 0), 0)));
            leftQuestionCount.putAll(collect);
            if (results.size() < count) {
                return results;
            }
            //按照剩余题量倒序排序
            results.sort(Comparator.comparing(i -> -collect.getOrDefault(i.getId(), 0)));
            return results.subList(0, count);
        } finally {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            logger.info(stopWatch.prettyPrint());
        }


    }

    /**
     * 根据试题id列表和抽题数量组装组卷策略 bean
     * 只能用于已经挂在知识点树(redis)上的试题
     * update by lijun 2018-05-14 优先保留未完成的试题，如果当前是试题是某个节点的最后几道试题，则可能出现 试题把未完成的过滤掉导致最后几道题一直无法出现。
     *
     * @param questionIds 试题id列表
     * @param size        抽题数量
     * @return
     */
    private QuestionStrategy getRamdomQuestionStrategy(List<Integer> questionIds, int size) {
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        //模块试题对应关系,用array list 来保证试题的顺序，这里主要是解决复合题之间的连续问题
        final ArrayListMultimap<Integer, Integer> multimap = ArrayListMultimap.create();
        int difficultSum = 0;
        //包含的复合题id列表
        Set<Integer> parents = new HashSet<>();

        for (Integer questionId : questionIds) {
            if (questionId == null) {//防止出现空指针
                continue;
            }
            final String questionIdKey = RedisKnowledgeKeys.getQuestionIdKey(questionId);
            final String parentStr = hashOperations.get(questionIdKey, "parent");
            if (parentStr == null) {//redis不存在该试题对象
                logger.error("questionIdKey={} not exist redis.", questionIdKey);
                continue;
            }

            Integer parent = NumberUtils.toInt(parentStr);

            if (parent != null && parent > 0) {//说明是复合题
                if (parents.contains(parent)) {//包含说明已经处理过,则不进行处理
                    continue;
                }

                parents.add(parent);//添加到已处理列表
                final String subIds = hashOperations.get(RedisKnowledgeKeys.getQuestionIdKey(parent), "subIds");
                if (StringUtils.isBlank(subIds)) {
                    logger.error("parent={} has not sub ids.", parent);
                    continue;
                }
                //复合题的子题是用,分割的
                final String[] ids = StringUtils.split(subIds, ",");
                for (String id : ids) {
                    int suQid = Ints.tryParse(id);
                    final String idKey = RedisKnowledgeKeys.getQuestionIdKey(suQid);
                    //取复合题moduleId
                    int moduleId = Ints.tryParse(hashOperations.get(RedisKnowledgeKeys.getQuestionIdKey(parent), "moduleId"));
                    //子题的难度系数
                    int difficult = DifficultGrade.GENERAL;
                    try {
                        difficult = Ints.tryParse(hashOperations.get(idKey, "difficult"));
                    } catch (Exception exceptione) {
                        logger.error(">>>fail fail , id={},idkey={}", id, idKey);
                    }
                    //加入模块
                    multimap.put(moduleId, suQid);
                    //难度系数添加
                    difficultSum = difficultSum + difficult;
                    if (multimap.size() == size) {//达到数量则会跳出
                        break;
                    }
                }
            } else {//普通试题
                final List<String> multiGet = hashOperations.multiGet(questionIdKey + "", Lists.newArrayList("difficult", "moduleId"));
                //加入模块
                multimap.put(Ints.tryParse(multiGet.get(1)), questionId);

                //难度系数添加
                difficultSum = difficultSum + Ints.tryParse(multiGet.get(0));
            }

            if (multimap.size() == size) {//达到数量则会跳出
                break;
            }
        }

        List<Module> modules = new ArrayList<>();
        List<Integer> questions = new ArrayList<>();
        //遍历多值map,组装QuestionStrategy
        for (Integer moduleId : multimap.keySet()) {
            final QuestionPoint point = questionPointDubboService.findById(moduleId);
            final List<Integer> ids = multimap.get(moduleId);
            final Module module = Module.builder().category(point.getId())
                    .name(point.getName())
                    .qcount(ids.size()).build();
            modules.add(module);
            questions.addAll(ids);
        }

        //计算难度,保证一位小数点
        BigDecimal difficulty = new BigDecimal(6);

        if (questions.size() > 0) {
            difficulty = new BigDecimal(difficultSum).divide(new BigDecimal(questions.size()), 1, BigDecimal.ROUND_HALF_UP);
        }
        final QuestionStrategy questionStrategy = QuestionStrategy.builder().modules(modules)
                .questions(questions)
                .difficulty(difficulty.doubleValue()).build();

        return questionStrategy;
    }


    /**
     * 完全随机组卷
     * 该组卷不根据用户做题信息进行组卷
     *
     * @param subject 科目
     * @param pointId 知识点id
     * @param size    试题个数
     * @return
     */
    @Override
    public QuestionStrategy randomStrategyNoUser(int subject, int pointId, int size) {
        //使用-3,表示一个不存在的用户,这种方式可以达到一个没有用户信息的效果,实际不应该这么做
        return randomStrategy(-3, subject, pointId, size);
    }

    private List<Integer> randomMultiplePoints(long uid, int subject, int size, List<QuestionPoint> finalPointIds) {

        //finalPointIds里面知识点的试题总数
        int questionCount = 0;
        Map<Integer, List<String>> questionMap = Maps.newHashMap();
        for (QuestionPoint finalPointId : finalPointIds) {
            int id = finalPointId.getId();
            List<String> questionsByPoint = getQuestionsByPoint(id);
            if (CollectionUtils.isNotEmpty(questionsByPoint)) {
                questionMap.put(id, questionsByPoint);
                questionCount = questionCount + questionsByPoint.size();
            }
        }

        //随机获取到的id 列表
        Set<String> questionIds = new HashSet<>();
        //遍历知识点
        for (QuestionPoint finalPointId : finalPointIds) {
            final List<String> questions = questionMap.getOrDefault(finalPointId.getId(), Lists.newArrayList());
            if (questions.size() == 0) {
                continue;
            }
            //按照比例来计算试题个数
            final int currentCount = (size * questions.size() / questionCount) + 1;
            int questionsSize = questions.size();
            //随机取出指定个数的试题id,该命令会产生重复的id,所以用set
            Set<String> tmp = Sets.newHashSet();
            for (int i = 0; i < currentCount; i++) {

                //update  by lijun 2018-02-06
                //原代码中  没有fromRedis 判断
                String questionId = questions.get(RandomUtils.nextInt(0, questionsSize));
                boolean fromRedis = filterQuestionIdFromRedis(questionId);
                if (fromRedis) {
                    //随机生成本知识点需要返回的试题个数
                    //随机取出指定个数的试题id,该命令会产生重复的id,所以用set
                    tmp.add(questionId);
                }
                //update finish
            }

            questionIds.addAll(filterFinished(tmp, uid, finalPointId.getId()));
            //已经取够了足量的试题数
            if (questionIds.size() >= size) {
                break;
            }
        }

        /**
         * updateby lijun 2018-05-14 最后题目不足需求的题目时，不进行数据补足
         * questionIds.size() < size
         */
        if (questionIds.size() < size) {
            //还没有凑够足够的试题,则扩大选择范围
            for (QuestionPoint finalPointId : finalPointIds) {
                final List<String> questions = questionMap.getOrDefault(finalPointId.getId(), Lists.newArrayList());

                //知识点没有试题
                if (CollectionUtils.isEmpty(questions)) {
                    continue;
                }

                int questionsSize = questions.size();
                for (int i = 0; i < size; i++) {
                    //update  by lijun 2018-02-06
                    //原代码中  没有fromRedis 判断
                    String questionId = questions.get(RandomUtils.nextInt(0, questionsSize));
                    boolean fromRedis = filterQuestionIdFromRedis(questionId);
                    if (fromRedis) {
                        //随机生成本知识点需要返回的试题个数
                        //随机取出指定个数的试题id,该命令会产生重复的id,所以用set
                        questionIds.add(questionId);
                    }
                    //update finish

                    //已经取够了足量的试题数
                    if (questionIds.size() >= size) {
                        break;
                    }
                }
                if (questionIds.size() >= size) {
                    break;
                }
            }
        }
        return questionIds.stream().map(q -> Ints.tryParse(q)).collect(Collectors.toList());
    }

    /**
     * 过滤掉已经做过的试题
     *
     * @param qids    试题列表
     * @param uid
     * @param pointId
     * @return 不包含已经做过的列表
     */
    private Collection<String> filterFinished(Set<String> qids, long uid, int pointId) {
        if (CollectionUtils.isEmpty(qids)) {
            return qids;
        }

        if (uid < 1) {//不存在的用户,不进行过滤
            return qids;
        }

        try {
            //获取已经做过的列表
            final Set<String> finishedSet = poxyUtilService.getQuestionFinishService().filterQuestionIds(uid, pointId, qids);
            //未做的试题列表
            return ListUtils.removeAll(qids, finishedSet);
        } catch (Exception e) {
            logger.error("ex", e);
        }
        //报错的情况下，则直接返回原列表
        return qids;
    }

    /**
     * 随机组卷
     *
     * @param uid
     * @param pointId 知识点id
     * @param size    返回试题大小
     * @return
     */
    @Override
    public QuestionStrategy randomErrorStrategy(long uid, int pointId, int subject, int size) {

        List<Integer> questions = Lists.newArrayList();
        if (pointId > 0) {
            Set<Integer> ids = poxyUtilService.getQuestionErrorService().getQuestionIds(pointId, uid, 0, size - 1);
            questions.addAll(ids);
        } else { //不指定知识点
            List<Integer> moduleIds = moduleDubboService.findSubjectModules(subject).stream()
                    .map(m -> m.getId())
                    .collect(Collectors.toList());

            for (Integer moduleId : moduleIds) {
                questions.addAll(poxyUtilService.getQuestionErrorService().getQuestionIds(moduleId, uid));
                Collections.shuffle(questions);
            }
        }

        if (questions.size() > size) {//超长则截取
            questions = Lists.newArrayList(questions.subList(0, size));
        }
        System.out.println("questions after2 = " + questions);

        Module module = Module.builder()
                .category(-1)
                .name("错题重练")
                .qcount(questions.size())
                .build();

        return QuestionStrategy.builder()
                .questions(questions)
                .modules(Lists.newArrayList(module))
                .difficulty(DifficultGrade.GENERAL)
                .build();

    }


    /**
     * @param uid
     * @param pointId
     * @param subject
     * @param size
     * @param flag    1是做题模式2是背题模式
     * @return
     */
    @Override
    public QuestionStrategy randomErrorStrategyWithFlag(long uid, int pointId, int subject, int size, int flag) {
        if (1 == flag) {
            return randomErrorStrategy(uid, pointId, subject, size);
        }
        return getRememberErrorStrategy(uid, pointId, subject, size);
    }

    /**
     * 背题模式生成策略
     *
     * @param uid
     * @param pointId
     * @param subject
     * @param size
     * @return
     */
    private QuestionStrategy getRememberErrorStrategy(long uid, int pointId, int subject, int size) {
        List<Integer> questions = Lists.newArrayList();
        if (pointId > 0) {
            List<Integer> ids = getRememberErrorIds(uid, pointId, size);
            questions.addAll(ids);
        } else { //不指定知识点
            List<Integer> moduleIds = moduleDubboService.findSubjectModules(subject).stream()
                    .map(m -> m.getId())
                    .collect(Collectors.toList());
            for (Integer moduleId : moduleIds) {
                questions.addAll(poxyUtilService.getQuestionErrorService().getQuestionIds(moduleId, uid));
                Collections.shuffle(questions);
            }
        }

        if (questions.size() > size) {//超长则截取
            questions = Lists.newArrayList(questions.subList(0, size));
        }
        Module module = Module.builder()
                .category(-1)
                .name("错题重练")
                .qcount(questions.size())
                .build();

        return QuestionStrategy.builder()
                .questions(questions)
                .modules(Lists.newArrayList(module))
                .difficulty(DifficultGrade.GENERAL)
                .build();
    }

    /**
     * 从redis中获取背题需要的试题
     *
     * @param uid
     * @param pointId
     * @param size
     * @return
     */
    private List<Integer> getRememberErrorIds(long uid, int pointId, int size) {
        List<Integer> result = Lists.newArrayList();
        int total = poxyUtilService.getQuestionErrorService().count(uid, QuestionPoint.builder().id(pointId).build());
        //错题总量检查
        if (total <= size) {
            result.addAll(poxyUtilService.getQuestionErrorService().getQuestionIds(pointId, uid, 0, size - 1));
            return result;
        }

        int reSizes = poxyUtilService.getQuestionErrorService().countLookMode(uid, pointId);
        //错题轮询集合检查
        if (reSizes == 0) {
            logger.info("wrong total={}", total);
            poxyUtilService.getQuestionErrorService().copyWrongSetToCursor(uid, pointId, total);
        }
        //错题抽取逻辑
        Function<Integer, List<Integer>> getWrongIds = (tempSize) -> {
            return poxyUtilService.getQuestionErrorService().getQuestionIdsLookMode(uid, pointId, tempSize);
        };
        List<Integer> ids = getWrongIds.apply(size);
        result.addAll(ids);
        //轮询数量补充
        if (ids.size() < size) {
            size = size - ids.size();
            poxyUtilService.getQuestionErrorService().copyWrongSetToCursor(uid, pointId, total);
            result.addAll(getWrongIds.apply(size));
        }
        return result;
    }


    /**
     * 收藏题目随机抽题
     *
     * @param uid
     * @param pointId
     * @param subject
     * @param size
     * @return
     */
    @Override
    public QuestionStrategy randomCollectStrategy(long uid, int pointId, int subject, int size) {
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final Set ids = new HashSet();
        final String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey(uid, pointId);
        final Set result = zSetOperations.range(collectSetKey, 0, size);
        ids.addAll(result);
        List<Integer> questions = toQuestionIds(ids);
        if (questions.size() > size) {//超长则截取
            questions = Lists.newArrayList(questions.subList(0, size));
        }

        //TODO 模块和难度也需要处理
        final Module module = Module.builder().category(392)
                .name("常识判断")
                .qcount(questions.size())
                .build();
        final ArrayList<Module> modules = Lists.newArrayList(module);
        final QuestionStrategy questionStrategy = QuestionStrategy.builder()
                .difficulty(3.0)
                .modules(modules)
                .questions(questions)
                .build();

        return questionStrategy;
    }

    @Override
    public QuestionStrategy randomCustomizeStrategy(long userId, int subject, Integer pointId, int size) {
        return customizeStrategyService.randCustomizeStrategy(userId, subject, pointId, size);
    }

    private List<Integer> toQuestionIds(Set ids) {
        List<Integer> questions = new ArrayList<>(ids.size());
        for (Object id : ids) {
            final Integer questionId = Ints.tryParse(String.valueOf(id));
            if (questionId == null) {//非法的key,不处理
                logger.error("illegal question id,id={}", id);
                continue;
            }
            questions.add(questionId);
        }
        return questions;
    }


    /**
     * 随机选取知识点
     *
     * @param ponintIds
     * @param random
     * @return
     */
    private Set<Integer> randomSelectPoints(List<Integer> ponintIds, Random random) {
        //被选中的知识点
        Set<Integer> finalPointIds = new HashSet<>();
        final int ponintSize = ponintIds.size();
        if (ponintSize <= MAX_POINT) {//小于指定个数时,则不用随机选取
            finalPointIds.addAll(ponintIds);
        } else {
            while (finalPointIds.size() < MAX_POINT) {//循环获取
                //添加随机索引下的id
                finalPointIds.add(ponintIds.get(random.nextInt(ponintSize)));
            }
        }
        return finalPointIds;
    }

    /**
     * 生成试卷时 添加 redis 节点过滤
     * 此处不在节点查询出增加 是为了避免 多次查询造成响应超时.
     *
     * @param questionId
     * @return 是否存在redis 中 如果存在 返回 true
     * @update by lijun 2018-02-06
     */
    public boolean filterQuestionIdFromRedis(final String questionId) {
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        final String questionIdKey = RedisKnowledgeKeys.getQuestionIdKey(Integer.valueOf(questionId));
        final String parentStr = hashOperations.get(questionIdKey, "parent");

        return parentStr != null;
    }
}
