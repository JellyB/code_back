package com.huatu.tiku.course.service.manager;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.collect.*;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.bean.vo.*;
import com.huatu.tiku.course.common.SubjectEnum;
import com.huatu.tiku.course.consts.NetschoolTerminalType;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.consts.SimpleCourseLiveBackLog;
import com.huatu.tiku.course.consts.SyllabusInfo;
import com.huatu.tiku.course.service.v1.practice.CourseLiveBackLogService;
import com.huatu.tiku.essay.essayEnum.CourseWareTypeEnum;
import lombok.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.StudyTypeEnum;
import com.huatu.tiku.course.common.YesOrNoStatus;
import com.huatu.tiku.course.dao.manual.CourseExercisesProcessLogMapper;
import com.huatu.tiku.course.netschool.api.v6.UserCourseServiceV6;
import com.huatu.tiku.course.netschool.api.v7.SyllabusServiceV7;
import com.huatu.tiku.course.service.v1.CourseExercisesService;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;
import com.huatu.tiku.entity.CourseExercisesProcessLog;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.common.AnswerCardStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import tk.mybatis.mapper.entity.Example;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-04 5:25 PM
 **/

@Component
@Slf4j
public class CourseExercisesProcessLogManager {

    @Autowired
    private CourseExercisesProcessLogMapper courseExercisesProcessLogMapper;

    @Autowired
    private CourseExercisesStatisticsManager courseExercisesStatisticsManager;

    @Autowired
    private CourseExercisesService courseExercisesService;

    @Autowired
    private PracticeCardServiceV1 practiceCardService;

    @Autowired
    private SyllabusServiceV7 syllabusService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserCourseServiceV6 userCourseServiceV6;

    @Autowired
    private EssayExercisesAnswerMetaManager essayExercisesAnswerMetaManager;

    @Autowired
    private CourseLiveBackLogService courseLiveBackLogService;

    public static final String LESSON_LABEL = "lesson";

    public static final String COURSE_LABEL = "course";

    private static final long PERIOD_TIME = 60 * 1000;

    private static final String CORRECT_DATA_KEY = "data_correct_2019";
    private static final String CORRECT_DATA_SWITCH = "data_correct_2019_switch";
    private static final String CORRECT_DATA_SWITCH_ON = "on";

    /**
     * 获取类型未读量
     * @param userId
     * @return
     */
    @Degrade(key = "unFinishNumV6", name = "学习面板 - 未读数")
    public Map<String, Integer> getCountByType(long userId,String userName) throws BizException{
        List<Integer> list = Lists.newArrayList(AnswerCardStatus.CREATE, AnswerCardStatus.UNDONE);
        StopWatch stopWatch = new StopWatch("学习面板-未读数");
        stopWatch.start("课后作业");
        Map<String, Integer> result = Maps.newHashMap();
        Map<String, String> param = Maps.newHashMap();
        Example example = new Example(CourseExercisesProcessLog.class);
        Example.Criteria criteria = example.and();
        criteria.andEqualTo("userId", userId);
        criteria.andEqualTo("status", YesOrNoStatus.YES.getCode());
        criteria.andEqualTo("isAlert", YesOrNoStatus.YES.getCode());
        criteria.andIn("bizStatus", list);
        criteria.andEqualTo("dataType", StudyTypeEnum.COURSE_WORK.getOrder());
        int countWork = courseExercisesProcessLogMapper.selectCountByExample(example);
        stopWatch.stop();
        stopWatch.start("阶段测试");
        result.put(StudyTypeEnum.COURSE_WORK.getKey(), countWork);
        //获取总数量
        param.put("userName",userName );
        NetSchoolResponse response  = userCourseServiceV6.unfinishStageExamCount(param);
        stopWatch.stop();
        log.info("学习面板-未读数 pretty:{}", stopWatch.prettyPrint());
		if (ResponseUtil.isSuccess(response)) {
			Map<String, Integer> retMap = (Map<String, Integer>) response.getData();
			Integer count = retMap.get("num");
			log.info("用户:{}需要提醒的阶段测试数为:{}", userName, count);
			result.put(StudyTypeEnum.PERIOD_TEST.getKey(), count);
		} else {
			result.put(StudyTypeEnum.PERIOD_TEST.getKey(), 0);
		}
        return result;
    }

    /**
     * 学习面板 - 降级处理
     * @param userId
     * @param userName
     * @return
     * @throws BizException
     */
    public Map<String, Integer> getCountByTypeDegrade(long userId,String userName) throws BizException{
        Map<String, Integer> result = Maps.newHashMap();
        result.put(StudyTypeEnum.COURSE_WORK.getKey(), 0);
        result.put(StudyTypeEnum.PERIOD_TEST.getKey(), 0);
        return result;
    }



    /**
     * 单种类型全部已读
     * @param userId
     * @param type
     * @return
     */
	public int allReadByType(long userId, String type, String uName) throws BizException {
		try {
			StudyTypeEnum studyTypeEnum = StudyTypeEnum.create(type);
			if (studyTypeEnum.equals(StudyTypeEnum.COURSE_WORK)) {
				CourseExercisesProcessLog courseExercisesProcessLog = new CourseExercisesProcessLog();
				courseExercisesProcessLog.setGmtModify(new Timestamp(System.currentTimeMillis()));
				courseExercisesProcessLog.setIsAlert(YesOrNoStatus.NO.getCode());

				Example example = new Example(CourseExercisesProcessLog.class);
				example.and().andEqualTo("status", YesOrNoStatus.YES.getCode())
						.andEqualTo("dataType", studyTypeEnum.getOrder()).andEqualTo("userId", userId)
						.andEqualTo("isAlert", YesOrNoStatus.YES.getCode());
				int count = courseExercisesProcessLogMapper.updateByExampleSelective(courseExercisesProcessLog,
						example);
				return count;
			} else {
				// 阶段测试全部已读
				Map<String, Object> param = Maps.newHashMap();
				param.put("userName", uName);
				param.put("type", YesOrNoStatus.YES.getCode());
				NetSchoolResponse response = userCourseServiceV6.readPeriod(param);
				log.info("用户{}全部已读阶段测试 返回结果:{}", uName, response.getData());
				return YesOrNoStatus.YES.getCode();
			}

		} catch (Exception e) {
			log.error("all read by type error!:{}", e);
			return -1;
		}
	}

    /**
     * 单条已读
     * @param userId
     * @param courseWareId
     * @param courseType
     * @return
     * @throws BizException
     */
	public int readyOneCourseWork(int userId, long courseWareId, int courseType) throws BizException {
        CourseExercisesProcessLog courseExercisesProcessLog = new CourseExercisesProcessLog();
        courseExercisesProcessLog.setGmtModify(new Timestamp(System.currentTimeMillis()));
        courseExercisesProcessLog.setIsAlert(YesOrNoStatus.NO.getCode());

        Example example = new Example(CourseExercisesProcessLog.class);
        example.and().andEqualTo("userId", userId)
                .andEqualTo("courseType", courseType)
                .andEqualTo("lessonId", courseWareId)
                .andEqualTo("dataType", StudyTypeEnum.COURSE_WORK.getOrder());

		return	courseExercisesProcessLogMapper.updateByExampleSelective(courseExercisesProcessLog, example);
    }

    /**
     * 单条已读 - 大纲id
     * @param userId
     * @param syllabusId
     * @return
     * @throws BizException
     */
    public int readyOneCourseWorkBySyllabusId(int userId, long syllabusId) throws BizException {
        CourseExercisesProcessLog courseExercisesProcessLog = new CourseExercisesProcessLog();
        courseExercisesProcessLog.setGmtModify(new Timestamp(System.currentTimeMillis()));
        courseExercisesProcessLog.setIsAlert(YesOrNoStatus.NO.getCode());

        Example example = new Example(CourseExercisesProcessLog.class);
        example.and().andEqualTo("userId", userId)
                .andEqualTo("syllabusId", syllabusId)
                .andEqualTo("dataType", StudyTypeEnum.COURSE_WORK.getOrder());

        return	courseExercisesProcessLogMapper.updateByExampleSelective(courseExercisesProcessLog, example);
    }

    /**
     * 录播 & 回放处理进度
     * @param recordProcess
     * @throws BizException
     */
    public void dealRecordProcess(RecordProcess recordProcess) throws BizException{
        if(recordProcess.getUserId() == 0 || recordProcess.getSyllabusId() == 0){
            return;
        }
        Table<String, Long, SyllabusWareInfo> syllabusWareInfoTable = dealSyllabusInfo2Table(Sets.newHashSet(recordProcess.getSyllabusId()));
        SyllabusWareInfo syllabusWareInfo = syllabusWareInfoTable.get(LESSON_LABEL, recordProcess.getSyllabusId());
        if(null == syllabusWareInfo){
            return;
        }
        /**
         * 移动端数据上报，只处理录播的学习进度，回放不处理
         */
        if(syllabusWareInfo.getVideoType() == CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK.getVideoType()){
            return;
        }
        // 申论创建课后作业
        if(null != syllabusWareInfo.getSubjectType() && syllabusWareInfo.getSubjectType() == SubjectEnum.SL.getCode()){
            int courseType = CourseWareTypeEnum.changeVideoType2TableCourseType(syllabusWareInfo.getVideoType());
            essayExercisesAnswerMetaManager.createEssayInitUserMeta(recordProcess.getUserId(), recordProcess.getSyllabusId(), courseType, syllabusWareInfo.getCoursewareId(), syllabusWareInfo.getClassId());
        }else{
            this.createCourseWorkAnswerCardEntranceV2(syllabusWareInfo.getClassId(),
                    recordProcess.getSyllabusId(),
                    syllabusWareInfo.getVideoType(),
                    syllabusWareInfo.getCoursewareId(),
                    recordProcess.getSubject(),
                    recordProcess.getTerminal(),
                    recordProcess.getCv(),
                    recordProcess.getUserId());
        }
    }


    /**
     * 创建课后作业答题卡前置逻辑入口
     * @param courseId
     * @param syllabusId
     * @param courseType
     * @param coursewareId
     * @param subject
     * @param terminal
     * @param cv
     * @param userId
     * @return
     * @throws BizException
     */
    @Deprecated
    public synchronized Object createCourseWorkAnswerCardEntrance(long courseId, long syllabusId, int courseType, long coursewareId, int subject, int terminal, String cv, int userId) throws BizException{
        log.info("请求创建课后作业答题卡参数信息:courseType:{},courseId:{},syllabusId:{},coursewareId:{},terminal:{},cv:{},userId:{}",
                courseType, courseId, syllabusId, coursewareId, terminal, cv, userId);
        StopWatch stopwatch = new StopWatch("手动创建录播或直播回放课后作业答题卡");
        stopwatch.start();
        HashMap<String, Object> result;
        try{
            if(courseType == CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK.getVideoType()){
                SyllabusWareInfo syllabusWareInfo = dealSyllabusInfo2Info(syllabusId);
                if(null == syllabusWareInfo || StringUtils.isEmpty(syllabusWareInfo.getRoomId())){
                    log.error("直播回放创建课后作业答题卡失败，查询不到百家云信息:{}", syllabusId);
                    return null;
                }
                SimpleCourseLiveBackLog courseLiveBackLog = courseLiveBackLogService.findByRoomIdAndLiveCourseWareIdV2(Long.valueOf(syllabusWareInfo.getRoomId()), syllabusWareInfo.getCoursewareId());
                if(null == courseLiveBackLog || null == courseLiveBackLog.getLiveCourseWareId()){
                    log.error("直播回放数据查询不到roomId:{},课件id:{},终端信息:terminal:{},cv:{}",syllabusWareInfo.getRoomId(), syllabusWareInfo.getCoursewareId(),terminal, cv);
                    return null;
                }else{
                    coursewareId = courseLiveBackLog.getLiveCourseWareId();
                    courseType = CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType();
                }
            }

            List<Map<String, Object>> list = courseExercisesService.listQuestionByCourseId(courseType, coursewareId);
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            String questionId = list.stream()
                    .filter(map -> null != map && null != map.get("id"))
                    .map(map -> String.valueOf(map.get("id")))
                    .collect(Collectors.joining(","));
            Object practiceCard = practiceCardService.createCourseExercisesPracticeCard(
                    terminal, subject, userId, "课后作业练习",
                    courseType, coursewareId, questionId
            );
            result = (HashMap<String, Object>) ZTKResponseUtil.build(practiceCard);
            if (MapUtils.isEmpty(result)) {
                return null;
            }
            result.computeIfPresent("id", (key, value) -> String.valueOf(value));
            result.computeIfPresent("score", (key, value) -> new Double(Double.parseDouble(value.toString())).intValue());
            //取消调用
            //createCourseWorkAnswerCard(userId, courseType, coursewareId, courseId, syllabusId, result, true);
        }catch (Exception e){
            log.error("Exception:{}, terminal:{}, cv:{}", syllabusId, terminal, cv);
            return null;
        }
        log.info("课后作业 - 创建课后答题卡请求参数:courseId:{},syllabusId:{},courseType:{},coursewareId:{},userId:{}", courseId, syllabusId, courseType, coursewareId, userId);
        stopwatch.stop();
        log.info("手动创建录播或直播回放课后作业答题卡:{}", stopwatch.prettyPrint());
        return result;
    }

    /**
     * 创建课后作业答题卡前置逻辑入口V2
     * @param courseId
     * @param syllabusId
     * @param courseType
     * @param courseWareId
     * @param subject
     * @param terminal
     * @param cv
     * @param userId
     * @return
     * @throws BizException
     */
    public synchronized Object createCourseWorkAnswerCardEntranceV2(long courseId, long syllabusId, int courseType, long courseWareId, int subject, int terminal, String cv, int userId) throws BizException{
        log.info("请求创建课后作业答题卡参数信息V2 :courseType:{},courseId:{},syllabusId:{},coursewareId:{},terminal:{},cv:{},userId:{}",
                courseType, courseId, syllabusId, courseWareId, terminal, cv, userId);
        StopWatch stopwatch = new StopWatch("手动创建录播或直播回放课后作业答题卡");
        stopwatch.start();
        HashMap<String, Object> result;
        try{
            if(courseType == CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK.getVideoType()){
                SyllabusWareInfo syllabusWareInfo = dealSyllabusInfo2Info(syllabusId);
                if(null == syllabusWareInfo || StringUtils.isEmpty(syllabusWareInfo.getRoomId())){
                    log.error("直播回放创建课后作业答题卡失败，查询不到百家云信息V2:{}", syllabusId);
                    return null;
                }
                SimpleCourseLiveBackLog courseLiveBackLog = courseLiveBackLogService.findByRoomIdAndLiveCourseWareIdV2(Long.valueOf(syllabusWareInfo.getRoomId()), syllabusWareInfo.getCoursewareId());
                if(null == courseLiveBackLog || null == courseLiveBackLog.getLiveCourseWareId()){
                    log.error("直播回放数据查询不到roomId:{},课件id:{},终端信息V2:terminal:{},cv:{}",syllabusWareInfo.getRoomId(), syllabusWareInfo.getCoursewareId(),terminal, cv);
                    return null;
                }else{
                    courseWareId = courseLiveBackLog.getLiveCourseWareId();
                    courseType = CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType();
                }
            }

            List<Map<String, Object>> list = courseExercisesService.listQuestionByCourseId(courseType, courseWareId);
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            String questionId = list.stream()
                    .filter(map -> null != map && null != map.get("id"))
                    .map(map -> String.valueOf(map.get("id")))
                    .collect(Collectors.joining(","));
            Object practiceCard = practiceCardService.createCourseExercisesPracticeCard(
                    terminal, subject, userId, "课后作业练习",
                    courseType, courseWareId, questionId
            );
            result = (HashMap<String, Object>) ZTKResponseUtil.build(practiceCard);
            if (MapUtils.isEmpty(result)) {
                return null;
            }
            if(NetschoolTerminalType.needTrans2Str(terminal)){
                result.computeIfPresent("id", (key, value) -> String.valueOf(value));
            }
            result.computeIfPresent("score", (key, value) -> new Double(Double.parseDouble(value.toString())).intValue());

            createCourseWorkAnswerCard(userId, courseType, courseWareId, courseId, syllabusId, result, true);
        }catch (Exception e){
            log.error("Exception:{}, terminal:{}, cv:{}", syllabusId, terminal, cv);
            return null;
        }
        log.info("课后作业 - 创建课后答题卡请求参数:courseId:{},syllabusId:{},courseType:{},coursewareId:{},userId:{}", courseId, syllabusId, courseType, courseWareId, userId);
        stopwatch.stop();
        log.info("手动创建录播或直播回放课后作业答题卡:{}", stopwatch.prettyPrint());
        return result;
    }

    /**
     * 课后作业创建答题卡异步处理方法
     * @param courseType
     * @param coursewareId
     * @param courseId
     * @param syllabusId
     * @param result
     * @param isAlert
     * @param userId
     */
    public synchronized void createCourseWorkAnswerCard(int userId, Integer courseType, Long coursewareId, Long courseId, Long syllabusId, HashMap<String,Object> result, boolean isAlert){
        Long cardId = MapUtils.getLongValue(result, "id");
        int status = MapUtils.getIntValue(result, "status");

        if(null == syllabusId || syllabusId.longValue() == 0){
            throw new IllegalArgumentException("大纲id数据不对");
        }
        Example example = new Example(CourseExercisesProcessLog.class);
        example.and().andEqualTo("syllabusId", syllabusId)
                .andEqualTo("userId", userId)
                .andEqualTo("status", YesOrNoStatus.YES.getCode());

        try{
            CourseExercisesProcessLog courseExercisesProcessLog = courseExercisesProcessLogMapper.selectOneByExample(example);
            log.info("创建课后作业答题卡信息:{}",JSONObject.toJSONString(courseExercisesProcessLog));
            if(null == courseExercisesProcessLog){
                // 新增数据
                CourseExercisesProcessLog newLog = newLog(userId, isAlert);
                newLog.setCourseType(courseType);
                newLog.setSyllabusId(syllabusId);
                newLog.setUserId(Long.valueOf(userId));
                newLog.setCourseId(courseId);
                newLog.setLessonId(coursewareId);
                newLog.setCardId(cardId);
                newLog.setBizStatus(status);
                //课后作业插入日志
                log.debug("新的课后作业入库:{}", JSONObject.toJSONString(newLog));
                courseExercisesProcessLogMapper.insertSelective(newLog);
                //putIntoDealList(syllabusId);
            }else{
                // 更新答题卡字段
                courseExercisesProcessLog.setGmtModify(new Timestamp(System.currentTimeMillis()));
                courseExercisesProcessLog.setBizStatus(status);
                courseExercisesProcessLogMapper.updateByPrimaryKeySelective(courseExercisesProcessLog);
            }
        }catch (Exception e){
            log.error("课后作业创建答题卡异步处理方法失败, userId:{},courseType:{},coursewareId:{},courseId:{},syllabusId:{},result:{},error:{}", userId, courseType, coursewareId, courseId, syllabusId, result, e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 待处理的大纲id
     * @param syllabusId
     */
    @Deprecated
    public void putIntoDealList(Long syllabusId){
        if(syllabusId.longValue() == 0){
            return;
        }
        String key = CourseCacheKey.getProcessLogSyllabusDealList();
        ZSetOperations<String, String> dealList = redisTemplate.opsForZSet();
        dealList.add(key, String.valueOf(syllabusId), System.currentTimeMillis());
    }

    /**
     * 每20秒处理一次
     * @throws BizException
     */
    //@Scheduled(fixedRate = PERIOD_TIME)
    public synchronized void dealList() throws BizException{
        String key = CourseCacheKey.getProcessLogSyllabusDealList();
        ZSetOperations<String, String> dealList = redisTemplate.opsForZSet();
        long max = System.currentTimeMillis();
        Set<Long> data = dealList.rangeByScore(key, 0, max).stream().map(Long::valueOf).collect(Collectors.toSet());
        List<Long> syllabusIds = Lists.newArrayList(data);
        syllabusIds = syllabusIds.subList(0, 20);
        if(CollectionUtils.isEmpty(syllabusIds)){
            return;
        }
        log.debug("deal syllabusInfo list:{}", syllabusIds);
        Set<Long> filter = Sets.newHashSet();
        for (Long syllabusId : syllabusIds) {
            if(syllabusId == 0){
                filter.add(syllabusId);
            }
        }
        if(CollectionUtils.isNotEmpty(filter)){
            syllabusIds.removeAll(filter);
            filter.forEach(item-> dealList.remove(key, String.valueOf(item)));
        }
        if(CollectionUtils.isEmpty(syllabusIds)){
            return;
        }
        List<SyllabusWareInfo> list = requestSyllabusWareInfoFromPhpAndPutIntoCache(Sets.newHashSet(syllabusIds));
        list.forEach(item -> dealList.remove(key, String.valueOf(item)));
    }

    /**
     * 保存课后练习答题卡，更新状态
     * @param answerCard
     * @throws BizException
     */
    public void submitCourseWorkAnswerCard(final PracticeCard answerCard)throws BizException{
        courseExercisesStatisticsManager.dealCourseExercisesStatistics(answerCard);
        //更新 mysql 答题卡数据状态
        Example example = new Example(CourseExercisesProcessLog.class);
        example.and()
                .andEqualTo("status", YesOrNoStatus.YES.getCode())
                .andEqualTo("userId", answerCard.getUserId())
                .andEqualTo("cardId", answerCard.getId());
        /**
         * 学员买了多个课，课后作业会有多个数据
         */
        List<CourseExercisesProcessLog> courseExercisesProcessLogs = courseExercisesProcessLogMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(courseExercisesProcessLogs)){
            for (CourseExercisesProcessLog courseExercisesProcessLog : courseExercisesProcessLogs) {
                courseExercisesProcessLog.setGmtModify(new Timestamp(System.currentTimeMillis()));
                courseExercisesProcessLog.setBizStatus(answerCard.getStatus());
                courseExercisesProcessLogMapper.updateByPrimaryKeySelective(courseExercisesProcessLog);
            }
        }else{
            log.error("队列提交课后作业数据查询为空:{}", JSONObject.toJSONString(answerCard));
        }
    }
    /**
     * new courseExercisesProcessLog
     * @return
     */
    private static CourseExercisesProcessLog newLog(int userId, boolean isAlert){
        CourseExercisesProcessLog courseExercisesProcessLog = new CourseExercisesProcessLog();
        courseExercisesProcessLog.setIsAlert(isAlert ? YesOrNoStatus.YES.getCode() : YesOrNoStatus.NO.getCode());
        courseExercisesProcessLog.setGmtModify(new Timestamp(System.currentTimeMillis()));
        courseExercisesProcessLog.setGmtCreate(new Timestamp(System.currentTimeMillis()));
        courseExercisesProcessLog.setDataType(StudyTypeEnum.COURSE_WORK.getOrder());
        courseExercisesProcessLog.setStatus(YesOrNoStatus.YES.getCode());
        courseExercisesProcessLog.setCreatorId(isAlert ? 0L : userId);
        courseExercisesProcessLog.setModifierId(isAlert ? 0L : userId);
        return courseExercisesProcessLog;
    }

    /**
     * 获取我的课后作业列表
     * @param userId
     * @param page
     * @param size
     * @return
     * @throws BizException
     */
    public Object courseWorkList(long userId, int page, int size) throws BizException{

        List<CourseWorkCourseVo> courseWorkCourseVos = Lists.newArrayList();
        Map<Long, AnswerCardInfo> answerCardMaps = Maps.newHashMap();
        List<HashMap<String, Object>> dataList = courseExercisesProcessLogMapper.getCoursePageInfo(userId, page, size);
        log.info("查询数据库获取用户未完成课后练习数据列表: keySet:{}", dataList);
        Set<Long> allSyllabusIds = Sets.newHashSet();
        dataList.forEach(item -> {
            String ids = String.valueOf(item.get("syllabusIds"));
            Set<Long> temp = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toSet());
            allSyllabusIds.addAll(temp);
        });
        Table<String, Long, SyllabusWareInfo> syllabusWareInfoTable = this.dealSyllabusInfo2Table(allSyllabusIds);
        if(syllabusWareInfoTable.isEmpty()){
            return courseWorkCourseVos;
        }

        Example example = new Example(CourseExercisesProcessLog.class);
        example.and()
                .andEqualTo("userId", userId)
                .andEqualTo("dataType", StudyTypeEnum.COURSE_WORK.getOrder())
                .andEqualTo("status", YesOrNoStatus.YES.getCode())
                .andIn("syllabusId", allSyllabusIds);
        try{
            List<CourseExercisesProcessLog> logList = courseExercisesProcessLogMapper.selectByExample(example);
            String cardIds = logList.stream().map(CourseExercisesProcessLog::getCardId).distinct().map(String::valueOf).collect(Collectors.joining(","));
            if(StringUtils.isNotBlank(cardIds)){
                Object practiceCardInfos = practiceCardService.getCourseExercisesCardInfoBatch(cardIds);
                List<HashMap<String, Object>> answerCardInfo = (List<HashMap<String, Object>>) ZTKResponseUtil.build(practiceCardInfos);
                answerCardMaps.putAll(answerCardInfo.stream().collect(Collectors.toMap(item -> MapUtils.getLong(item,"id"), item -> {
                    AnswerCardInfo dataInfo = new AnswerCardInfo();
                    try{
                        org.apache.commons.beanutils.BeanUtils.populate(dataInfo, item);
                        return dataInfo;
                    }catch (Exception e) {
                        log.error("答题卡信息转换异常:{}", e);
                        return dataInfo;
                    }
                })));
            }

            Map<Long, CourseExercisesProcessLog> courseExercisesProcessLogMap = Maps.newHashMap();
            for (CourseExercisesProcessLog courseExercisesProcessLog : logList) {
                courseExercisesProcessLogMap.put(courseExercisesProcessLog.getSyllabusId(), courseExercisesProcessLog);
            }

            dataList.forEach(item->{
                CourseWorkCourseVo courseWorkCourseVo = new CourseWorkCourseVo();
                long courseId = MapUtils.getLong(item, "courseId");
                courseWorkCourseVo.setCourseId(courseId);
                SyllabusWareInfo courseInfo = syllabusWareInfoTable.get(COURSE_LABEL, courseWorkCourseVo.getCourseId());
                if(null == courseInfo){
                    courseWorkCourseVo.setCourseTitle(StringUtils.EMPTY);
                    log.error("根据大纲id获取大纲信息异常:课程id & 大纲 ids: {}", item);
                }else{
                    courseWorkCourseVo.setCourseTitle(courseInfo.getClassName());
                }
                String ids = String.valueOf(item.get("syllabusIds"));
                Set<Long> temp = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toSet());
                if(CollectionUtils.isEmpty(temp)){
                    courseWorkCourseVo.setUndoCount(0);
                    courseWorkCourseVo.setWareInfoList(Lists.newArrayList());
                    courseWorkCourseVo.setCourseTitle(StringUtils.EMPTY);
                }

                List<CourseWorkWareVo> wareVos = Arrays.stream(ids.split(","))
                        .filter(ware -> {
                            boolean result = true;
                            result = result && (null != syllabusWareInfoTable.get(LESSON_LABEL, Long.valueOf(ware)));
                            result = result && (null !=  courseExercisesProcessLogMap.get(Long.valueOf(ware)));
                            return result;
                        }).map(ware -> {
                            Long syllabusId = Long.valueOf(ware);
                            SyllabusWareInfo syllabusWareInfo = syllabusWareInfoTable.get(LESSON_LABEL, syllabusId);
                            CourseExercisesProcessLog courseExercisesProcessLog = courseExercisesProcessLogMap.get(Long.valueOf(ware));
                            CourseWorkWareVo courseWorkWareVo = new CourseWorkWareVo();

                            courseWorkWareVo.setSyllabusId(syllabusId);
                            courseWorkWareVo.setCourseWareTitle(syllabusWareInfo.getCoursewareName());
                            courseWorkWareVo.setVideoLength(syllabusWareInfo.getLength());
                            courseWorkWareVo.setSerialNumber(syllabusWareInfo.getSerialNumber());
                            courseWorkWareVo.setAnswerCardId(courseExercisesProcessLog.getCardId());
                            if(syllabusWareInfo.getVideoType() == CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK.getVideoType()){
                                courseWorkWareVo.setCourseWareId(courseExercisesProcessLog.getLessonId());
                                courseWorkWareVo.setVideoType(courseExercisesProcessLog.getCourseType());
                            }else{
                                courseWorkWareVo.setCourseWareId(syllabusWareInfo.getCoursewareId());
                                courseWorkWareVo.setVideoType(syllabusWareInfo.getVideoType());
                            }
                            courseWorkWareVo.setQuestionIds("");
                            courseWorkWareVo.setIsAlert(courseExercisesProcessLog.getIsAlert());
                            if(answerCardMaps.containsKey(courseExercisesProcessLog.getCardId())){
                                courseWorkWareVo.setAnswerCardInfo(answerCardMaps.get(courseExercisesProcessLog.getCardId()));
                            }else{
                                courseWorkWareVo.setAnswerCardInfo(new AnswerCardInfo());
                            }
                            return courseWorkWareVo;
                        }).collect(Collectors.toList());


                courseWorkCourseVo.setUndoCount(temp.size());
                courseWorkCourseVo.setWareInfoList(wareVos);
                courseWorkCourseVos.add(courseWorkCourseVo);
            });
        }catch (Exception e){
            log.error("获取课后练习列表异常:{},{}",userId, e);
            return courseWorkCourseVos;
        }
        return courseWorkCourseVos;
    }

    /**
     * 处理大纲信息返回 table 走缓存
     * @param syllabusIds
     * @return
     * @throws BizException
     */
    public Table<String, Long, SyllabusWareInfo> dealSyllabusInfo2Table(Set<Long> syllabusIds) throws BizException{
        log.debug("deal syllabusId info:{}", syllabusIds);
        Table<String, Long, SyllabusWareInfo> table = TreeBasedTable.create();
        if(CollectionUtils.isEmpty(syllabusIds)){
            return table;
        }
        Set<Long> copy = Sets.newHashSet();
        copy.addAll(syllabusIds);
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        syllabusIds.forEach(item -> {
            if(null == item || item.longValue() == 0){
                return;
            }
            String key = CourseCacheKey.getProcessLogSyllabusInfo(item);
            if(redisTemplate.hasKey(key)){
                try{
                    String value = valueOperations.get(key);
                    SyllabusWareInfo syllabusWareInfo = JSONObject.parseObject(value, SyllabusWareInfo.class);
                    if(SyllabusWareInfo.cacheCheck(syllabusWareInfo)){
                        table.put(LESSON_LABEL, item, syllabusWareInfo);
                        table.put(COURSE_LABEL, syllabusWareInfo.getClassId(), syllabusWareInfo);
                        copy.remove(item);
                    }else{
                        new Thread(() -> requestSyllabusWareInfoFromPhpAndPutIntoCache(item)).start();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("dealSyllabusInfo.2.table key:{}, errorMsg:{}", key, e.getMessage());
                }
            }
        });
        if(CollectionUtils.isNotEmpty(copy)){
            table.putAll(requestMultiSyllabusInfoAndReturnTable(copy));
        }
        return table;
    }

    /**
     * 处理大纲信息返回 info 信息 走缓存
     * @param syllabusId
     * @return
     * @throws BizException
     */
     public SyllabusWareInfo dealSyllabusInfo2Info(long syllabusId) throws BizException{
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        String key = CourseCacheKey.getProcessLogSyllabusInfo(syllabusId);
        if(redisTemplate.hasKey(key)){
            String value = valueOperations.get(key);
            try{
                SyllabusWareInfo syllabusWareInfo = JSONObject.parseObject(value, SyllabusWareInfo.class);
                if(SyllabusWareInfo.cacheCheck(syllabusWareInfo)){
                    return syllabusWareInfo;
                }else{
                    redisTemplate.delete(key);
                }
            }catch (Exception e){
                log.error("deal syllabus 2 info error, key:{},error:{}", syllabusId, e.getMessage());
            }
        }
         return requestSyllabusWareInfoFromPhpAndPutIntoCache(syllabusId);
    }



    /**
     * 使用多个大纲id获取大纲详细信息并返回 table 信息
     * @param syllabusIds
     * @return
     * @throws BizException
     */
    private Table<String, Long, SyllabusWareInfo> requestMultiSyllabusInfoAndReturnTable(Set<Long> syllabusIds)throws BizException{
        StopWatch stopwatch = new StopWatch("requestSyllabusWareInfoPut2Cache");
        stopwatch.start();
        Table<String, Long, SyllabusWareInfo> table = TreeBasedTable.create();
        List<SyllabusWareInfo> list = requestSyllabusWareInfoFromPhpAndPutIntoCache(syllabusIds);
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(item -> {
                table.put(LESSON_LABEL, item.getSyllabusId(), item);
                table.put(COURSE_LABEL, item.getClassId(), item);
            });
        }
        return table;
    }

    /**
     * 远程调用 php 获取大纲信息并放入缓存
     * @param syllabusId
     * @return
     */
    private SyllabusWareInfo requestSyllabusWareInfoFromPhpAndPutIntoCache(long syllabusId){
        Set<Long> data = Sets.newHashSet(syllabusId);
        List<SyllabusWareInfo> list = requestSyllabusWareInfoFromPhpAndPutIntoCache(data);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 去请求 php 不在缓存中的大纲，并放入缓存
     * @param syllabusIds
     * @return
     */
    private List<SyllabusWareInfo> requestSyllabusWareInfoFromPhpAndPutIntoCache(Set<Long> syllabusIds){
        List<SyllabusWareInfo> list = Lists.newArrayList();
        if(CollectionUtils.isEmpty(syllabusIds)){
            return list;
        }
        try{
            String ids = Joiner.on(",").join(syllabusIds);
            HashMap<String, Object> params = HashMapBuilder.<String,Object>newBuilder().put("syllabusIds", ids).build();
            NetSchoolResponse<LinkedHashMap<String, Object>> netSchoolResponse = syllabusService.courseWareInfo(params);
            if(ResponseUtil.isFailure(netSchoolResponse) || netSchoolResponse == NetSchoolResponse.DEFAULT){
                log.error("请求php批量获取大纲信息异常:{}", syllabusIds);
                return list;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            List<LinkedHashMap<String, Object>> data = (List<LinkedHashMap<String, Object>>)netSchoolResponse.getData();

            List<SyllabusWareInfo> dataList = data.stream().map(item -> {
                LinkedHashMap<String, Object> map = item;
                try{
                    return objectMapper.convertValue(map, SyllabusWareInfo.class);
                }catch (Exception e){
                    log.error("convert map 2 SyllabusWareInfo keu:{},error! {}", map, e.getMessage());
                    return null;
                }
            }).collect(Collectors.toList());

            if(CollectionUtils.isNotEmpty(dataList)){
                ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
                list.forEach(item -> {
                    String key = CourseCacheKey.getProcessLogSyllabusInfo(item.getSyllabusId());
                    valueOperations.set(key, JSONObject.toJSONString(item));
                    redisTemplate.expire(key, 20, TimeUnit.MINUTES);
                });
                list.addAll(dataList);
            }
            return list;
        }catch (Exception e){
            return list;
        }
    }


    /**
     * 直播数据上报处理，非直播不处理
     * @param subject
     * @param terminal
     * @param userId
     * @param syllabusWareInfo
     * @param cv
     */
    public void createCivilInitUserMeta(int userId, int subject, int terminal, SyllabusWareInfo syllabusWareInfo, String cv) {
        if(syllabusWareInfo.getAfterCoreseNum() == 0){
            return;
        }
        if(CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType() != syllabusWareInfo.getVideoType()){
            return;
        }
        log.info("直播创建或更新课后作业答题卡:大纲id{}", syllabusWareInfo.getSyllabusId());
        createCourseWorkAnswerCardEntranceV2(syllabusWareInfo.getClassId(), syllabusWareInfo.getSyllabusId(), syllabusWareInfo.getVideoType(), syllabusWareInfo.getCoursewareId(), subject, terminal, cv, userId);
    }

    /**
     *
     * @param userId
     * @param secret
     * @return
     */
    public void dataCorrect(int userId, String secret){
        List<Integer> list = Lists.newArrayList(AnswerCardStatus.CREATE, AnswerCardStatus.UNDONE);
        Example example = new Example(CourseExercisesProcessLog.class);
        example.and().andEqualTo("status", YesOrNoStatus.YES.getCode())
                .andNotEqualTo("modifierId", userId)
                .andIn("bizStatus", list);
        List<CourseExercisesProcessLog> courseExercisesProcessLogs = courseExercisesProcessLogMapper.selectByExample(example);
        log.info("dataCorrect:{}", courseExercisesProcessLogs.size());
        for (CourseExercisesProcessLog courseExercisesProcessLog : courseExercisesProcessLogs) {
            String message = userId + "_" + courseExercisesProcessLog.getId();
            rabbitTemplate.convertAndSend("", RabbitMqConstants.COURSE_EXERCISES_PROCESS_LOG_CORRECT_QUEUE, message);
        }
    }

    /**
     * 数据纠正开关
     * @param userId
     * @param str
     */
    public void dataCorrectSwitch(int userId, String str){
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        if(str.equals(CORRECT_DATA_SWITCH_ON)){
            valueOperations.set(CORRECT_DATA_SWITCH, CORRECT_DATA_SWITCH_ON);
        }else{
            valueOperations.set(CORRECT_DATA_SWITCH, String.valueOf(userId).concat(CORRECT_DATA_SWITCH_ON));
        }

    }

    /**
     * 数据处理
     * @param message
     */
    public void correct(String message){
        log.info(">>>>>>>>> current deal message:{}", message);
        ValueOperations<String, String> keySwitch = redisTemplate.opsForValue();
        SetOperations<String, String> keyExist = redisTemplate.opsForSet();
        String [] data = message.split("_");
        long id = Long.valueOf(data[1]);
        long userId = Long.valueOf(data[0]);
        try{
            int courseType;
            long lessonId;
            CourseExercisesProcessLog courseExercisesProcessLog = courseExercisesProcessLogMapper.selectByPrimaryKey(id);
            SyllabusWareInfo syllabusWareInfo = dealSyllabusInfo2Info(courseExercisesProcessLog.getSyllabusId());
            if(null == syllabusWareInfo){
                return;
            }

            /**
             * 如果为直播回放，获取直播信息，处理
             */
            if(syllabusWareInfo.getVideoType() == CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK.getVideoType()){
                String roomId = syllabusWareInfo.getRoomId();
                SimpleCourseLiveBackLog courseLiveBackLog = courseLiveBackLogService.findByRoomIdAndLiveCourseWareIdV2(Long.valueOf(roomId), syllabusWareInfo.getCoursewareId());
                if(null == courseLiveBackLog || null == courseLiveBackLog.getLiveCourseWareId()){
                    return;
                }
                courseType = CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType();
                lessonId = courseLiveBackLog.getLiveCourseWareId();
            }else{
                courseType = syllabusWareInfo.getVideoType();
                lessonId = syllabusWareInfo.getCoursewareId();
            }
            StringBuffer stringBuffer = new StringBuffer(String.valueOf(userId)).append(String.valueOf(courseExercisesProcessLog.getSyllabusId()));
            if(lessonId != courseExercisesProcessLog.getLessonId() || courseType != courseExercisesProcessLog.getCourseType()){
                if(redisTemplate.hasKey(CORRECT_DATA_SWITCH) && keySwitch.get(CORRECT_DATA_SWITCH).equals(CORRECT_DATA_SWITCH_ON)){
                    keyExist.remove(CORRECT_DATA_KEY, stringBuffer.toString());
                    courseExercisesProcessLog.setCourseType(courseType);
                    courseExercisesProcessLog.setLessonId(lessonId);
                    courseExercisesProcessLog.setModifierId(userId);
                    int execute = courseExercisesProcessLogMapper.updateByPrimaryKeySelective(courseExercisesProcessLog);
                    if(execute > 0){
                        log.info("成功更新数据:,课件:{},类型:{}, 大纲数据:课件:{}, 类型:{}", courseExercisesProcessLog.getLessonId(), courseExercisesProcessLog.getCourseType(), lessonId, courseType);
                    }
                }else{
                    keyExist.add(CORRECT_DATA_KEY, stringBuffer.toString());
                    log.info("暂存缓存数据:,课件:{},类型:{}, 大纲数据:课件:{}, 类型:{}", courseExercisesProcessLog.getLessonId(), courseExercisesProcessLog.getCourseType(),lessonId,courseType);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("修正课后作业数据失败:数据id:{},{}",id, e);
        }
    }


    /**
     * 阶段测试单条已读
     * @param syllabusId
     * @param courseId
     * @param uname
     * @return
     */
	public void readyOnePeriod(Long syllabusId, Long courseId, String uname) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("syllabusId", syllabusId);
		params.put("netClassId", courseId);
		params.put("userName", uname);
		params.put("type", 0);
		NetSchoolResponse response = userCourseServiceV6.readPeriod(params);
		log.info("用户{}已读阶段测试大纲id为{} 返回结果:{}", uname, syllabusId, response.getData());
	}

    /**
     * 数据迁移处理逻辑
     * @param message
     */
    @Deprecated
	public void dealCourseWorkReportUsers(String message){
	    int userId = Integer.parseInt(message);
	    String userIdStr = String.valueOf(userId);
	    String alreadyProcessed = CourseCacheKey.COURSE_WORK_REPORT_USERS_ALREADY_PROCESSED;
	    SetOperations<String, String> alreadyProcessedOperations = redisTemplate.opsForSet();
        //如果处理完队列中存在 userId -> 不做处理返回
        Object courseExercisesCardInfo = practiceCardService.getCourseExercisesAllCardInfo(userId);
        if(courseExercisesCardInfo == ResponseUtil.DEFAULT_PAGE_EMPTY){
            log.error("obtain current user's course works failed! userId:{}", userId);
        }else{
            try{
                Object build = ZTKResponseUtil.build(courseExercisesCardInfo);
                List<Map> courseExercisesCards = (List<Map>) build;
                if(CollectionUtils.isEmpty(courseExercisesCards)){
                    log.info("current user's course work size is 0 :userId:{}", userIdStr);
                }else{
                    synchronized (this){
                        dealCourseExercisesCards(userId, courseExercisesCards);
                        //处理完毕加入已经处理 set 中
                        alreadyProcessedOperations.add(alreadyProcessed, userIdStr);
                    }
                }
            }catch (Exception e){
                alreadyProcessedOperations.remove(alreadyProcessed, userIdStr);
                log.error("deal course exercises caught an exception:{}", e);
            }
        }
    }

    /**
     * 查询数据库校验答题卡是否存在
     * @param userId
     * @param hashMapImmutableList
     * @param longImmutableList
     */
    public void analyzeCardIdExist(int userId, ImmutableList<HashMap<String, Object>> hashMapImmutableList, ImmutableList<Long> longImmutableList){
        List<HashMap<String,Object>> fixList = Lists.newArrayList();
        for(HashMap<String, Object> map : hashMapImmutableList){

            int courseType = MapUtils.getInteger(map, SyllabusInfo.VideoType, 0);
            long lessonId = MapUtils.getInteger(map, SyllabusInfo.CourseId, 0);

            try{
                Example example = new Example(CourseExercisesProcessLog.class);
                example.and()
                        .andEqualTo("userId", userId)
                        .andEqualTo("lessonId", lessonId)
                        .andEqualTo("courseType", courseType)
                        .andEqualTo("status", YesOrNoStatus.YES.getCode());
                CourseExercisesProcessLog log = courseExercisesProcessLogMapper.selectOneByExample(example);
                if(null == log || !(longImmutableList.contains(log.getCardId()))){
                    fixList.add(map);
                }
            }catch (Exception e){
                log.error("courseExercisesProcessLogMapper selectOneByExample error,userId = {}, courseType = {}, lessonId = {}, error = {}", userId, courseType, lessonId, e.getMessage());
            }
        }
        if(CollectionUtils.isNotEmpty(fixList)){
            requestCourseWorkInfo4Mysql(userId, fixList);
        }
    }

    /**
     * 请求 paper 服务刷新数据进入 mysql
     * @param userId
     * @param fixList
     */
    private void requestCourseWorkInfo4Mysql(int userId, List<HashMap<String,Object>> fixList){
        log.error("课后作业数据修正 - 需要请求paper并放入mysql的数据  userId = {}, paramList = {}", userId, fixList);
        Object practiceCardInfos = practiceCardService.getCourseExercisesCardInfo(userId, fixList);
        List<Map> answerCardInfo = (List<Map>) ZTKResponseUtil.build(practiceCardInfos);
        if(CollectionUtils.isNotEmpty(answerCardInfo)){
            log.info("requestCourseWorkInfo4Mysql ===》 mysql, userId = {}, answerCardInfos = {}", userId, JSONObject.toJSONString(answerCardInfo));
            dealCourseExercisesCards(userId, answerCardInfo);
        }
    }


    /**
     * 遍历获取到的数据
     * @param courseExercisesCards
     */
    @Deprecated
    private void dealCourseExercisesCards(int userId, List<Map> courseExercisesCards){
	    for(Map<String,Object> current : courseExercisesCards){
	        try{
                int courseType = MapUtils.getInteger(current, "courseType");
                Long courseWareId = MapUtils.getLong(current, "courseId");
                Long cardId = MapUtils.getLong(current, "id");
                int status = MapUtils.getInteger(current, "status");
                Map<String,Object> params = Maps.newHashMap();
                params.put("coursewareId", courseWareId);
                params.put("coursewareType", courseType);
                NetSchoolResponse netSchoolResponse = syllabusService.obtainSyllabusIdByCourseWareId(params);
                if(netSchoolResponse != NetSchoolResponse.DEFAULT){
                    List<Map<String,Object>> syllabusDataInfo = (List<Map<String,Object>>)netSchoolResponse.getData();
                    if(CollectionUtils.isNotEmpty(syllabusDataInfo)){
                        //处理入库 mysql
                        dealCourseWorkDataIntoMySQL(userId, courseType, cardId, status, courseWareId, syllabusDataInfo);
                    }
                }
            }catch (Exception e){
	            log.error("dealCourseExercisesCards caught an error:{}", e.getMessage());
	            throw new IllegalArgumentException("遍历数据请求 php 并入库异常");
            }
        }
    }

    /**
     * 根据 courseType && courseId 获取课后作业信息
     * @param userId
     * @param courseType
     * @param courseWareId
     * @return
     */
    public Optional<CourseExercisesProcessLog> getCourseExercisesProcessLogByTypeAndWareId(long userId, int courseType, long courseWareId) {
        Example example = new Example(CourseExercisesProcessLog.class);
        example.and()
                .andEqualTo("userId", userId)
                .andEqualTo("lessonId", courseWareId)
                .andEqualTo("courseType", courseType)
                .andEqualTo("status", YesOrNoStatus.YES.getCode());

        example.orderBy("gmtCreate").desc();
        List<CourseExercisesProcessLog> list = courseExercisesProcessLogMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(list)){
            return Optional.of(list.get(0));
        }else{
            return Optional.ofNullable(null);
        }
    }

    /**
     * 处理数据入库 mySql
     * @param courseType
     * @param courseWareId
     * @param syllabusDataInfo
     */
    @Deprecated
    private void dealCourseWorkDataIntoMySQL(int userId, int courseType, Long cardId, int status, Long courseWareId, List<Map<String,Object>> syllabusDataInfo){
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("id", cardId);
        params.put("status", status);
        for(Map<String, Object> current : syllabusDataInfo){
            try{
                Long courseId = MapUtils.getLong(current, "classId");
                Long syllabusId = MapUtils.getLong(current, "syllabusId");
                //取消调用
                //createCourseWorkAnswerCard(userId, courseType, courseWareId, courseId, syllabusId, params,false);
                log.debug("处理课后作业入库----> userId:{},courseType:{},courseWareId:{},courseId:{},syllabusId:{},params:{}", userId, courseType, courseWareId, courseId, syllabusId, params);
            }catch (Exception e){
                log.error("处理课后作业入库 mysql 异常 userId:{}, courseType:{}, cardId:{}, status:{}, courseWareId:{},syllabusDataInfo:{}, error:{}", userId, courseType, cardId, status, courseWareId, syllabusDataInfo,e.getMessage());
                throw new IllegalArgumentException("处理课后作业入库 mysql 异常" + userId);
            }
        }
    }

    /**
     * 通过courseType或者lessonId获取答题卡id
     * @param userId
     * @param paramsList
     * @return
     */
    public List<Long> obtainCardIdsByCourseTypeAndLessonId(long userId, List<HashMap<String, Object>> paramsList){

        List<Long> cardIds = Lists.newArrayList();
        for(Map item : paramsList){
            int courseType = MapUtils.getIntValue(item, SyllabusInfo.VideoType);
            int lessonId = MapUtils.getIntValue(item, SyllabusInfo.CourseId);
            try{
                Example example = new Example(CourseExercisesProcessLog.class);
                example.and()
                        .andEqualTo("userId", userId)
                        .andEqualTo("status", YesOrNoStatus.YES.getCode())
                        .andEqualTo("courseType", courseType)
                        .andEqualTo("lessonId", lessonId);

                CourseExercisesProcessLog log = courseExercisesProcessLogMapper.selectOneByExample(example);
                cardIds.add(log.getCardId());
            }catch (Exception e){
                log.error("obtainCardIdsByCourseTypeAndLessonId error!: userId = {}, paramsList = {}, error = {}", userId, paramsList, e.getMessage());
            }
        }
        log.info("obtainCardIdsByCourseTypeAndLessonId: cardId = {}", cardIds);
        return cardIds;
    }


    /**
     * 通过courseType或者lessonId获取答题卡id
     * @param userId
     * @param syllabusIds
     * @return
     */
    public List<Long> obtainCardIdsBySyllabusIds(long userId, List<Long> syllabusIds){
        try{
            if(userId == 0){
               return Lists.newArrayList();
            }
            Example example = new Example(CourseExercisesProcessLog.class);
            example.and()
                    .andEqualTo("userId", userId)
                    .andEqualTo("status", YesOrNoStatus.YES.getCode())
                    .andIn("syllabusId", syllabusIds);

            List<CourseExercisesProcessLog> logList = courseExercisesProcessLogMapper.selectByExample(example);
            if(CollectionUtils.isNotEmpty(logList)){
                return logList.stream().map(CourseExercisesProcessLog::getCardId).collect(Collectors.toList());
            }else{
                return Lists.newArrayList();
            }
        }catch (Exception e){
            log.error("obtainCardIdsBySyllabusIds 异常: userId:{}, syllabusIds:{}", userId, syllabusIds);
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }


    @Deprecated
    public void dataFix(long modifierId){
        List<HashMap<String, Object>> dataList = courseExercisesProcessLogMapper.getDuplicateDate();
        log.info("查询错误数据..............: dataList:{}", dataList);
        List<DataFix> dataFixes = Lists.newArrayList();
        dataList.forEach(item -> {
            String ids = String.valueOf(item.get("ids"));
            String userId = String.valueOf(item.get("user_id"));
            List<Long> ids_ = Arrays.stream(ids.split(",")).map(Long::valueOf).distinct().collect(Collectors.toList());
            DataFix dataFix = DataFix.builder().userId(Long.valueOf(userId)).ids(ids_).build();
            dataFixes.add(dataFix);
        });
        if(CollectionUtils.isEmpty(dataFixes)){
            return;
        }
        for (DataFix dataFix : dataFixes) {
            if(dataFix.getIds().size() > 1){
                log.error("current data fix is too many, skip...");
            }
            List<Long> syllabusIds = dataFix.getIds();
            Example example = new Example(CourseExercisesProcessLog.class);
            example.and().andEqualTo("status", YesOrNoStatus.YES.getCode())
                    .andEqualTo("userId", dataFix.getUserId())
                    .andEqualTo("syllabusId", syllabusIds.get(0));
            example.orderBy("bizStatus").asc();

            List<CourseExercisesProcessLog> courseExercisesProcessLogs = courseExercisesProcessLogMapper.selectByExample(example);
            if(courseExercisesProcessLogs.size() <= 1){
                log.error("data is normal");
                continue;
            }
            CourseExercisesProcessLog tmp = courseExercisesProcessLogs.get(0);
            log.debug("课后作业当前需要修正的数据:{}", JSONObject.toJSONString(tmp));
            CourseExercisesProcessLog update = new CourseExercisesProcessLog();
            update.setId(tmp.getId());
            update.setGmtModify(new Timestamp(System.currentTimeMillis()));
            update.setStatus(YesOrNoStatus.NO.getCode());
            update.setModifierId(modifierId);
            courseExercisesProcessLogMapper.updateByPrimaryKeySelective(update);
        }
    }

    /**
     * 行测课后作业未读数
     * @param userId
     * @return
     */
    public int obtainCivilCourseWorkUnReadCount(long userId){
        List<Integer> list = Lists.newArrayList(AnswerCardStatus.CREATE, AnswerCardStatus.UNDONE);
        Example example = new Example(CourseExercisesProcessLog.class);
        example.and().andEqualTo("userId", userId)
                .andEqualTo("status", YesOrNoStatus.YES.getCode())
                .andEqualTo("isAlert",YesOrNoStatus.YES.getCode())
                .andIn("bizStatus", list);

        return courseExercisesProcessLogMapper.selectCountByExample(example);
    }

    /**
     * 课后作业是否完成 0 为完成 1 已完成
     * @param userId
     * @param syllabusId
     * @return
     * @throws BizException
     */
    public Object cardInfo(int userId, long syllabusId) throws BizException{
        Map<String, Object> result = Maps.newHashMap();
        int status = YesOrNoStatus.NO.getCode();
        long cardId = 0l;
        Example example = new Example(CourseExercisesProcessLog.class);
        example.and().andEqualTo("userId", userId)
                .andEqualTo("syllabusId",syllabusId)
                .andEqualTo("status", YesOrNoStatus.YES.getCode());
        List<CourseExercisesProcessLog> list = courseExercisesProcessLogMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(list)){
            CourseExercisesProcessLog courseExercisesProcessLog = list.get(0);
            status = courseExercisesProcessLog.getBizStatus() == AnswerCardStatus.FINISH ? YesOrNoStatus.YES.getCode() : YesOrNoStatus.NO.getCode();
            cardId = courseExercisesProcessLog.getCardId() == null ? cardId : courseExercisesProcessLog.getCardId();
        }
        result.put("status", status);
        result.put("cardId", String.valueOf(cardId));
        return result;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    static class DataFix implements Serializable {
        private Long userId;
        private List<Long> ids;

        @Builder
        public DataFix(Long userId, List<Long> ids) {
            this.userId = userId;
            this.ids = ids;
        }
    }


    @NoArgsConstructor
    @Getter
    @Setter
    public static class UserInfo{
        private Integer userId;
        private Long lessonId;
        private Integer courseType;

        @Builder
        public UserInfo(Integer userId, Long lessonId, Integer courseType) {
            this.userId = userId;
            this.lessonId = lessonId;
            this.courseType = courseType;
        }
    }
}
