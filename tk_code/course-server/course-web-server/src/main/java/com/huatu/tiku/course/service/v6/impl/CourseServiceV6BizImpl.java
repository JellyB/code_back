package com.huatu.tiku.course.service.v6.impl;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.huatu.common.ErrorResult;
import com.huatu.common.Result;
import com.huatu.common.SuccessMessage;
import com.huatu.common.consts.TerminalType;
import com.huatu.common.utils.web.RequestUtil;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.common.PracticeStatusEnum;
import com.huatu.tiku.course.common.SecKillCourseInfo;
import com.huatu.tiku.course.consts.SimpleCourseLiveBackLog;
import com.huatu.tiku.course.dao.manual.CoursePracticeQuestionInfoMapper;
import com.huatu.tiku.course.netschool.api.SearchServiceV1;
import com.huatu.tiku.course.netschool.api.fall.FallbackCacheHolder;
import com.huatu.tiku.course.service.v1.practice.CourseLiveBackLogService;
import com.huatu.tiku.course.service.v1.practice.PracticeUserMetaService;
import com.huatu.tiku.entity.CoursePracticeQuestionInfo;
import com.huatu.tiku.essay.essayEnum.CourseWareTypeEnum;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import javafx.scene.paint.Stop;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.reflect.BeanUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.bean.vo.PeriodTestListVO;
import com.huatu.tiku.course.common.EstimateCourseRedisKey;
import com.huatu.tiku.course.netschool.api.v6.CourseServiceV6;
import com.huatu.tiku.course.netschool.api.v6.LessonServiceV6;
import com.huatu.tiku.course.netschool.api.v6.UserCourseServiceV6;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import com.huatu.tiku.course.service.manager.CourseExercisesStatisticsManager;
import com.huatu.tiku.course.service.v6.CourseServiceV6Biz;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.util.DateUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;
import com.huatu.tiku.course.ztk.api.v4.paper.PeriodTestServiceV4;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticeForCoursePaper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import tk.mybatis.mapper.entity.Example;


/**
 * 描述：
 *
 * @author biguodong
 *         Create time 2019-01-08 下午11:13
 **/

@Service
@Slf4j
public class CourseServiceV6BizImpl implements CourseServiceV6Biz {

    /**
     * response from php
     */
    private static final String ORIGIN_TITLE = "title";
    private static final String ORIGIN_PRICE = "price";
    private static final String ORIGIN_LIVE_TIME = "liveDate";
    /**
     * response
     */
    private static final String RESPONSE_TITLE = "courseTitle";
    private static final String RESPONSE_LIVE_TIME = "liveDate";
    private static final String RESPONSE_PRICE = "price";
    private static final String RESPONSE_CLASS_ID = "classId";

    private static final String RESPONSE_CLASS_IDS = "classIds";
    private static final String PAGE_SIZE = "pageSize";

    private static final String COURSE_LIST_FALLBACKCACHEHOLDER = "_course_list_static_data_v6";

    @Autowired
    private SearchServiceV1 searchServiceV1;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CourseServiceV6 courseService;

    @Autowired
    private UserCourseServiceV6 userCourseServiceV6;

    @Autowired
    private CourseExercisesStatisticsManager courseExercisesStatisticsManager;

    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;

    @Autowired
    private PeriodTestServiceV4 periodTestServiceV4;

    @Autowired
    private PracticeCardServiceV1 practiceCardService;

    @Autowired
    private LessonServiceV6 lessonService;

    @Autowired
    private CourseLiveBackLogService courseLiveBackLogService;

    @Autowired
    private PracticeUserMetaService practiceUserMetaService;

    @Autowired
    private CoursePracticeQuestionInfoMapper coursePracticeQuestionInfoMapper;

    /**
     * 模考大赛解析课信息,多个id使用逗号分隔
     * 模考大赛专用
     *
     * @param classIds
     * @return
     */
    @Override
    public HashMap<String, LinkedHashMap> getClassAnalysis(String classIds) {
        HashMap<String, LinkedHashMap> responseMap = Maps.newHashMap();
        SimpleDateFormat courseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            String[] classArray = classIds.split(",");
            if (ArrayUtils.isEmpty(classArray)) {
                return responseMap;
            }
            for (String s : classArray) {
                LinkedHashMap<String, Object> linkedHashMap = Maps.newLinkedHashMap();
                int classId = Integer.valueOf(s);
                NetSchoolResponse netSchoolResponse = obtainNetNetSchoolResponseFromCache(classId);
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) netSchoolResponse.getData();
                if (!result.containsKey(ORIGIN_PRICE) || !result.containsKey(ORIGIN_LIVE_TIME)) {
                    return Maps.newLinkedHashMap();
                }
                String liveDate = String.valueOf(result.get(ORIGIN_LIVE_TIME));
                if (StringUtils.isEmpty(liveDate)) {
                    liveDate = courseDateFormat.format(new Date());
                }
                Date date = courseDateFormat.parse(liveDate);
                linkedHashMap.put(RESPONSE_TITLE, result.get(ORIGIN_TITLE));
                linkedHashMap.put(RESPONSE_LIVE_TIME, date.getTime());
                linkedHashMap.put(RESPONSE_PRICE, result.get(ORIGIN_PRICE));
                linkedHashMap.put(RESPONSE_CLASS_ID, classId);
                responseMap.put(s, linkedHashMap);
            }

            return responseMap;
        } catch (Exception e) {
            log.error("parse time info error, for classId:{}", classIds);
            return Maps.newLinkedHashMap();
        }
    }

    /**
     * 分页查询小模考历史解析课列表
     *
     * @param subject
     * @param page
     * @param size
     * @param startTime
     * @param endTime   @return
     */
    @Override
    public NetSchoolResponse analysisClassList(int subject, int page, int size, long startTime, long endTime) {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        String smallEstimateCourseIdKey = EstimateCourseRedisKey.getSmallEstimateCourseIdKey(subject);
        String courseIds = "";
        try {
            Set<RedisZSetCommands.Tuple> tuples = connection.zRevRangeWithScores(smallEstimateCourseIdKey.getBytes(), 0, -1);
            List<RedisZSetCommands.Tuple> all = tuples.stream().filter(i -> i.getScore().longValue() >= startTime)
                    .filter(i -> i.getScore().longValue() <= endTime)
                    .collect(Collectors.toList());
            //降序排序
            Collections.sort(all, (o1, o2) -> o2.getScore() - o1.getScore() > 0 ? 1 : -1);
            int startIndex = (page - 1) * size;
            int endIndex = page * size > all.size() ? all.size() : page * size;
            if (startIndex < 0 || startIndex >= endIndex) {
                return NetSchoolResponse.newInstance(initResultSet(page,size,startIndex,endIndex,all));
            }
            courseIds = all.subList(startIndex, endIndex)
                    .stream()
                    .map(RedisZSetCommands.Tuple::getValue)
                    .map(String::new)
                    .collect(Collectors.joining(","));
            Map<String, Object> params = Maps.newHashMap();
            params.put(RESPONSE_CLASS_IDS, courseIds);
            params.put(PAGE_SIZE, size);
            NetSchoolResponse netSchoolResponse = courseService.analysisClassList(params);
            Object data = netSchoolResponse.getData();
            if (data instanceof LinkedHashMap) {
                sortClassById((LinkedHashMap) data, courseIds);
                fillPageResult(page,size,startIndex,endIndex,all,(LinkedHashMap)data);
            }
            return netSchoolResponse;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }

        return NetSchoolResponse.newInstance(initResultSet(page,size,0,0,Lists.newArrayList()));
    }

    private LinkedHashMap initResultSet(int page, int size, int startIndex, int endIndex, List<RedisZSetCommands.Tuple> all) {
        LinkedHashMap data = Maps.newLinkedHashMap();
        data.put("data",Lists.newArrayList());
        fillPageResult(page,size,startIndex,endIndex,all,data);
        return data;
    }

    private void fillPageResult(int page, int size, int startIndex, int endIndex, List<RedisZSetCommands.Tuple> all, LinkedHashMap data) {
        data.put("current_page", page);
        data.put("last_page", page * size >= all.size() ? page : page + 1);
        data.put("per_page", size);
        data.put("from", startIndex);
        data.put("to", endIndex);
        data.put("total", all.size());
    }

    /**
     * 返回列表倒序排序
     *
     * @param data
     * @param courseIds
     */
    private void sortClassById(LinkedHashMap data, String courseIds) {
        try{
            List<LinkedHashMap> list = (List<LinkedHashMap>) data.get("data");
            String[] split = courseIds.split(",");
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            List<LinkedHashMap> result = Lists.newArrayList();
            for (String id : split) {
                Optional<LinkedHashMap> any = list.stream().filter(i ->
                        id.equals(MapUtils.getString(i, "classId")) ||
                                id.equals(MapUtils.getString(i, "id")))
                        .findAny();
                if(any.isPresent()){
                    result.add(any.get());
                }
            }
            data.put("data",result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 缓存课程信息
     *
     * @param classId
     * @return
     */
    private NetSchoolResponse obtainNetNetSchoolResponseFromCache(int classId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(RESPONSE_CLASS_ID, classId);
        String key = CourseCacheKey.courseAnalysisV6(String.valueOf(classId));
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        NetSchoolResponse netSchoolResponse;
        if (null == valueOperations.get(key)) {
            netSchoolResponse = courseService.analysis(params);
            if (null != netSchoolResponse.getData()) {
                valueOperations.set(key, JSONObject.toJSONString(netSchoolResponse.getData()), 1, TimeUnit.DAYS);
            } else {
                log.info("obtain course info from php client error, classId:{}", classId);
                return NetSchoolResponse.newInstance(Maps.newLinkedHashMap());
            }
        } else {
            String valueStr = String.valueOf(valueOperations.get(key));
            LinkedHashMap valueData = JSONObject.parseObject(valueStr, LinkedHashMap.class);
            netSchoolResponse = NetSchoolResponse.newInstance(valueData);
        }
        return netSchoolResponse;
    }


    @Override
	public Object periodTestList(Map<String, Object> params) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		int uid = (int) params.get("userId");
		NetSchoolResponse<PeriodTestListVO> response = userCourseServiceV6.unfinishStageExamList(params);
		log.info("接口unfinish_stage_exam_list调用php响应用时:{}", String.valueOf(stopwatch.stop()));
		if (ResponseUtil.isSuccess(response)) {
			Stopwatch stopwatchExplain = Stopwatch.createStarted();
			PeriodTestListVO periodTestListVO = response.getData();
			// key为paperid_syllabusId value 为试卷信息
			Map<String, PeriodTestListVO.PeriodTestInfo> paperMap = Maps.newHashMap();
			periodTestListVO.getList().forEach(courseInfo -> {
				courseInfo.setUndoCount(courseInfo.getChild().size());
				courseInfo.getChild().forEach(periodTestInfo -> {
					//log.info("接口unfinish_stage_exam_list调用php响应返回periodTestInfo:{}", periodTestInfo.toString());
					// 填充app端展示所需时间
					periodTestInfo.setShowTime(
							DateUtil.getSimpleDate(periodTestInfo.getStartTime(), periodTestInfo.getEndTime()));
					periodTestInfo.setIsExpired(DateUtil.isExpired(periodTestInfo.getEndTime()));
					periodTestInfo.setIsAlert(periodTestInfo.getAlreadyRead() == 0 ? 1 : 0);
					paperMap.put(periodTestInfo.getExamId() + "_" + periodTestInfo.getSyllabusId(), periodTestInfo);
				});
			});
			
			Set<String> paperSyllabusSet = paperMap.keySet();
			if (!paperSyllabusSet.isEmpty()) {
				NetSchoolResponse<Map<String, Integer>> bathResponse = periodTestServiceV4.getPaperStatusBath(uid,
						paperSyllabusSet);
				if (ResponseUtil.isSuccess(bathResponse)) {
					// key为paperid_syllabusId value 为试卷状态
					Map<String, Integer> retMap = bathResponse.getData();
					log.info("getPaperStatusBath ret is:{}", retMap.toString());
					// 修改试卷状态
					for (Entry<String, Integer> paperRet : retMap.entrySet()) {
						paperMap.get(paperRet.getKey()).setStatus(paperRet.getValue());
					}
				}
				log.info("getpaper param is:{}", paperSyllabusSet.toString());
			}
			log.info("接口unfinish_stage_exam_list解析用时:{}", String.valueOf(stopwatchExplain.stop()));
			return periodTestListVO;
		}
		return new PeriodTestListVO();
	}

    /**
     * 查询我的课后作业报告
     *
     * @param userSession
     * @param terminal
     * @param cardId
     * @return
     * @throws BizException
     */
    @Override
    public Object courseWorkReport(UserSession userSession, int terminal , long cardId) throws BizException {
        SimpleDateFormat courseDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        NetSchoolResponse netSchoolResponse = practiceCardService.getAnswerCard(userSession.getToken(), terminal, cardId);
        if(null == netSchoolResponse.getData()){
            log.error("课后作业答题卡信息不存在:{}", cardId);
            return new JSONObject();
        }
        Object response = ResponseUtil.build(netSchoolResponse);
        JSONObject data = new JSONObject((LinkedHashMap<String, Object>) response);

        JSONObject paper = data.getJSONObject("paper");
        PracticeForCoursePaper practiceForCoursePaper = JSONObject.parseObject(paper.toJSONString(), PracticeForCoursePaper.class);

        PracticeCard practiceCard = JSONObject.parseObject(data.toJSONString(), PracticeCard.class);
        practiceCard.setPaper(practiceForCoursePaper);
        if(!checkUserSubmitAnswerCard(userSession.getId(), practiceForCoursePaper.getCourseId(), practiceForCoursePaper.getCourseType())){
            if(practiceCard.getStatus() == AnswerCardStatus.FINISH){
                courseExercisesProcessLogManager.submitCourseWorkAnswerCard(practiceCard);
            }
            if(terminal == TerminalType.PC){
                Stopwatch stopwatch = Stopwatch.createStarted();
                log.info("如果为pc端查看课后作业，入库并重新计算一次统计信息,耗时:{}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }else{
                log.error("学员没有提交答题卡:userId:{}, courseWareId:{}, videoType:{}", userSession.getId(), practiceForCoursePaper.getCourseId(), practiceForCoursePaper.getCourseType());
                ErrorResult errorResult = ErrorResult.create(10000103, "请先提交答题卡后查看报告！");
                throw new BizException(errorResult);
            }
        }
        List<QuestionPointTree> points_ = Lists.newArrayList();
        Map<String,Object> paperInfo = Maps.newHashMap();

        List<QuestionPointTree> level1Points = practiceCard.getPoints();
        level1Points.forEach(level1Item -> {
            if(CollectionUtils.isNotEmpty(level1Item.getChildren())){
                List<QuestionPointTree> level2Points = level1Item.getChildren();
                level2Points.forEach(level2Item ->{
                    List<QuestionPointTree> level3Points = level2Item.getChildren();
                    if(CollectionUtils.isNotEmpty(level3Points)){
                        level3Points.forEach(level3Item -> points_.add(level3Item));
                    }
                });
            }
        });
        paperInfo.put("modules", practiceCard.getPaper().getModules());
        paperInfo.put("questions", practiceCard.getPaper().getQuestions());
        data.put("paper", paperInfo);
        data.put("points", points_);
        data.putAll(courseExercisesStatisticsManager.obtainCourseRankInfo(practiceCard, terminal));
        data.put("tcount", practiceForCoursePaper.getQcount());
        data.put("rcount", practiceCard.getRcount());
        data.put("wcount", practiceCard.getWcount());
        data.put("ucount", practiceCard.getUcount());
        data.put("timesTotal", Arrays.stream(practiceCard.getTimes()).sum());

        data.put("avgMyCost", practiceCard.getSpeed());

        Date date = new Date(practiceCard.getCreateTime() == 0 ? System.currentTimeMillis():practiceCard.getCreateTime());
        data.put("submitTimeInfo", courseDateFormat.format(date));
        return data;
    }

    /**
     * 判断用户是否已经提交了课后作业答题卡信息
     * @param userId
     * @param courseWareId
     * @param videoType
     * @return
     */
    private boolean checkUserSubmitAnswerCard(long userId, long courseWareId, int videoType){
        String existsKey = CourseCacheKey.getCourseWorkDealData(videoType, courseWareId);
        HashOperations<String, String, String> existsHash = redisTemplate.opsForHash();
        return existsHash.hasKey(existsKey, String.valueOf(userId));
    }

    /**
     * 我的学习报告
     *
     * @param userSession
     * @param bjyRoomId
     * @param classId
     * @param netClassId
     * @param courseWareId
     * @param videoType
     * @param exerciseCardId 课后作业答题卡id
     * @param terminal
     * @return
     * @throws BizException
     */
    @Override
    public Object learnReport(UserSession userSession, String bjyRoomId, long classId, long netClassId, long courseWareId, int videoType, long exerciseCardId, long syllabusId, int terminal, String cv) throws BizException {
        Map<String,Object> result = Maps.newHashMap();
        /**
         * 直播报告
         */
        Map<String,Object> liveReport = Maps.newHashMap();
        /**
         * 随堂练习
         */
        Map<String,Object> classPractice = Maps.newHashMap();
        /**
         * 课后作业
         */
        Map<String,Object> courseWorkPractice = Maps.newHashMap();
        /**
         * 课后作业知识点
         */
        List<QuestionPointTree> courseWorkPracticePoints = Lists.newArrayList();
        /**
         * 随堂练习知识点
         */
        List<Map<String,Object>> classPracticePoints = Lists.newArrayList();

        CourseWareTypeEnum.VideoTypeEnum videoTypeEnum = CourseWareTypeEnum.VideoTypeEnum.create(videoType);
        //如果为录播回放，查看回放是否生成
        boolean playBackAvailable = false;
        if(videoTypeEnum == CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK){
            SimpleCourseLiveBackLog courseLiveBackLog = courseLiveBackLogService.findByRoomIdAndLiveCourseWareIdV2(Long.valueOf(bjyRoomId), courseWareId);
            if(null != courseLiveBackLog && null != courseLiveBackLog.getLiveCourseWareId()){
                courseWareId = courseLiveBackLog.getLiveCourseWareId();
                playBackAvailable = true;
            }
        }
        /**
         * 听课报告
         */
        dealLearnReportAboutLiveReport(userSession, bjyRoomId, classId, netClassId, courseWareId, videoTypeEnum, playBackAvailable, liveReport);
        /**
         * 随堂练习报告
         */
        dealLearnReportAboutWithClassReport(userSession, bjyRoomId, courseWareId, videoTypeEnum, playBackAvailable, classPractice, classPracticePoints);
        /**
         * 课后作业报告
         */
        dealLearnReportAboutCourseWork(userSession, classId, courseWareId, videoTypeEnum, exerciseCardId, syllabusId, terminal, playBackAvailable, courseWorkPractice, courseWorkPracticePoints);

        result.put("classPractice", classPractice);
        result.put("courseWorkPractice", courseWorkPractice);
        result.put("liveReport", liveReport);
        result.put("points", dealLearnReportPoints(classPracticePoints, courseWorkPracticePoints));
        result.put("teacherComment", "激励的话儿有很多，但还是自己的决心最有效。");
        return result;
    }



    /**
     * 处理学习报告 - 听课记录
     * @param userSession
     * @param bjyRoomId
     * @param classId
     * @param netClassId
     * @param courseWareId
     * @param videoType
     * @param liveReport
     * @param playBackAvailable
     */
    private void dealLearnReportAboutLiveReport(UserSession userSession, String bjyRoomId, long classId, long netClassId, final long courseWareId, final CourseWareTypeEnum.VideoTypeEnum videoType, final boolean playBackAvailable, Map<String, Object> liveReport) {


        /**
         * 处理听课记录，只有直播&回放有听课记录
         */
        if(videoType == CourseWareTypeEnum.VideoTypeEnum.LIVE || playBackAvailable){
            Stopwatch stopwatch = Stopwatch.createStarted();
            //听课记录请求参数
            Map<String,Object> studyReport = Maps.newHashMap();
            studyReport.put("bjyRoomId", bjyRoomId);
            studyReport.put("userName", userSession.getUname());
            studyReport.put("classId", classId);
            studyReport.put("netClassId", netClassId);
            studyReport.put("lessonId", courseWareId);
            studyReport.put("videoType", CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType());
            NetSchoolResponse netSchoolResponse = lessonService.studyReport(studyReport);
            if(ResponseUtil.isSuccess(netSchoolResponse)){
                LinkedHashMap<String,Object> data = (LinkedHashMap<String,Object>)netSchoolResponse.getData();
                liveReport.put("learnTime", MapUtils.getInteger(data, "listenLength"));
                liveReport.put("learnPercent", MapUtils.getInteger(data, "listenPercent"));
                liveReport.put("abovePercent", MapUtils.getInteger(data, "concentrationPercent"));
                liveReport.put("teacherComment", MapUtils.getString(data, "msg"));
                stopwatch.stop();
                log.info("学习报告 - 直播学习时长 - 请求参数:{},耗时:{}", studyReport, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }else{
            return;
        }
    }

    /**
     * 随堂练习学习报告处理
     * @param userSession
     * @param bjyRoomId
     * @param courseWareId
     * @param videoType
     * @param classPractice
     * @param classPracticePoints
     * @param playBackAvailable
     */
    private void dealLearnReportAboutWithClassReport(UserSession userSession, String bjyRoomId, final long courseWareId, final CourseWareTypeEnum.VideoTypeEnum videoType, final boolean playBackAvailable, Map<String, Object> classPractice, List<Map<String, Object>> classPracticePoints) {
        /**
         * 处理随堂随堂练习报告
         * 如果直播，并且生成了随堂练习答题卡，返回答题卡信息
         * 如果为录播，查询答题卡，返回答题卡信息
         * 如果
         */
        //直播回放或直播处理逻辑
        Stopwatch stopwatch = Stopwatch.createStarted();
        if(playBackAvailable || videoType == CourseWareTypeEnum.VideoTypeEnum.LIVE){
           if(checkClassPracticeReportAvailable(bjyRoomId)){
               NetSchoolResponse classReport = practiceCardService.getClassExerciseReport(courseWareId, CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType(), userSession.getId());
               /**
                * 如果直播答题卡id存在
                */
               if(classReport != ResponseUtil.DEFAULT_PAGE_EMPTY && null != classReport && null != classReport.getData()){
                   LinkedHashMap linkedHashMap = (LinkedHashMap<String, Object>) classReport.getData();
                   if(MapUtils.getLong(linkedHashMap, "id") > 0) {
                       classPractice.put("practiceStatus", PracticeStatusEnum.AVAILABLE.getCode());
                       classPracticePoints.addAll((List<Map<String,Object>>) linkedHashMap.get("points"));
                       classPractice.putAll(linkedHashMap);
                       classPractice.putAll(practiceUserMetaService.getCountDateByRIdAndCId(Long.valueOf(bjyRoomId), courseWareId));
                   }
               }else{
                   classPractice.put("practiceStatus", PracticeStatusEnum.MISSED_OR_UNFINISHED.getCode());
               }
           }else{
               classPractice.put("practiceStatus", PracticeStatusEnum.NONE.getCode());
           }
        }else if(videoType == CourseWareTypeEnum.VideoTypeEnum.DOT_LIVE){
            NetSchoolResponse classReport = practiceCardService.getClassExerciseReport(courseWareId, videoType.getVideoType(), userSession.getId());
            if(classReport != ResponseUtil.DEFAULT_PAGE_EMPTY && null != classReport && null != classReport.getData()){
                LinkedHashMap linkedHashMap = (LinkedHashMap<String, Object>) classReport.getData();
                classPractice.putAll(linkedHashMap);
                classPracticePoints.addAll((List<Map<String,Object>>) linkedHashMap.get("points"));
                int wcount = MapUtils.getIntValue(linkedHashMap, "wcount");
                int rcount = MapUtils.getIntValue(linkedHashMap, "rcount");
                if((rcount + wcount) > 0){
                    classPractice.put("practiceStatus", PracticeStatusEnum.AVAILABLE.getCode());
                }else{
                    classPractice.put("practiceStatus", PracticeStatusEnum.MISSED_OR_UNFINISHED.getCode());
                }
            }else{
                classPractice.put("practiceStatus", PracticeStatusEnum.MISSED_OR_UNFINISHED.getCode());
            }
        }else{
            classPractice.put("practiceStatus", PracticeStatusEnum.NONE.getCode());
        }
        log.info("学习报告 - 随堂练习 - 请求参数:{},{},{},{},{},耗时:{}", userSession.getId(), bjyRoomId, courseWareId, videoType.getVideoType(),playBackAvailable, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    /**
     * 查看此房间下是否出题
     * @param bjyRoomId
     * @return
     */
    private boolean checkClassPracticeReportAvailable(String bjyRoomId){
        try{
            Example example = new Example(CoursePracticeQuestionInfo.class);
            List<Integer> bizStatus = Lists.newArrayList(2,3);
            example.and()
                    .andEqualTo("roomId", Long.valueOf(bjyRoomId))
                    .andIn("bizStatus", bizStatus);
            List<CoursePracticeQuestionInfo> list = coursePracticeQuestionInfoMapper.selectByExample(example);
            return CollectionUtils.isEmpty(list) ? false : true;
        }catch (Exception e){
            log.error("随堂练习 - 老师是否放题异常:{}", e);
            return false;
        }
    }
    /**
     * 处理学习报告 -- 课后作业
     * @param userSession 用户信息
     * @param classId 课程id
     * @param courseWareId 课件
     * @param videoType 课件类型
     * @param exerciseCardId 课后作业答题卡
     * @param syllabusId  大纲
     * @param terminal 用户终端
     * @param courseWorkPractice 课后作业报告信息
     * @param courseWorkPracticePoints 课后作业知识点汇总信息
     */
    private void dealLearnReportAboutCourseWork(UserSession userSession, long classId, final long courseWareId, final CourseWareTypeEnum.VideoTypeEnum videoType, long exerciseCardId, long syllabusId, int terminal, final boolean playBackAvailable, Map<String, Object> courseWorkPractice, List<QuestionPointTree> courseWorkPracticePoints) {
        /**
         * 处理课后作业报告，如果用户主动提交了答题卡信息处理 practiceStatus 置为 1
         * 否则提示学员去做题界面做题并提交答题卡
         */
        Stopwatch stopWatch = Stopwatch.createStarted();
        boolean checkUserSubmitAnswerCard;
        if(videoType == CourseWareTypeEnum.VideoTypeEnum.LIVE || playBackAvailable){
            checkUserSubmitAnswerCard = checkUserSubmitAnswerCard(userSession.getId(), courseWareId, CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType());
        }else{
            checkUserSubmitAnswerCard = checkUserSubmitAnswerCard(userSession.getId(), courseWareId, videoType.getVideoType());
        }
        if(checkUserSubmitAnswerCard && exerciseCardId > 0){
            Map<String, Object> temp = (Map<String, Object>)courseWorkReport(userSession, terminal, exerciseCardId);
            courseWorkPractice.put("answers", temp.get("answers"));
            courseWorkPractice.put("avgCorrect", temp.get("avgCorrect"));
            courseWorkPractice.put("avgMyCost", temp.get("avgMyCost"));
            courseWorkPractice.put("avgTimeCost", temp.get("avgTimeCost"));
            courseWorkPractice.put("corrects", temp.get("corrects"));
            courseWorkPractice.put("doubts", temp.get("doubts"));
            courseWorkPractice.put("id", MapUtils.getString(temp, "id"));
            courseWorkPractice.put("paper", temp.get("paper"));
            courseWorkPractice.put("rcount", temp.get("rcount"));
            courseWorkPractice.put("wcount", temp.get("wcount"));
            courseWorkPractice.put("ucount", temp.get("ucount"));
            courseWorkPractice.put("tcount", temp.get("tcount"));
            courseWorkPractice.put("timesTotal", temp.get("timesTotal"));
            courseWorkPractice.put("practiceStatus", PracticeStatusEnum.AVAILABLE.getCode());
            courseWorkPractice.put("submitTimeInfo", temp.get("submitTimeInfo"));
            courseWorkPracticePoints.addAll((List<QuestionPointTree>) temp.getOrDefault("points", Lists.<QuestionPointTree>newArrayList()));
        }else{
            if(exerciseCardId > 0){
                courseWorkPractice.put("id", String.valueOf(exerciseCardId));
                courseWorkPractice.put("practiceStatus", PracticeStatusEnum.MISSED_OR_UNFINISHED.getCode());
            }else{
                courseWorkPractice.put("id", String.valueOf(exerciseCardId));
                courseWorkPractice.put("practiceStatus", PracticeStatusEnum.NONE.getCode());
            }
        }
        log.info("学习报告 - 课后作业 - 课后作业答题卡信息:{},{},{},{},{}, 耗时:{}", classId, syllabusId, videoType.getVideoType(), courseWareId, userSession.getId(), stopWatch.elapsed(TimeUnit.MILLISECONDS));
    }

    /**
     * 处理课件知识点id
     * @param classPracticePoints 随堂练习知识点
     * @param courseWorkPracticePoints 课后练习知识点
     * @return
     * @throws BizException
     */
    private List<QuestionPointTree> dealLearnReportPoints(List<Map<String,Object>> classPracticePoints, List<QuestionPointTree> courseWorkPracticePoints)throws BizException{
        try{
            Map<Integer, QuestionPointTree> classPracticePointsMap  = classPracticePoints.stream()
                    .collect(Collectors.toMap(
                            item->{
                                int id = MapUtils.getIntValue(item, "id");
                                return id;
                            },item->{
                                QuestionPointTree questionPointTree = BeanUtil.fromMap(QuestionPointTree.class, item);
                                return questionPointTree;
                            }));
            Map<Integer, QuestionPointTree> courseWorkPracticePointsMap  = courseWorkPracticePoints.stream().collect(Collectors.toMap(item-> item.getId(), item->item));
            List<QuestionPointTree> pointTrees = Lists.newArrayList();

            List<Integer> commonPointIds = Lists.newArrayList();
            if(CollectionUtils.isEmpty(classPracticePointsMap.keySet()) && CollectionUtils.isEmpty(courseWorkPracticePointsMap.keySet())){
                return pointTrees;
            }else if(CollectionUtils.isEmpty(classPracticePointsMap.keySet()) && CollectionUtils.isNotEmpty(courseWorkPracticePointsMap.keySet())){
                return Lists.newArrayList(courseWorkPracticePointsMap.values());
            }else if(CollectionUtils.isNotEmpty(classPracticePointsMap.keySet()) && CollectionUtils.isEmpty(courseWorkPracticePointsMap.keySet())){
                return Lists.newArrayList(classPracticePointsMap.values());
            }else{
                Predicate<QuestionPointTree> predicate = current -> courseWorkPracticePointsMap.keySet().contains(current.getId());
                classPracticePointsMap.keySet().forEach(sId -> {
                    QuestionPointTree middelPoint = classPracticePointsMap.get(sId);
                    if(predicate.test(middelPoint)){
                        pointTrees.add(dealCommonPoints(middelPoint, courseWorkPracticePointsMap.get(sId)));
                        commonPointIds.add(sId);
                    }else{
                        pointTrees.add(middelPoint);
                    }
                });
            }
            courseWorkPracticePointsMap.keySet().forEach(sId -> {
                if(CollectionUtils.isNotEmpty(commonPointIds) && !commonPointIds.contains(sId)){
                    pointTrees.add(courseWorkPracticePointsMap.get(sId));
                }
            });
            return pointTrees;
        }catch (Exception e){
            log.error("处理随堂练习&课后练习汇总知识点异常:{}", e);
            return Lists.newArrayList();
        }
    }

    /**
     * 处理随堂练练习 & 课后作业汇总知识点
     * @param middlePoint
     * @param afterPoint
     * @return
     */
    private QuestionPointTree dealCommonPoints(QuestionPointTree middlePoint, QuestionPointTree afterPoint){
        QuestionPointTree total = new QuestionPointTree();
        total.setQnum(middlePoint.getQnum() + afterPoint.getQnum());
        total.setRnum(middlePoint.getRnum() + afterPoint.getRnum());
        total.setWnum(middlePoint.getWnum() + afterPoint.getWnum());
        total.setUnum(middlePoint.getUnum() + afterPoint.getUnum());
        total.setTimes(middlePoint.getTimes() + afterPoint.getTimes());
        total.setId(middlePoint.getId());
        total.setName(middlePoint.getName());
        total.setSpeed(total.getTimes() / total.getQnum());
        int questionNum = total.getRnum() + total.getWnum();
        /**
         * 计算平均时间&正确率
         */
        int speed = total.getTimes() / questionNum;
        double accuracy = 0;
        if (questionNum > 0) {
            accuracy = new BigDecimal(total.getRnum() * 100).divide(new BigDecimal(questionNum), 1, RoundingMode.HALF_UP).doubleValue();
        }
        total.setAccuracy(speed);
        total.setAccuracy(accuracy);
        return total;
    }

    /**
     * 我的课程列表
     *
     * @param params
     * @return
     * @throws BizException
     */
    @Override
    @Degrade(key = "obtainCourseListV6", name = "课程列表v6")
    public Object obtainCourseList(Map<String, Object> params) throws BizException {
        if(params.containsKey("userName")){
            params.remove("userName");
        }
        log.info("obtainCourseList normal params:{}", params);
        NetSchoolResponse netSchoolResponse = courseService.obtainCourseList(params);
        if(null != netSchoolResponse && null !=  netSchoolResponse.getData() && ResponseUtil.isHardSuccess(netSchoolResponse)){
            this.setCourseList2FallbackCacheHolder(params, netSchoolResponse);
        }
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * data fallbackCacheHolder
     * @param params
     * @param response
     */
    private void setCourseList2FallbackCacheHolder(Map<String, Object> params, NetSchoolResponse response){
        String key = COURSE_LIST_FALLBACKCACHEHOLDER + RequestUtil.getParamSign(params);
        FallbackCacheHolder.put(key, response);
    }

    /**
     * 课程接口degrade 处理
     * 获取课程 startTime & stopTime
     * @param params
     * @return
     * @throws BizException
     */
    public Object obtainCourseListDegrade(Map<String, Object> params) throws BizException {
        log.warn("obtainCourseList degrade params:{}", params);
        if(params.containsKey("userName")){
            params.remove("userName");
        }
        String key = COURSE_LIST_FALLBACKCACHEHOLDER + RequestUtil.getParamSign(params);
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(!ResponseUtil.isHardSuccess(response)){
            log.warn("obtain obtainCourseList degrade data not exist in fallbackHolder...");
            return ResponseUtil.build(new NetSchoolResponse(Result.SUCCESS_CODE, "", Lists.newArrayList()));
        }

        Object object =  ResponseUtil.build(response);
        log.info("过滤秒杀课....");
        filterStartTime((List<LinkedHashMap>) object);
        return object;
    }


    /**
     * 秒杀课程数据降级 - filter 处理
     * @param response
     */
    private void filterStartTime(List<LinkedHashMap> response){
        if(CollectionUtils.isEmpty(response)){
            return;
        }
        SecKillCourseInfo instance = SecKillCourseInfo.getInstance();
        for(LinkedHashMap<String, Object> currentCateInfo : response){
            if(!currentCateInfo.containsKey("data")){
                continue;
            }
            List<LinkedHashMap<String, Object>> detailList = (List<LinkedHashMap<String, Object>>)currentCateInfo.get("data");
            if(CollectionUtils.isEmpty(detailList)){
                continue;
            }

            for(LinkedHashMap<String, Object> detailInfo : detailList){
                //合集课不处理
                if(MapUtils.getBoolean(detailInfo, "isCollect")){
                    log.info("合集课程不支持 ms 不予处理:{}", detailInfo);
                    continue;
                }
                if(!detailInfo.containsKey("classId")){
                    log.error("ms 课程信息参数异常:{}", detailInfo);
                    continue;
                }
                String classId = MapUtils.getString(detailInfo, "classId");
                if(null == instance || StringUtils.isEmpty(instance.getClassId())){
                    continue;
                }
                if(!classId.equals(instance.getClassId())){
                    continue;
                }
                long startTimeStamp = MapUtils.getLong(detailInfo, "startTimeStamp");
                long stopTimeStamp = MapUtils.getLong(detailInfo, "stopTimeStamp");
                if(startTimeStamp > (System.currentTimeMillis() / 1000)){
                    log.info("------------> current time:{}, start time:{}", (System.currentTimeMillis() / 1000),  startTimeStamp);
                    long saleStart = startTimeStamp - (System.currentTimeMillis() / 1000);
                    long saleEnd = stopTimeStamp  - startTimeStamp;
                    detailInfo.put("saleStart", saleStart);
                    detailInfo.put("saleEnd", saleEnd);
                    detailInfo.put("limit", instance.getLimit());
                }else{
                    detailInfo.put("saleStart", 0);
                    detailInfo.put("saleEnd", stopTimeStamp - (System.currentTimeMillis() / 1000));
                    detailInfo.put("limit", instance.getLimit());
                }
                log.info("处理后的 data info:{}", detailInfo);
            }
        }
    }

    /**
     * 添加秒杀课信息
     * @param classId
     * @param limit
     * @return
     * @throws BizException
     */
    @Override
    public Object addSecKillInfo(String classId, int limit) throws BizException {
        SecKillCourseInfo instance  = SecKillCourseInfo.getInstance();
        if(null == instance){
            return SuccessMessage.create("对象不存在");
        }
        instance.setClassId(classId);
        instance.setLimit(limit);
        log.info("更新秒杀课信息 --- classId:{}, limit:{}", classId, limit);
        return SuccessMessage.create("ok");
    }

    @Override
    public Object getUserCourseStatus(String uname, int netClassId, int collageActivityId) {
        if(StringUtils.isBlank(uname)){     //游客模式直接返回
            return new HashMap() {{put("id",netClassId);}};
        }
        StopWatch stopWatch = new StopWatch("getUserCourseStatus:"+netClassId);
        stopWatch.start("1");
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("userName",uname);
        map.put("netClassId",netClassId);
        if(collageActivityId > 0){
            map.put("collageActivityId",collageActivityId);
        }
        NetSchoolResponse netSchoolResponse = courseService.userCourseStatus(map);
        Map data = (Map)netSchoolResponse.getData();
        data.put("id",netClassId);
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        return data;
    }

    /**
     * 更新 key word 排序
     * @param token
     * @param keyWord
     * @return
     */
    @Override
    public Object upSetSearchKeyWord(String token, String keyWord) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try{
            executorService.submit(() ->{
                searchServiceV1.upSetKeyWord(token, keyWord);
            });
            log.debug("update key.word.offset:{}", keyWord);
        }catch (Exception e){
            log.error("upset keyWord offset error");
        }finally {
            executorService.shutdown();
        }
        return SuccessMessage.create("success");
    }
}
