package com.huatu.tiku.course.web.controller.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.event.EventPublisher;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.bean.vo.EssayAnswerCardInfo;
import com.huatu.tiku.course.common.*;
import com.huatu.tiku.course.consts.RabbitMqConstants;
import com.huatu.tiku.course.consts.SimpleCourseLiveBackLog;
import com.huatu.tiku.course.consts.SyllabusInfo;
import com.huatu.tiku.course.hbase.api.v1.VideoServiceV1;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import com.huatu.tiku.course.service.manager.EssayExercisesAnswerMetaManager;
import com.huatu.tiku.course.service.v1.CourseExercisesService;
import com.huatu.tiku.course.service.v1.practice.CourseLiveBackLogService;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;
import com.huatu.tiku.course.ztk.api.v4.paper.PeriodTestServiceV4;
import com.huatu.tiku.entity.CourseExercisesProcessLog;
import com.huatu.tiku.entity.CourseLiveBackLog;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import com.huatu.tiku.essay.essayEnum.CourseWareTypeEnum;
import com.huatu.tiku.essay.essayEnum.SyllabusTypeEnum;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/6/25
 */
@Slf4j
@Component
public class CourseUtil {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private VideoServiceV1 videoServiceV1;

    @Autowired
    private PracticeCardServiceV1 practiceCardServiceV1;

    @Autowired
    private PeriodTestServiceV4 PeriodTestService;

    @Autowired
    private CourseLiveBackLogService courseLiveBackLogService;

    @Autowired
    private CourseExercisesService courseExercisesService;

    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;

    @Autowired
    private EssayExercisesAnswerMetaManager essayExercisesAnswerMetaManager;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 添加课程播放的时间-用以每日任务处理
     */
    public void pushPlayEvent(UserSession userSession, NetSchoolResponse netSchoolResponse, Object response) {
        //发布事件
        if (ResponseUtil.isSuccess(netSchoolResponse) && response instanceof Map && ((Map) response).containsKey("course")) {
            Object courseDetail = ((Map) response).get("course");
            boolean isFree = (courseDetail instanceof Map && ((Map) courseDetail).containsKey("free") && "1".equals(String.valueOf(((Map) courseDetail).get("free"))));
            pushPlayEvent(userSession, isFree);
        }
    }

    /**
     * 加金币
     */
    public void pushPlayEvent(UserSession userSession, boolean isFree) {
        if (isFree) {
            //免费课
            eventPublisher.publishEvent(RewardActionEvent.class,
                    this,
                    (event) -> event.setAction(RewardAction.ActionType.WATCH_FREE)
                            .setUname(userSession.getUname())
                            .setUid(userSession.getId())
            );
        } else {
            //收费课
            eventPublisher.publishEvent(RewardActionEvent.class,
                    this,
                    (event) -> event.setAction(RewardAction.ActionType.WATCH_PAY)
                            .setUname(userSession.getUname())
                            .setUid(userSession.getId())
            );
        }
    }

    /**
     * 在播放列表添加播放进度
     *
     * @param response 播放列表信息
     */
    public void addStudyProcessIntoSecrInfo(Object response, final String token, final String cv, final int terminal) {
        if (null != response) {
            JSONObject result = (JSONObject) response;
            Object resultList = result.get("lession");
            if (null != resultList) {
                //批量接口查询
                List<HashMap> paramList = ((List<Map>) resultList).parallelStream()
                        .map(data -> {
                            HashMap params = HashMapBuilder.<String, Object>newBuilder()
                                    .put("rid", String.valueOf(data.get("rid")))
                                    .put("joinCode", data.get("JoinCode") == null ? "" : String.valueOf(data.get("JoinCode")))
                                    .put("roomId", data.get("bjyRoomId") == null ? "" : String.valueOf(data.get("bjyRoomId")))
                                    .put("sessionId", data.get("bjySessionId") == null ? "" : String.valueOf(data.get("bjySessionId")))
                                    .build();
                            params.put((data.get("hasTeacher") == null || String.valueOf(data.get("hasTeacher")).equals("0")) ? "videoIdWithoutTeacher" : "videoIdWithTeacher",
                                    data.get("bjyVideoId") == null ? "" : String.valueOf(data.get("bjyVideoId")));
                            return params;
                        })
                        .collect(Collectors.toList());

                long currentTimeMillis = System.currentTimeMillis();
                Object data = videoServiceV1.videoProcessDetailV1(token, terminal, cv, paramList);
                //log.info(" videoServiceV1 videoProcessDetailV1 ===> token = {},paramList = {}",token, JSON.toJSON(paramList));
                List<HashMap<String, Object>> hbaseDataList = (List<HashMap<String, Object>>) ((Map<String, Object>) data).get("data");
                //log.info(" videoServiceV1 videoProcessDetailV1 ===> result = {},time = {}",JSON.toJSON(hbaseDataList),System.currentTimeMillis() - currentTimeMillis);

                if (null != hbaseDataList) {
                    //组装进度数据
                    List<Map> list = ((List<Map>) resultList).parallelStream()
                            .map(lessionData -> {
                                //查询匹配的数据
                                Optional<HashMap<String, Object>> first = hbaseDataList.parallelStream()
                                        .filter(hBaseData -> String.valueOf(lessionData.get("rid")).equals(String.valueOf(hBaseData.get("rid"))))
                                        .findFirst();
                                //如果有匹配数据
                                if (first.isPresent()) {
                                    HashMap<String, Object> buildResult = first.get();
                                    if (null == buildResult || null == buildResult.get("wholeTime") || (int) buildResult.get("wholeTime") == 0) {
                                        lessionData.put("process", 0);
                                    } else {
                                        float process = Float.valueOf((int) buildResult.get("playTime"))
                                                / Float.valueOf((int) buildResult.get("wholeTime"));
                                        BigDecimal bg = new BigDecimal(process);
                                        double f1 = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                                        lessionData.put("process", f1 * 100);
                                    }
                                } else {
                                    lessionData.put("process", 0);
                                }
                                return lessionData;
                            })
                            .collect(Collectors.toList());
                    result.replace("result", list);
                } else {
                    //此处只会在 快速失败的情况下被调用
                    List<Map> list = ((List<Map>) resultList).parallelStream()
                            .map(lessionData -> {
                                lessionData.put("process", 0);
                                return lessionData;
                            })
                            .collect(Collectors.toList());
                    result.replace("result", list);
                }
            }
        }
    }

    /**
     * 课程大纲-售后-添加课后答题结果信息
     *
     * @param response 响应结果集
     * @param userId   用户ID
     */
    public void addExercisesCardInfo(LinkedHashMap response, long userId, boolean need2Str) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("课程大纲-售后-添加课后答题结果信息");
        response.computeIfPresent("list", (key, value) -> {
                    List<HashMap<String, Object>> paramsList = ((List<Map>) value).stream()
                            .filter(map -> (null != MapUtils.getString(map, SyllabusInfo.Type)
                                    && MapUtils.getString(map, SyllabusInfo.Type).equals(String.valueOf(SyllabusTypeEnum.COURSE_WARE.getType())))
                            )
                            .filter(map -> null != map.get(SyllabusInfo.VideoType) && null != map.get(SyllabusInfo.CourseWareId))
                            .map(map -> {
                                HashMap<String, Object> build = HashMapBuilder.<String, Object>newBuilder()
                                        .put(SyllabusInfo.VideoType, MapUtils.getIntValue(map, SyllabusInfo.VideoType, 0))
                                        .put(SyllabusInfo.CourseId, MapUtils.getIntValue(map, SyllabusInfo.CourseWareId, 0))
                                        .build();
                                return build;
                            })
                            .collect(Collectors.toList());


                    //查询用户答题信息
                    log.info("获取课后练习的答题卡信息,参数信息，userId = {},paramsList = {}", userId, paramsList);
                    Object courseExercisesCardInfo = practiceCardServiceV1.getCourseExercisesCardInfo(userId, paramsList);
                    log.info("获取课后练习的答题卡信息,参数信息，userId = {},paramsList = {}, result = {}", userId, paramsList, JSONObject.toJSONString(courseExercisesCardInfo));
                    Object build = ZTKResponseUtil.build(courseExercisesCardInfo);
                    return buildResponseConstructCardInfo(need2Str, (List<Map>) value, (List<Map>) build);
                }
        );
        stopWatch.stop();
        log.info("学习报告 - 课后作业答题卡信息:userId:{},耗时:{}", userId,  stopWatch.prettyPrint());
    }


    /**
     * 课程大纲-售后-添加课后答题结果信息 V2
     *
     * @param response 响应结果集
     * @param userId   用户ID
     */
    @Deprecated
    public void addExercisesCardInfoV2(LinkedHashMap response, int userId, boolean need2Str) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("课程大纲-售后-添加课后答题结果信息 V2");
        response.computeIfPresent("list", (key, value) -> {
                    List<HashMap<String, Object>> paramsList = ((List<Map>) value).stream()
                            .filter(map -> (null != MapUtils.getString(map, SyllabusInfo.Type)
                                    && MapUtils.getString(map, SyllabusInfo.Type).equals(String.valueOf(SyllabusTypeEnum.COURSE_WARE.getType())))
                            )
                            .filter(map -> null != map.get(SyllabusInfo.VideoType) && null != map.get(SyllabusInfo.CourseWareId))
                            .map(map -> {
                                HashMap<String, Object> build = HashMapBuilder.<String, Object>newBuilder()
                                        .put(SyllabusInfo.VideoType, MapUtils.getIntValue(map, SyllabusInfo.VideoType, 0))
                                        .put(SyllabusInfo.CourseId, MapUtils.getIntValue(map, SyllabusInfo.CourseWareId, 0))
                                        .build();
                                return build;
                            })
                            .collect(Collectors.toList());


                    //查询用户答题信息
                    log.info("获取课后练习的答题卡信息 v2,参数信息, userId = {}, paramsList = {}", userId, paramsList);
                    List<Long> cardIds = courseExercisesProcessLogManager.obtainCardIdsByCourseTypeAndLessonId(userId, paramsList);
                    log.info("获取课后练习的答题卡信息 v2,答题卡返回信息, userId = {}, cardIds = {}", userId, cardIds);
                    Object courseExercisesCardInfo = practiceCardServiceV1.getCourseExercisesCardInfoV2(cardIds);
                    courseWorkDataConsistenceCheck(userId, paramsList, cardIds);
                    log.info("获取课后练习的答题卡信息 v2,参数信息, userId = {}, paramsList = {}, result = {}", userId, paramsList, JSONObject.toJSONString(courseExercisesCardInfo));
                    Object build = ZTKResponseUtil.build(courseExercisesCardInfo);

                    return buildResponseConstructCardInfo(need2Str, (List<Map>) value, (List<Map>) build);
                }
        );
        stopWatch.stop();
        log.info("学习报告 - 课后作业答题卡信息 V2:userId:{}, 耗时:{}", userId,  stopWatch.prettyPrint());
    }

    /**
     * 课程大纲-售后-添加课后答题结果信息 V3
     * 使用 大纲 id 获取
     * @param response 响应结果集
     * @param userId   用户ID
     */
    public void addExercisesCardInfoV3(LinkedHashMap response, int userId, boolean need2Str) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("课程大纲-售后-添加课后答题结果信息 V3");
        response.computeIfPresent("list", (key, value) -> {
                    List<Long> paramsList = ((List<Map>) value).stream()
                            .filter(map -> null != map.get(SyllabusInfo.VideoType) && null != map.get(SyllabusInfo.CourseWareId))
                            .filter(map -> null != map.get(SyllabusInfo.SyllabusId))
                            .map(map -> MapUtils.getLongValue(map, SyllabusInfo.SyllabusId))
                            .collect(Collectors.toList());

                    //查询用户答题信息
                    List<Map> build = Lists.newArrayList();
                    log.info("获取课后练习的答题卡信息 v3,参数信息, userId = {}, paramsList = {}", userId, paramsList);
                    if(CollectionUtils.isEmpty(paramsList) || userId == 0){
                        log.error("获取课后练习的答题卡信息 v3,参数信息 syllabusList is empty, userId:{}, response:{}",userId, response);
                        return buildResponseConstructCardInfo(need2Str, (List<Map>) value, build);
                    }
                    List<Long> cardIds = courseExercisesProcessLogManager.obtainCardIdsBySyllabusIds(userId, paramsList);
                    log.info("获取课后练习的答题卡信息 v3,答题卡返回信息, userId = {}, cardIds = {}", userId, cardIds);

                    if(CollectionUtils.isNotEmpty(cardIds)){
                        Object courseExercisesCardInfo = practiceCardServiceV1.getCourseExercisesCardInfoV2(cardIds);
                        log.info("获取课后练习的答题卡信息 v3,参数信息, userId = {}, paramsList = {}, result = {}", userId, paramsList, JSONObject.toJSONString(courseExercisesCardInfo));
                        Object object = ZTKResponseUtil.build(courseExercisesCardInfo);
                        build.addAll((List<Map>) object);
                    }
                    return buildResponseConstructCardInfo(need2Str, (List<Map>) value, build);
                }
        );
        stopWatch.stop();
        log.info("学习报告 - 课后作业答题卡信息 V3:userId:{}, 耗时:{}", userId,  stopWatch.prettyPrint());
    }


    /**
     * 课程大纲-售后-添加课后答题结果信息 with civil 直播 & 录播
     * 使用 大纲 id 获取
     * @param response 响应结果集
     * @param userId   用户ID
     */
    public void addExercisesCardInfoWithCivil(LinkedHashMap response, int userId, boolean need2Str) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("课程大纲-售后-添加课后答题结果信息 with civil");
        response.computeIfPresent("list", (key, value) -> {
                    List<Long> civilParamsList = ((List<Map>) value).stream()
                            .filter(map -> null != map.get(SyllabusInfo.VideoType) && null != map.get(SyllabusInfo.CourseWareId))
                            .filter(map -> MapUtils.getIntValue(map, SyllabusInfo.VideoType) != CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK.getVideoType())
                            .filter(map -> null != map.get(SyllabusInfo.SyllabusId))
                            .filter(map -> null != map.get(SyllabusInfo.SubjectType) && MapUtils.getIntValue(map, SyllabusInfo.SubjectType) == SubjectEnum.XC.getCode())
                            .map(map -> MapUtils.getLongValue(map, SyllabusInfo.SyllabusId))
                            .collect(Collectors.toList());

                    //查询用户答题信息
                    List<Map> build = Lists.newArrayList();
                    log.info("获取课后练习的答题卡信息 with civil,参数信息, userId = {}, paramsList = {}", userId, civilParamsList);
                    if(CollectionUtils.isEmpty(civilParamsList)){
                        log.error("获取课后练习的答题卡信息 civil,参数信息 syllabusList is empty, userId:{}, response:{}",userId, response);
                        return buildResponseConstructCardInfo(need2Str, (List<Map>) value, build);
                    }
                    List<Long> cardIds = courseExercisesProcessLogManager.obtainCardIdsBySyllabusIds(userId, civilParamsList);
                    log.info("获取课后练习的答题卡信息 civil,答题卡返回信息, userId = {}, cardIds = {}", userId, cardIds);

                    if(CollectionUtils.isNotEmpty(cardIds)){
                        Object courseExercisesCardInfo = practiceCardServiceV1.getCourseExercisesCardInfoV2(cardIds);
                        log.info("获取课后练习的答题卡信息 civil,参数信息, userId = {}, paramsList = {}, result = {}", userId, civilParamsList, JSONObject.toJSONString(courseExercisesCardInfo));
                        Object object = ZTKResponseUtil.build(courseExercisesCardInfo);
                        build.addAll((List<Map>) object);
                    }
                    return buildResponseConstructCardInfo(need2Str, (List<Map>) value, build);
                }
        );
        stopWatch.stop();
        log.info("学习报告 - 课后作业答题卡信息 V3:userId:{}, 耗时:{}", userId,  stopWatch.prettyPrint());
    }


    /**
     * 课程大纲-售后-添加课后答题结果信息 with essay
     * 使用 大纲 id 获取
     * @param response 响应结果集
     * @param userId   用户ID
     */
    public void addExercisesCardInfoWithEssay(LinkedHashMap response, int userId) {
        if(!response.containsKey("list")){
            return;
        }
        List<Map> mapList = (List<Map>) response.get("list");
        for(Map map : mapList){
            if(check(map)){
                EssayAnswerCardInfo essayAnswerCardInfo = essayExercisesAnswerMetaManager.buildEssayAnswerCardInfo(userId, map);
                map.put("answerCard", essayAnswerCardInfo);
            }
        }
    }

    /**
     * 大纲数据检查
     * @param map
     * @return
     */
    private boolean check(Map map){
        boolean result = true;
        result = result && MapUtils.getIntValue(map, SyllabusInfo.VideoType, 0) > 0;
        result = result && MapUtils.getIntValue(map, SyllabusInfo.CourseWareId, 0) > 0;
        result = result && MapUtils.getIntValue(map, SyllabusInfo.SubjectType) == SubjectEnum.SL.getCode();
        result = result && MapUtils.getIntValue(map, SyllabusInfo.AfterCourseNum, 0) > 0;
        return result;
    }

    private Object buildResponseConstructCardInfo(boolean need2Str, List<Map> value, List<Map> build) {
        Map<Object, Object> defaultMap = HashMapBuilder.newBuilder()
                .put("status", 0)
                .put("rcount", 0)
                .put("wcount", 0)
                .put("ucount", 0)
                .put("id", need2Str ? "0" : 0)
                .build();
        List<Map> courseExercisesCards = build;         //课后作业相关答题卡
        if (CollectionUtils.isNotEmpty(courseExercisesCards)) {
            //获取答题卡信息状态
            Function<HashMap<String, Object>, Map> getCourse = (valueData) -> {
                Optional<Map> first = courseExercisesCards.stream()
                        .filter(result -> null != result.get(SyllabusInfo.CourseId) && null != result.get("courseType"))
                        .filter(result ->
                                MapUtils.getString(result, SyllabusInfo.CourseId).equals(MapUtils.getString(valueData, SyllabusInfo.CourseWareId))
                                        && MapUtils.getString(result, "courseType").equals(MapUtils.getString(valueData, SyllabusInfo.VideoType))
                        )
                        .findFirst();
                if (first.isPresent()) {
                    Map map = first.get();
                    map.remove("courseId");
                    map.remove("courseType");
                    if(need2Str){
                        map.computeIfPresent("id", (mapK, mapV) -> String.valueOf(mapV));
                    }
                    return map;
                } else {
                    return defaultMap;
                }
            };
            List<Map> mapList = value.stream()
                    .map(valueData -> {
                        Map answerCard = getCourse.apply((HashMap<String, Object>) valueData);
                        valueData.put("answerCard", answerCard);
                        return valueData;
                    })
                    .collect(Collectors.toList());
            return mapList;
        } else {
            List<Map> mapList = value.stream()
                    .map(valueData -> {
                        valueData.put("answerCard", defaultMap);
                        return valueData;
                    })
                    .collect(Collectors.toList());
            return mapList;
        }
    }


    /**
     * V6 之前接口不展示课后作业
     * @param response
     */
    public void filterV5AndV6(LinkedHashMap response){
        List<Map<String,Object>> list = (List<Map<String,Object>>) response.get("list");
        for(int i = 0; i < list.size(); i ++) {
            Map<String, Object> currentMap = list.get(i);
            int subjectType = MapUtils.getIntValue(currentMap, SyllabusInfo.SubjectType, SubjectEnum.XC.getCode());
            if(subjectType == SubjectEnum.SL.getCode()){
                currentMap.put(SyllabusInfo.AfterCourseNum, 0);
            }
        }
    }

    /**
     * 课程大纲-售后-添加课后答题结果信息 - 处理直播回放课后作业信息
     * @param response
     * @param userId
     * @param need2Str
     */
    public void addLiveCardExercisesCardInfo(LinkedHashMap response, long userId, boolean need2Str){
        StopWatch stopWatch = new StopWatch("courseUtil - addLiveCardExercisesCardInfo");
        stopWatch.start();
        List<Map<String,Object>> list = (List<Map<String,Object>>) response.get("list");
        //遍历数据 -> 查询直播回放的直播课件信息 存储当前遍历数据索引 index 和 答题卡 id
        TreeMap<Integer, Long> answerCardTree = Maps.newTreeMap();
        for(int i = 0; i < list.size(); i ++){
            Map<String,Object> currentMap = list.get(i);
            int type = MapUtils.getIntValue(currentMap, SyllabusInfo.Type);
            int videoType = MapUtils.getIntValue(currentMap, SyllabusInfo.VideoType);
            Long courseWareId = MapUtils.getLong(currentMap, SyllabusInfo.CourseWareId);
            SyllabusTypeEnum typeEnum = SyllabusTypeEnum.create(type);
            CourseWareTypeEnum.VideoTypeEnum videoTypeEnum = CourseWareTypeEnum.VideoTypeEnum.create(videoType);
            if(typeEnum != SyllabusTypeEnum.COURSE_WARE){
                continue;
            }
            if(videoTypeEnum != CourseWareTypeEnum.VideoTypeEnum.LIVE_PLAY_BACK){
                continue;
            }
            long bjyRoomId = MapUtils.getLongValue(currentMap, SyllabusInfo.BjyRoomId);
            SimpleCourseLiveBackLog courseLiveBackLog = courseLiveBackLogService.findByRoomIdAndLiveCourseWareIdV2(bjyRoomId, courseWareId);
            if(null == courseLiveBackLog || null == courseLiveBackLog.getLiveCourseWareId()){
                continue;
            }
            //课后作业数目处理
            List<Map<String, Object>> listQuestionByCourseId = courseExercisesService.listQuestionByCourseId(CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType(), courseLiveBackLog.getLiveCourseWareId());
            if (CollectionUtils.isEmpty(listQuestionByCourseId)) {
                continue;
            }
            currentMap.put(SyllabusInfo.AfterCourseNum, listQuestionByCourseId.size());
            //如果是直播回放 -> 查询直播回放的课后练习数据信息
            Optional<CourseExercisesProcessLog> optionalCourseExercisesProcessLog = courseExercisesProcessLogManager.getCourseExercisesProcessLogByTypeAndWareId(userId, CourseWareTypeEnum.VideoTypeEnum.LIVE.getVideoType(), courseLiveBackLog.getLiveCourseWareId());
            if(!optionalCourseExercisesProcessLog.isPresent()){
                continue;
            }
            answerCardTree.put(i, optionalCourseExercisesProcessLog.get().getCardId());
        }
        if(answerCardTree.isEmpty()){
            return;
        }
        try{
            String answerCardIds = answerCardTree.values().stream().map(Object::toString).collect(Collectors.joining(","));
            Object courseExercisesCardInfo = practiceCardServiceV1.getCourseExercisesCardInfoBatch(answerCardIds);
            Object build = ZTKResponseUtil.build(courseExercisesCardInfo);
            List<Map<String,Object>> courseExercisesCards = (List<Map<String,Object>>) build;
            if(CollectionUtils.isEmpty(courseExercisesCards)){
                return;
            }
            log.debug("addLiveCardExercisesCardInfo -> getCourseExercisesCardInfoBatch: cardIds:{}, result.size",answerCardIds, courseExercisesCards.size());
            Map<Long, Integer> convertTreeMap = answerCardTree.keySet().stream().collect(Collectors.toMap(i -> answerCardTree.get(i),i -> i));
            for(Map<String, Object> cardMap : courseExercisesCards){
                Long answerCardId = MapUtils.getLong(cardMap, "id");
                int index = convertTreeMap.get(answerCardId);
                Map<String,Object> detail = list.get(index);
                if(need2Str){
                    detail.computeIfPresent("id", (mapK, mapV) -> String.valueOf(mapV));
                }
                detail.put("answerCard", cardMap);
            }
            stopWatch.stop();
            log.info("courseUtil - addLiveCardExercisesCardInfo - userId:{}, 耗时:{}",userId, stopWatch.prettyPrint());
        }catch (Exception e){
            log.error("addLiveCardExercisesCardInfo caught an error!");
        }
    }

    /**
     * 处理阶段考试状态信息
     * @param response
     * @param userId
     */
    public void addPeriodTestInfo(LinkedHashMap response, int userId){
                StopWatch stopWatch = new StopWatch("courseUtil - addPeriodTestInfo");
                stopWatch.start();
                response.computeIfPresent("list", (key, value) -> {
                        Set<String> paperIds = ((List<Map>) value).stream()
                                //videoType	1点播2直播3直播回放4阶段测试题
                                .filter(map -> CourseWareTypeEnum.VideoTypeEnum.create(MapUtils.getIntValue(map, "videoType")) == CourseWareTypeEnum.VideoTypeEnum.PERIOD_TEST)
                                    //coursewareId	课件id
                                .filter(map -> null != map.get(SyllabusInfo.CourseWareId) && null != map.get("id"))
                                .map(map -> {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder
                                            .append(MapUtils.getString(map, "coursewareId"))
                                            .append("_")
                                            .append(MapUtils.getString(map,"id"));
                                    return stringBuilder.toString();
                                })
                                .collect(Collectors.toSet());
                        //查询用户答题信息
                        log.info("获取阶段测试完成情况：userId = {}, paperIds = {}", userId, paperIds);
                        NetSchoolResponse netSchoolResponse = PeriodTestService.getPaperStatusBath(userId, paperIds);
                        Map<String, Object> data = (Map<String, Object>) netSchoolResponse.getData();
			if (null != data && data.size() > 0) {
				List<Map> mapList = ((List<Map>) value).stream().map(valueData -> {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(MapUtils.getString(valueData, "coursewareId")).append("_")
							.append(MapUtils.getString(valueData, "id"));
					valueData.put("testStatus", MapUtils.getInteger(data, stringBuilder.toString(), -1));
					// 设置是否过期
					if (1 == MapUtils.getInteger(valueData, "isEffective")
                            //当前时间大于结束时间（liveStartTime）
							&& System.currentTimeMillis() > (MapUtils.getLong(valueData, "liveStartTime"))*1000) {
						valueData.put("isExpired", 1);
					} else {
						valueData.put("isExpired", 0);
					}
					return valueData;
				}).collect(Collectors.toList());
				return mapList;
			} else {
                            List<Map> mapList = ((List<Map>) value).stream()
                                    .map(valueData -> {
                                        valueData.put("testStatus", -1);
                                        return valueData;
                                    })
                                    .collect(Collectors.toList());
                            return mapList;
                        }
                    }
            );
        stopWatch.stop();
        log.info("courseUtil addPeriodTestInfo uerId:{}, 耗时:{}", userId, stopWatch.prettyPrint());
    }



    /**
     * 处理学习报告逻辑
     * @param response
     * @param userId
     */
    public void addLearnReportInfoV2(LinkedHashMap response, int userId){
        StopWatch stopWatch = new StopWatch("courseUtil - addLearnReportInfoV2");
        stopWatch.start();
        List<Map<String, Object>> list = (List<Map<String, Object>>)response.get("list");
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        for (Map<String, Object> stringObjectMap : list) {
            try{
                int type =  MapUtils.getInteger(stringObjectMap, SyllabusInfo.Type);
                SyllabusTypeEnum typeEnum = SyllabusTypeEnum.create(type);
                if(typeEnum != SyllabusTypeEnum.COURSE_WARE){
                    stringObjectMap.put(SyllabusInfo.ReportStatus, YesOrNoStatus.UN_DEFINED.getCode());
                    continue;
                }
                int videoType = MapUtils.getIntValue(stringObjectMap, SyllabusInfo.VideoType);
                long courseWareId = MapUtils.getLong(stringObjectMap, SyllabusInfo.CourseWareId);
                String bjyRoomId = MapUtils.getString(stringObjectMap, SyllabusInfo.BjyRoomId);
                int liveStatus = MapUtils.getIntValue(stringObjectMap, SyllabusInfo.LiveStatus);
                int studyReport = MapUtils.getIntValue(stringObjectMap, SyllabusInfo.StudyReport);
                Map<String,Object> branchMap = dealLearnReportBranchInfo(videoType, courseWareId, bjyRoomId, userId, liveStatus, studyReport);
                stringObjectMap.putAll(branchMap);
            }catch (Exception e){
                log.error("处理大纲我的学习好高状态异常:{}, userId:{}",stringObjectMap, userId);
            }
        }
        stopWatch.stop();
        log.info("courseUtil - addLearnReportInfoV2 - userId:{}, 耗时:{}",userId, stopWatch.prettyPrint());
    }


    /**
     * 三条分支更新 学习报告
     * @param videoType
     * @param courseWareId
     * @param bjyRoomId
     * @param userId
     * @param liveStatus
     * @param studyReport
     * @return
     */
    public Map<String,Object> dealLearnReportBranchInfo(int videoType, long courseWareId, String bjyRoomId, int userId, int liveStatus, int studyReport){
        Map<String, Object> branchMap = Maps.newHashMap();
        branchMap.put(SyllabusInfo.ReportStatus, YesOrNoStatus.UN_DEFINED.getCode());
        CourseWareTypeEnum.VideoTypeEnum videoTypeEnum = CourseWareTypeEnum.VideoTypeEnum.create(videoType);
        switch (videoTypeEnum){
            case LIVE:
                branchMap.putAll(doLiveReport(liveStatus));
                break;
            case LIVE_PLAY_BACK:
                branchMap.putAll(doLivePlayBack(courseWareId, bjyRoomId));
                break;
            case DOT_LIVE:
                YesOrNoStatus studyReportEnum = YesOrNoStatus.create(studyReport);
                if(studyReportEnum == YesOrNoStatus.YES){
                    Map dotLive = doDotLive(courseWareId, userId);
                    branchMap.putAll(dotLive);
                }
                break;
            default:
                branchMap.put(SyllabusInfo.ReportStatus, YesOrNoStatus.UN_DEFINED.getCode());
        }
        return branchMap;
    }
    /**
     * 直播报告处理
     * @param liveStatus
     * @return
     * @throws BizException
     */
    private Map<String, Object> doLiveReport(int liveStatus) throws BizException{
        StopWatch stopWatch = new StopWatch("addLearnReportInfoV2 - doLiveReport");
        stopWatch.start();
        Map<String, Object> result = Maps.newHashMap();
        LiveStatusEnum liveStatusEnum = LiveStatusEnum.create(liveStatus);
        if(liveStatusEnum == LiveStatusEnum.FINISHED){
            result.put(SyllabusInfo.ReportStatus, YesOrNoStatus.YES.getCode());
        }
        stopWatch.stop();
        log.info("addLearnReportInfoV2 - doLiveReport, 耗时:{}", stopWatch.prettyPrint());
        return result;
    }

    /**
     * 直播回放报告处理
     * @param liveBackCoursewareId
     * @param bjyRoomId
     * @return
     * @throws BizException
     */
    private Map<String, Object> doLivePlayBack(long liveBackCoursewareId, String bjyRoomId) throws BizException{
        StopWatch stopWatch = new StopWatch("addLearnReportInfoV2 - doLivePlayBack");
        Map<String,Object> result = Maps.newHashMap();
        Example example = new Example(CourseLiveBackLog.class);
        example.and()
                .andEqualTo("liveBackCoursewareId", liveBackCoursewareId)
                .andEqualTo("roomId", bjyRoomId);
        try{
            SimpleCourseLiveBackLog courseLiveBackLog = courseLiveBackLogService.findByRoomIdAndLiveCourseWareIdV2(Long.valueOf(bjyRoomId), liveBackCoursewareId);
            if(null == courseLiveBackLog || null == courseLiveBackLog.getLiveCourseWareId()){
                result.put(SyllabusInfo.ReportStatus, YesOrNoStatus.NO.getCode());
            }else{
                result.put(SyllabusInfo.ReportStatus, YesOrNoStatus.YES.getCode());
            }
            stopWatch.stop();
            log.info("addLearnReportInfoV2 - doLivePlayBack, 耗时:{}", stopWatch.prettyPrint());
            return result;
        }catch (Exception e){
            result.put("reportStatus", YesOrNoStatus.NO.getCode());
            return result;
        }
    }

    /**
     * 录播逻辑处理
     * 获取答题卡 id --> 调用 paper 获取答题卡信息
     * @param courseWareId
     * @return
     * @throws BizException
     */
    private Map doDotLive(long courseWareId, int userId) throws BizException{
        StopWatch stopWatch = new StopWatch("addLearnReportInfoV2 - doDotLive");
        stopWatch.start();
        Map<String,Object> result = Maps.newHashMap();
        result.put("reportStatus", YesOrNoStatus.NO.getCode());
        NetSchoolResponse netSchoolResponse = practiceCardServiceV1.getClassExerciseReport(courseWareId, CourseWareTypeEnum.VideoTypeEnum.DOT_LIVE.getVideoType(), userId);
        if(ResponseUtil.isSuccess(netSchoolResponse) && null != netSchoolResponse.getData() ){
            Map<String,Object> data = (Map<String,Object>)netSchoolResponse.getData();
            if(data.containsKey("id")){
                result.put("reportStatus", YesOrNoStatus.YES.getCode());
                return result;
            }
        }
        stopWatch.stop();
        log.info("addLearnReportInfoV2 - doDotLive, 耗时:{}", stopWatch.prettyPrint());
        return result;
    }

    /**
     * 课后作业待处理的 userId
     * @param userId
     */
    @Deprecated
    public synchronized void dealCourseWorkReport2BProcessed(int userId){
        String userIdStr = String.valueOf(userId);
        String alreadyProcessed = CourseCacheKey.COURSE_WORK_REPORT_USERS_ALREADY_PROCESSED;
        SetOperations<String, String> alreadyProcessedOperations = redisTemplate.opsForSet();
        //如果用户已经存在当前等待处理的set中不处理
        if(alreadyProcessedOperations.isMember(alreadyProcessed, userIdStr)){
            log.info("already processed userId:{}", userIdStr);
        }else{
            log.debug("deal userId:{} into rabbit mq", userIdStr);
            rabbitTemplate.convertAndSend("", RabbitMqConstants.COURSE_WORK_REPORT_USERS_DEAL_QUEUE, userIdStr);
        }
    }

    /**
     * 处理课后作业数据是否需要 fix
     * @param userId
     * @param paramsList
     * @param cardIds
     */
    private void courseWorkDataConsistenceCheck(int userId, List<HashMap<String, Object>> paramsList, List<Long> cardIds){
        int paramSize = paramsList.size();
        int cardSize = cardIds.size();
        if(CollectionUtils.isEmpty(paramsList)){
            return;
        }
        if(paramSize > cardSize){
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            final ImmutableList<HashMap<String, Object>> hashMapImmutableList = ImmutableList.copyOf(paramsList);
            final ImmutableList<Long> longImmutableList = ImmutableList.copyOf(cardIds);
            final int userId_ = userId;
            executorService.execute(() ->{
                courseExercisesProcessLogManager.analyzeCardIdExist(userId_, hashMapImmutableList, longImmutableList);
                log.info("课后练习数据答题卡，数据fix进入队列 -> userId = {}, paramList = {}, cardIds = {}", userId_, hashMapImmutableList, longImmutableList);
            });
            executorService.shutdown();
        }
    }
}
