package com.huatu.tiku.course.service.manager;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.bean.practice.QuestionInfo;
import com.huatu.tiku.course.bean.practice.QuestionInfoWithStatistics;
import com.huatu.tiku.course.common.YesOrNoStatus;
import com.huatu.tiku.course.consts.SimpleCourseLiveBackLog;
import com.huatu.tiku.course.dao.manual.CourseExercisesQuestionsStatisticsMapper;
import com.huatu.tiku.course.dao.manual.CourseExercisesChoicesStatisticsMapper;
import com.huatu.tiku.course.dao.manual.CourseExercisesStatisticsMapper;
import com.huatu.tiku.course.service.v1.practice.CourseLiveBackLogService;
import com.huatu.tiku.course.service.v1.practice.QuestionInfoService;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.ztk.api.v4.user.UserServiceV4;
import com.huatu.tiku.entity.CourseExercisesChoicesStatistics;
import com.huatu.tiku.entity.CourseExercisesQuestionsStatistics;
import com.huatu.tiku.entity.CourseExercisesStatistics;
import com.huatu.tiku.essay.essayEnum.CourseWareTypeEnum;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticeForCoursePaper;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-10 4:22 PM
 **/

@Slf4j
@Component
public class CourseExercisesStatisticsManager {

    @Autowired
    private CourseExercisesStatisticsMapper courseExercisesStatisticsMapper;

    @Autowired
    private CourseExercisesQuestionsStatisticsMapper questionsStatisticsMapper;

    @Autowired
    private CourseExercisesChoicesStatisticsMapper choicesStatisticsMapper;

    @Autowired
    private CourseLiveBackLogService courseLiveBackLogService;

    @Autowired
    private QuestionInfoService questionInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final int START = 0;
    private static final int END = 9;
    private static final int COUNT = 10;

    @Autowired
    private UserServiceV4 userService;

    /**
     * 用户提交课后作业处理统计信息
     * @param answerCard
     * @throws BizException
     */
    public void dealCourseExercisesStatistics(final PracticeCard answerCard) throws BizException{
        try{
            PracticeForCoursePaper practiceForCoursePaper = (PracticeForCoursePaper)answerCard.getPaper();
            String existsKey = CourseCacheKey.getCourseWorkDealData(practiceForCoursePaper.getCourseType(), practiceForCoursePaper.getCourseId());
            String rankInfoKey = CourseCacheKey.getCourseWorkRankInfo(practiceForCoursePaper.getCourseType(), practiceForCoursePaper.getCourseId());
            HashOperations<String, String, String> existsHash = redisTemplate.opsForHash();
            ZSetOperations<String, String> rankInfoZset = redisTemplate.opsForZSet();

            /**
             * 如果用户已经提交过不处理
             */
            if(existsHash.hasKey(existsKey, String.valueOf(answerCard.getUserId()))){
                log.info("not first deal course work info :type:{},courseId:{},cardId:{}",practiceForCoursePaper.getCourseType(), practiceForCoursePaper.getCourseId(), answerCard.getIdStr());
                return;
            }

            // 优先处理用户排名信息
            int times = Arrays.stream(answerCard.getTimes()).sum();
            long score = (practiceForCoursePaper.getQcount() - answerCard.getRcount()) * 100000 + times;

            UserRankInfo userRankInfo = UserRankInfo.builder()
                    .uid(answerCard.getUserId())
                    .rcount(answerCard.getRcount())
                    .expendTime(times)
                    .submitTimeInfo(System.currentTimeMillis())
                    .build();

            rankInfoZset.add(rankInfoKey, String.valueOf(answerCard.getUserId()), score);
            existsHash.put(existsKey, String.valueOf(answerCard.getUserId()), JSONObject.toJSONString(userRankInfo));

            //更新课后作业统计数据
            Example example = new Example(CourseExercisesStatistics.class);
            example.and()
                    .andEqualTo("courseId", practiceForCoursePaper.getCourseId())
                    .andEqualTo("courseType", practiceForCoursePaper.getCourseType())
                    .andEqualTo("status", YesOrNoStatus.YES.getCode())
                    //默认为0 课后作业
                    .andEqualTo("type", YesOrNoStatus.NO.getCode());
            example.setForUpdate(true);
            CourseExercisesStatistics courseExercisesStatistics = courseExercisesStatisticsMapper.selectOneByExample(example);

            if(null == courseExercisesStatistics){
                courseExercisesStatistics = new CourseExercisesStatistics();
                courseExercisesStatistics.setStatus(YesOrNoStatus.YES.getCode());
                courseExercisesStatistics.setCorrects(answerCard.getRcount());
                courseExercisesStatistics.setCosts(answerCard.getExpendTime());
                courseExercisesStatistics.setCounts(1);
                courseExercisesStatistics.setQuestionCount( practiceForCoursePaper.getQcount());
                courseExercisesStatistics.setCourseType(practiceForCoursePaper.getCourseType());
                courseExercisesStatistics.setCourseId(practiceForCoursePaper.getCourseId());
                courseExercisesStatistics.setGmtModify(new Timestamp(System.currentTimeMillis()));
                courseExercisesStatistics.setGmtCreate(new Timestamp(System.currentTimeMillis()));
                courseExercisesStatistics.setType(YesOrNoStatus.NO.getCode());
                courseExercisesStatisticsMapper.insertSelective(courseExercisesStatistics);
            }else{
                CourseExercisesStatistics update = new CourseExercisesStatistics();
                update.setId(courseExercisesStatistics.getId());
                update.setCounts(courseExercisesStatistics.getCounts() + 1);
                update.setCosts(courseExercisesStatistics.getCosts() + answerCard.getExpendTime());
                update.setCorrects(courseExercisesStatistics.getCorrects() + answerCard.getRcount());
                update.setGmtModify(new Timestamp(System.currentTimeMillis()));
                courseExercisesStatisticsMapper.updateByPrimaryKeySelective(update);
            }

            // 异步处理详细统计信息
            final CourseExercisesStatistics detailStatistics = courseExercisesStatistics;
            new Thread(() -> dealCourseExercisesDetailStatistics(detailStatistics.getId(), answerCard)).start();
        }catch (Exception e){
            log.error("处理课后作业统计信息异常!:{}", e);
        }
    }



    /**
     * 统计选项的统计信息
     * @param id
     * @param answerCard
     * @throws BizException
     */
    private void dealCourseExercisesDetailStatistics(long id, PracticeCard answerCard)throws BizException{
        PracticeForCoursePaper practiceForCoursePaper = (PracticeForCoursePaper)answerCard.getPaper();
        List<Integer> questionIds = practiceForCoursePaper.getQuestions();

        List<QuestionInfo> baseQuestionInfoList = questionInfoService.getBaseQuestionInfo(questionIds.stream().map(Long::new).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(baseQuestionInfoList)) {
            return;
        }
        for (QuestionInfo item : baseQuestionInfoList){
            Long questionId = item.getId();
            if (!checkQuestionsStatisticsExist(id, questionId)) {
                createQuestionsStatistics(id, questionId, item.getChoiceList().size());
            }
            List<Integer> userQuestions = practiceForCoursePaper.getQuestions();
            List<String> userAnswers = Arrays.stream(answerCard.getAnswers()).collect(Collectors.toList());
            List<Integer> userCorrects = Arrays.stream(answerCard.getCorrects()).boxed().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userQuestions) || CollectionUtils.isEmpty(userAnswers) || CollectionUtils.isEmpty(userCorrects)) {
                log.error("试题统计数据不合法:userQuestions:{},userAnswers:{},userCorrects:{}", userQuestions.size(), userAnswers.size(), userCorrects.size());
                continue;
            }
            try{
                int index = userQuestions.indexOf(questionId.intValue());
                String userAnswer = userAnswers.get(index);

                Long statisticsQuestionId = updateQuestionStatisticsCount(id, questionId, userAnswers.get(index), userCorrects.get(index));
                updateChoiceStatisticsCount(statisticsQuestionId, userAnswer);
            }catch (Exception e){
                log.error("课后作业试题统计数据异常:{}", e);
                continue;
            }
        }
    }


    /**
     * 检查课后练习单题数据是否存在
     * 存在返回 true
     * 不存在返回 false
     * @param statisticsId
     * @param questionId
     * @return
     */
    private boolean checkQuestionsStatisticsExist(long statisticsId, long questionId){
        Example example = new Example(CourseExercisesQuestionsStatistics.class);
        example.and()
                .andEqualTo("statisticsId", statisticsId)
                .andEqualTo("questionId", questionId)
                .andEqualTo("status", YesOrNoStatus.YES.getCode());
        CourseExercisesQuestionsStatistics questionsStatistics = questionsStatisticsMapper.selectOneByExample(example);
        return questionsStatistics != null;
    }

    /**
     * 更新每道题的答题状态
     * @param statisticsTableId 统计主表id
     * @param questionId 试题id
     * @param correct 是否正确
*    * @param answer 用户答案
     */
    private synchronized Long updateQuestionStatisticsCount(long statisticsTableId, long questionId, String answer, Integer correct){
        if(Integer.valueOf(answer) == 0 || correct == 0){
            return null;
        }
        /**
         * 统计试题做题次数
         */
        Example example = new Example(CourseExercisesQuestionsStatistics.class);
        example.and()
                .andEqualTo("statisticsId", statisticsTableId)
                .andEqualTo("questionId", questionId)
                .andEqualTo("status", YesOrNoStatus.YES.getCode());
        CourseExercisesQuestionsStatistics origin = questionsStatisticsMapper.selectOneByExample(example);
        /**
         * 统计每道题做对次数
         */
        CourseExercisesQuestionsStatistics newData = new CourseExercisesQuestionsStatistics();
        newData.setCounts(origin.getCounts() + 1);
        newData.setId(origin.getId());
        if(correct == 1){
            newData.setCorrects(origin.getCorrects() + 1);
        }
        questionsStatisticsMapper.updateByPrimaryKeySelective(newData);
        return origin.getId();
    }


    /**
     * 更新当前试题答案选择次数
     * @param statisticsQuestionId 课后作业试题统计表 id
     * @param userAnswer 用户答案
     * @throws BizException
     */
    private synchronized void updateChoiceStatisticsCount(Long statisticsQuestionId, String userAnswer)throws BizException{
        if(null == statisticsQuestionId){
            return;
        }
        /**
         * 多选题处理
         */
        if (StringUtils.isNotBlank(userAnswer) && userAnswer.toCharArray().length > 1) {
            Stream.of(userAnswer.toCharArray()).forEach(item ->{
                int choice = Integer.parseInt(item + "");
                updateChoiceStatisticsCount(statisticsQuestionId, choice);
            });
        } else {
            /**
             * 单选题处理
             */
            updateChoiceStatisticsCount(statisticsQuestionId, Integer.valueOf(userAnswer));
        }
    }

    /**
     * 每道题的更新次数
     * @param statisticsQuestionId
     * @param userAnswer
     */
    private synchronized void updateChoiceStatisticsCount(Long statisticsQuestionId, Integer userAnswer){
        if(null == statisticsQuestionId || null == userAnswer){
            return;
        }
        Example example = new Example(CourseExercisesChoicesStatistics.class);
        example.and()
                .andEqualTo("questionId", statisticsQuestionId)
                .andEqualTo("choice", userAnswer)
                .andEqualTo("status", YesOrNoStatus.YES.getCode());

        CourseExercisesChoicesStatistics origin = choicesStatisticsMapper.selectOneByExample(example);
        if(null == origin){
            log.error("试题选项信息查询不到:statisticsQuestionId:{},userAnswer:{}", statisticsQuestionId, userAnswer);
            return;
        }
        CourseExercisesChoicesStatistics update = new CourseExercisesChoicesStatistics();
        update.setCounts(origin.getCounts() + 1);
        update.setGmtModify(new Timestamp(System.currentTimeMillis()));
        update.setId(origin.getId());
        choicesStatisticsMapper.updateByPrimaryKeySelective(update);
    }



    /**
     * 创建课后 每道题 & 每道题选项统计的信息
     * @param statisticsId
     * @param questionId
     * @param choiceSize
     */
    private void createQuestionsStatistics(long statisticsId, long questionId, int choiceSize){
        CourseExercisesQuestionsStatistics questionsStatistics = new CourseExercisesQuestionsStatistics();
        questionsStatistics.setCorrects(0);
        questionsStatistics.setCounts(0);
        questionsStatistics.setStatus(YesOrNoStatus.YES.getCode());
        questionsStatistics.setStatisticsId(statisticsId);
        questionsStatistics.setQuestionId(questionId);
        questionsStatistics.setGmtCreate(new Timestamp(System.currentTimeMillis()));
        questionsStatistics.setGmtModify(new Timestamp(System.currentTimeMillis()));
        questionsStatisticsMapper.insertSelective(questionsStatistics);
        if(choiceSize <= 0){
            return;
        }
        for (int choice = 1; choice <= choiceSize; choice ++){
            CourseExercisesChoicesStatistics courseExercisesChoicesStatistics = new CourseExercisesChoicesStatistics();
            courseExercisesChoicesStatistics.setQuestionId(questionsStatistics.getId());
            courseExercisesChoicesStatistics.setChoice(choice);
            courseExercisesChoicesStatistics.setCounts(0);
            courseExercisesChoicesStatistics.setStatus(YesOrNoStatus.YES.getCode());
            courseExercisesChoicesStatistics.setGmtCreate(new Timestamp(System.currentTimeMillis()));
            courseExercisesChoicesStatistics.setGmtModify(new Timestamp(System.currentTimeMillis()));
            choicesStatisticsMapper.insertSelective(courseExercisesChoicesStatistics);
        }
    }

    /**
     * 课后作业的统计信息
     * @param params
     * @return
     * @throws BizException
     */
    public Object statistics(List<Map<String,Object>> params) throws BizException{
        Map<String, Object> defaultResult = Maps.newHashMap();

        defaultResult.put("count", 0);
        defaultResult.put("percent", new BigDecimal(0d).setScale(1,  BigDecimal.ROUND_HALF_UP).doubleValue());
        defaultResult.put("id", 0L);

        List<Map> result = Lists.newArrayList();
        if(CollectionUtils.isEmpty(params)){
            return result;
        }
        for (Map<String, Object> param : params) {
            int courseType = MapUtils.getIntValue(param, "courseType");
            long courseId = MapUtils.getLongValue(param, "courseId");
            CourseWareTypeEnum.VideoTypeEnum courseTypeEnum = CourseWareTypeEnum.VideoTypeEnum.create(courseType);
            if(courseTypeEnum == CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK){
                Long bjyRoomId = MapUtils.getLong(param,"bjyRoomId");
                if(null == bjyRoomId || bjyRoomId.longValue() == 0){
                    param.putAll(defaultResult);
                    result.add(param);
                    continue;
                }else{
                    SimpleCourseLiveBackLog courseLiveBackLog = courseLiveBackLogService.findByRoomIdAndLiveCourseWareIdV2(bjyRoomId, courseId);
                    if(null == courseLiveBackLog || null == courseLiveBackLog.getLiveCourseWareId()){
                        param.putAll(defaultResult);
                        result.add(param);
                        continue;
                    }else{
                        courseId = courseLiveBackLog.getLiveCourseWareId();
                        courseType = CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType();
                    }
                }
            }
            Example example = new Example(CourseExercisesStatistics.class);
            example.and().andEqualTo("courseType", courseType)
                    .andEqualTo("courseId", courseId)
                    .andEqualTo("status", YesOrNoStatus.YES.getCode());
            CourseExercisesStatistics statistics = courseExercisesStatisticsMapper.selectOneByExample(example);
            if(null == statistics){
                param.putAll(defaultResult);
                result.add(param);
                continue;
            }else{
                double correctCate = ((double) statistics.getCorrects() / (statistics.getCounts() * statistics.getQuestionCount())) * 100;
                correctCate = new BigDecimal(correctCate).setScale(1,  BigDecimal.ROUND_HALF_UP).doubleValue();
                param.put("count", statistics.getCounts());
                param.put("percent", correctCate);
                param.put("id", statistics.getId());
                result.add(param);
            }
        }
       return result;
    }

    /**
     * 课后作业的详细统计信息
     * @param statisticsTableId 统计主表 id
     * @return
     * @throws BizException
     */
    public Object statisticsDetail(long statisticsTableId)throws BizException{
        List<QuestionInfoWithStatistics> result = Lists.newArrayList();
        Example example = new Example(CourseExercisesQuestionsStatistics.class);
        example.and().andEqualTo("statisticsId", statisticsTableId)
                .andEqualTo("status", YesOrNoStatus.YES.getCode());
        /**
         * 试题id <-> 试题计信息
         */
        List<CourseExercisesQuestionsStatistics> questionsStatisticsList = questionsStatisticsMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(questionsStatisticsList)){
            return result;
        }
        Map<Long, CourseExercisesQuestionsStatistics> questionsStatisticsMap = questionsStatisticsList.stream().collect(Collectors.toMap(i -> i.getQuestionId(), i -> i));

        /**
         * 试题id <-> 试题原生信息
         */
        List<QuestionInfo> baseQuestionInfoList = questionInfoService.getBaseQuestionInfo(Lists.newArrayList(questionsStatisticsMap.keySet()));
        if(CollectionUtils.isEmpty(baseQuestionInfoList)){
            return result;
        }
        Map<Long, QuestionInfo> baseQuestionInfoMap = baseQuestionInfoList.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));

        baseQuestionInfoList.forEach(item-> {
            Long questionId = item.getId();
            QuestionInfo questionInfo = baseQuestionInfoMap.get(questionId);
            CourseExercisesQuestionsStatistics questionsStatistics = questionsStatisticsMap.get(questionId);
            QuestionInfoWithStatistics questionInfoWithStatistics = constructorQuestionStatistics(questionInfo, questionsStatistics);
            result.add(questionInfoWithStatistics);
        });
        return result;
    }

    /**
     * 试题统计信息详情构建
     * @param questionInfo
     * @param questionsStatistics
     * @return
     */
    private QuestionInfoWithStatistics constructorQuestionStatistics(QuestionInfo questionInfo, CourseExercisesQuestionsStatistics questionsStatistics){
        QuestionInfoWithStatistics questionInfoWithStatistics = new QuestionInfoWithStatistics();
        if(null == questionInfo || null == questionsStatistics){
            log.error("获取试题统计信息数据为空:questionInfo:{}, questionsStatistics:{}",JSONObject.toJSONString(questionInfo), JSONObject.toJSONString(questionsStatistics));
            return questionInfoWithStatistics;
        }
        double correctRate = ((double) questionsStatistics.getCorrects() / questionsStatistics.getCounts()) * 100;
        correctRate = new BigDecimal(correctRate).setScale(1,  BigDecimal.ROUND_HALF_UP).doubleValue();
        questionInfoWithStatistics.setQuestionInfo(questionInfo);
        questionInfoWithStatistics.setCount(questionsStatistics.getCounts());
        questionInfoWithStatistics.setCorrectRate(correctRate);
        List<Double> choiceRate = constructorChoiceStatistics(questionsStatistics.getId());
        questionInfoWithStatistics.setChoiceRate(choiceRate);
        return questionInfoWithStatistics;

    }

    /**
     * 试题选项统计信息详情构建
     * @param questionTableId
     * @return
     */
    private List<Double> constructorChoiceStatistics(Long questionTableId){
        List<Double> list = Lists.newArrayList();
        Example example = new Example(CourseExercisesChoicesStatistics.class);
        example.and()
                .andEqualTo("questionId", questionTableId)
                .andEqualTo("status", YesOrNoStatus.YES.getCode());
        example.orderBy("choice").asc();

        List<CourseExercisesChoicesStatistics> choicesStatistics = choicesStatisticsMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(choicesStatistics)){
            return list;
        }
        int sum = choicesStatistics.stream().mapToInt(CourseExercisesChoicesStatistics::getCounts).sum();
        if(sum == 0){
            return list;
        }
        choicesStatistics.forEach(choice ->{
            double choiceRate = ((double) choice.getCounts() / sum) * 100;
            choiceRate = new BigDecimal(choiceRate).setScale(1,  BigDecimal.ROUND_HALF_UP).doubleValue();
            list.add(choiceRate);
        });
        return list;
    }


    /**
     * 获取课后作业排名统计信息
     * @param practiceCard
     * @return
     * @throws BizException
     */
    public Map<String, Object> obtainCourseRankInfo(PracticeCard practiceCard, int terminal)throws BizException{
        Map<String, Object> rankInfo = Maps.newHashMap();
        rankInfo.put("avgTimeCost", 0);
        rankInfo.put("avgCorrect", 0);
        rankInfo.put("maxCorrect", 0);
        rankInfo.put("myRank", 0);
        rankInfo.put("ranks", Lists.newArrayList());
        log.debug("课后作业答题卡信息:{}", JSONObject.toJSONString(practiceCard));
        try {
            PracticeForCoursePaper practiceForCoursePaper = (PracticeForCoursePaper) practiceCard.getPaper();
            if(null == practiceForCoursePaper){
                return rankInfo;
            }
            final long courseId = practiceForCoursePaper.getCourseId();
            final int courseType = practiceForCoursePaper.getCourseType();
            final long userId = practiceCard.getUserId();
            Example example = new Example(CourseExercisesStatistics.class);
            example.and()
                    .andEqualTo("courseId", courseId)
                    .andEqualTo("courseType", courseType)
                    .andEqualTo("status", YesOrNoStatus.YES.getCode());

            CourseExercisesStatistics courseExercisesStatistics = courseExercisesStatisticsMapper.selectOneByExample(example);
            if(null == courseExercisesStatistics){
                return rankInfo;
            }
            rankInfo.put("avgTimeCost", courseExercisesStatistics.getCosts() / courseExercisesStatistics.getCounts());
            rankInfo.put("avgCorrect", courseExercisesStatistics.getCorrects() / courseExercisesStatistics.getCounts());
            String existsKey = CourseCacheKey.getCourseWorkDealData(courseType, courseId);
            String rankKey = CourseCacheKey.getCourseWorkRankInfo(courseType, courseId);
            log.info("obtainCourseRankInfo >>>> existsKey:{}, rankKey:{}, userId:{}",existsKey, rankKey, practiceCard.getUserId());
            if(!redisTemplate.hasKey(existsKey) || !redisTemplate.hasKey(rankKey)){
                log.info("课后作业下用户第一个提交:courseType:{},courseId:{},userId:{}",courseType, courseId, userId);
                this.dealCourseExercisesStatistics(practiceCard);
            }

            HashOperations<String, String, String> existHash = redisTemplate.opsForHash();
            ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
            long myRank;
            try{
                myRank = zSetOperations.rank(rankKey, String.valueOf(practiceCard.getUserId())) + 1;
            }catch (Exception e){
                myRank = 0L;
                log.error("课后作业统计排名异常 rankKey:{}, terminal:{}, value:{}, errorMsg:{}", rankKey, terminal, JSONObject.toJSONString(practiceCard), e);
                e.printStackTrace();
            }

            Set<String> userIdRanks = zSetOperations.range(rankKey, START, END);
            if(CollectionUtils.isEmpty(userIdRanks)){
                return rankInfo;
            }
            List<UserRankInfo> userRankInfoArrayList = Lists.newArrayList();
            userRankInfoArrayList.addAll(userIdRanks.stream().map(item -> {
                String value = existHash.get(existsKey, item);
                UserRankInfo userRankInfo = JSONObject.parseObject(value, UserRankInfo.class);
                return userRankInfo;
            }).collect(Collectors.toList()));

            /*List<Long> userIds = userRankInfoArrayList.stream().map(UserRankInfo::getUid).collect(Collectors.toList());
            UserRankInfo userRankInfo = UserRankInfo
                    .builder()
                    .uid(userId)
                    .submitTimeInfo(practiceCard.getCreateTime())
                    .rcount(practiceCard.getRcount())
                    .expendTime(Arrays.stream(practiceCard.getTimes()).sum())
                    .build();*/
            //用户排名列表整理
            /*if(!userIds.contains(userId)) {
                if(userIds.size() < COUNT){
                    userRankInfoArrayList.add(userRankInfo);
                }else{
                    userRankInfoArrayList.add(END, userRankInfo);
                    userRankInfoArrayList = userRankInfoArrayList.subList(START, COUNT);
                }
            }*/

            UserRankInfo top = userRankInfoArrayList.get(0);
            rankInfo.put("maxCorrect", top.getRcount());
            rankInfo.put("ranks", dealRanks(userRankInfoArrayList));
            rankInfo.put("myRank", myRank);
            return rankInfo;
        }catch (Exception e){
            log.info("答题卡信息:{}",JSONObject.toJSONString(practiceCard));
            log.error("获取课作业排名统计信息异常!{}", e);
            return rankInfo;
        }
    }


    /**
     * 处理课后作业排名
     * @param userRankInfoArrayList
     * @return
     * @throws BizException
     */
    private List<UserRankInfo> dealRanks(List<UserRankInfo> userRankInfoArrayList) throws BizException{
        List<String> userIds = userRankInfoArrayList.stream().map(item->{
            long uId = item.getUid();
            return String.valueOf(uId);
        }).collect(Collectors.toList());
        NetSchoolResponse netSchoolResponse = userService.getUserLevelBatch(userIds);
        Map<String, Map<String, Object>> userInfoMaps = Maps.newHashMap();
        List<Map<String, Object>> userInfoList = (List<Map<String, Object>>) netSchoolResponse.getData();
        userInfoList.forEach(item -> userInfoMaps.put(String.valueOf(item.get("id")), item));
        AtomicInteger rank = new AtomicInteger(1);
        userRankInfoArrayList.forEach(userRankInfo ->{
            Map<String,Object> detail = userInfoMaps.get(String.valueOf(userRankInfo.getUid()));
            userRankInfo.setAvatar(String.valueOf(detail.get("avatar")));
            userRankInfo.setUname(String.valueOf(detail.get("nick")));
            userRankInfo.setRank(rank.getAndIncrement());
        });
        return userRankInfoArrayList;
    }


    @NoArgsConstructor
    @Getter
    @Setter
    public static class UserRankInfo implements Serializable{
        private int rank;
        private long uid;
        private String uname;
        private String avatar;
        private int rcount;
        private int expendTime;
        private long submitTimeInfo;

        @Builder
        public UserRankInfo(int rank, long uid, String uname, String avatar, int rcount, int expendTime, long submitTimeInfo) {
            this.rank = rank;
            this.uid = uid;
            this.uname = uname;
            this.avatar = avatar;
            this.rcount = rcount;
            this.expendTime = expendTime;
            this.submitTimeInfo = submitTimeInfo;
        }
    }
}
