package com.huatu.tiku.teacher.service.impl.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.baseEnum.BaseStatusEnum;
import com.huatu.tiku.config.web.event.PaperChangeEvent;
import com.huatu.tiku.constants.teacher.ExportType;
import com.huatu.tiku.course.common.EstimateCourseRedisKey;
import com.huatu.tiku.entity.knowledge.QuestionKnowledge;
import com.huatu.tiku.entity.question.PaperQuestionSimpleInfo;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.entity.teacher.PaperSearchInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.request.paper.SelectActivityReq;
import com.huatu.tiku.teacher.dao.knowledge.KnowledgeSubjectMapper;
import com.huatu.tiku.teacher.dao.mongo.MatchDao;
import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.enums.ActivityLookParseType;
import com.huatu.tiku.teacher.enums.ActivityTagEnum;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.enums.DifficultyLevelEnum;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.common.ImportPaperService;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.download.v1.PdfWriteServiceV1;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.knowledge.QuestionKnowledgeService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.paper.PaperSearchService;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.MatchBackendStatus;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.common.ResponseMsg;
import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.huatu.tiku.service.impl.BaseServiceImpl.throwBizException;
import static com.huatu.tiku.teacher.enums.ActivityTypeAndStatus.ActivityTypeEnum.MATCH;
import static com.huatu.tiku.teacher.enums.ActivityTypeAndStatus.ActivityTypeEnum.TRUE_PAPER;
import static com.huatu.ztk.paper.common.PaperStatus.DELETED;

/**
 * 处理试卷-活动 Mysql-MongoDB 同步
 * Created by lijun on 2018/9/19
 */
@Slf4j
@Service
public class ImportPaperServiceImpl implements ImportPaperService, ApplicationContextAware, ApplicationListener<PaperChangeEvent> {

    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PaperActivityService paperActivityService;

    @Autowired
    private PaperSearchService paperSearchService;

    @Autowired
    ImportService importService;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private OldPaperDao oldPaperDao;

    @Value("${updateFormativeTestPaper}")//(TODO-lzj)
    private String updateFormativeTestPaper;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private KnowledgeSubjectMapper knowledgeSubjectMapper;
    @Autowired
    private QuestionKnowledgeService questionKnowledgeService;

    @Autowired
    private KnowledgeService knowledgeService;
    @Autowired
    private PdfWriteServiceV1 pdfWriteService;


    @Override

    public void importPaper(long paperId) {
        //1.根据试卷查询可用题目
        if (paperId > 0) {
            PaperChangeEvent paperChangeEvent = new PaperChangeEvent(applicationContext, paperId);
            applicationContext.publishEvent(paperChangeEvent);
        }
    }

    @Override
    public void importPaper(List<Long> paperIds) {
        if (CollectionUtils.isNotEmpty(paperIds)) {
            paperIds.stream()
                    .filter(paperId -> null != paperId)
                    .forEach(paperId -> importPaper(paperId));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 异步处理MongoDB数据同步问题
     */
    @Async
    @Override
    public void onApplicationEvent(PaperChangeEvent event) {
        Long paperId;
        if (null == (paperId = event.getPaperId())) {
            return;
        }
        //1.根据试卷查询可用题目
        SelectActivityReq paperActivity = paperActivityService.findById(paperId);
        if (null == paperActivity) {
            throwBizException("活动卷不存在");
        }
        if (BaseStatusEnum.isNormal(paperActivity.getStatus())) {
            updatePaperInfo(paperActivity);
        } else {//试卷信息 删除 - 删除试卷信息逻辑
            removePaperInfo(paperActivity);
        }
    }

    /**
     * 更新试卷信息
     *
     * @param paperActivity 待更新的试卷信息
     */
    public void updatePaperInfo(SelectActivityReq paperActivity) {
        //试卷信息 正常 - 更新试卷信息逻辑
        ActivityTypeAndStatus.ActivityTypeEnum activityTypeEnum = ActivityTypeAndStatus.ActivityTypeEnum.create(paperActivity.getType());
        //试卷信息更新到mongo后，同步试题信息
        Consumer<Paper> importQuestion = (paper -> {
            List<Integer> questions = paper.getQuestions();
            if (CollectionUtils.isNotEmpty(questions)) {
                log.info("开始同步试卷下试题:{}", questions.stream().map(String::valueOf).collect(Collectors.joining(",")));
                importService.sendQuestion2Mongo(questions);
            }
        });
        switch (activityTypeEnum) {
            case TRUE_PAPER://真题卷
            case APPLETS_PAPER:     //小程序
                Paper paper = new Paper();
                fillPaperBaseInfo(paper, paperActivity);
                oldPaperDao.save(paper);
                importQuestion.accept(paper);
                break;
            case REGULAR_PAPER: //专项模考
            case ESTIMATE_PAPER://精准估分
                EstimatePaper estimatePaper = new EstimatePaper();
                fillPaperBaseInfo(estimatePaper, paperActivity);
                fillPaperEstimateInfo(estimatePaper, paperActivity);
                //小程序添加课程id
                estimatePaper.setCourseId(paperActivity.getCourseId() == null ? 0 : paperActivity.getCourseId().intValue());
                estimatePaper.setCourseInfo(paperActivity.getCourseInfo());
                oldPaperDao.save(estimatePaper);
                importQuestion.accept(estimatePaper);
                //删除活动的缓存信息
                clearEstimatePaperCache(estimatePaper.getCatgory() + "");
                break;
            case MATCH://模考大赛
                importCommonEstimateInfo(paperActivity, importQuestion);
                Match match = new Match();
                fillMatchInfo(match, paperActivity);
                matchDao.save(match);
                break;
            case SMALL_ESTIMATE: //小模考
                importCommonEstimateInfo(paperActivity, importQuestion);
                //小模考课程ID维护
                updateSmallEstimateCourseListCache(paperActivity);
                break;
            case FORMATIVE_TEST_ESTIMATE:// 阶段测试
                importCommonEstimateInfo(paperActivity, importQuestion);
                //阶段测试试卷修改同步到php
                syncPaperToPhp(paperActivity);
                break;
            default:
                throwBizException("无效的活动类型");
        }

        /**
         * 异步生成pdf
         */
        createPdfInfo(paperActivity);
        clearPaperDetailCache(paperActivity.getId());
        log.info("活动试卷信息同步完成更新,活动卷ID是:{},试题卷ID是:{}", paperActivity.getId(), paperActivity.getPaperId());

    }

    /**
     * 下载类型 默认2题目内容    1全部属性   3解析内容
     *
     * @param paperActivity
     */
    public void createPdfInfo(SelectActivityReq paperActivity) {
        String fileUrl = null;
        Integer paperType = PaperInfoEnum.TypeInfo.SIMULATION.getCode();
        Integer exportType = ExportType.PAPER_WORD_TYPE_SIDE_STEM;
        Long paperId = paperActivity.getId();
        PaperInfoEnum.TypeInfo typeInfo = PaperInfoEnum.TypeInfo.create(paperType);
        QuestionElementEnum.QuestionFieldEnum questionFieldEnum = QuestionElementEnum.QuestionFieldEnum.create(exportType);
        Map mapData = Maps.newHashMap();
        mapData.put("paperId", paperId);

        try {
            fileUrl = pdfWriteService.download(paperId, typeInfo, questionFieldEnum, mapData);
            mapData.put("url", fileUrl);
            PaperActivity newPaperActivity = paperActivityService.selectByPrimaryKey(paperId);
            if (null != paperActivity) {
                Timestamp updateTime = null;
                if (null == paperActivity.getGmtModify()) {
                    updateTime = newPaperActivity.getGmtCreate();
                } else {
                    updateTime = newPaperActivity.getGmtModify();
                }
                mapData.put("time", updateTime.getTime());
                mapData.put("updateTime", new Date(updateTime.getTime()));
            }

            pdfWriteService.saveDownCache(paperId, paperType, exportType, mapData);
            log.info("异步生成pdf试卷成功,缓存信息是:{}", mapData);

        } catch (IOException e) {
            mapData.put("error", e.getMessage());
            e.printStackTrace();
            log.info("试卷pdf生成失败,试卷ID是:{}", paperId);
        } catch (DocumentException e) {
            mapData.put("error", e.getMessage());
            log.info("试卷pdf生成失败,试卷ID是:{}", paperId);
            e.printStackTrace();
        }


    }


    /**
     * 维护小模考的历史解析课
     *
     * @param paperActivity
     */
    private void updateSmallEstimateCourseListCache(SelectActivityReq paperActivity) {
        Long courseId = paperActivity.getCourseId();
        long time = paperActivity.getOnlineTime().getTime();
        List<Long> subjectIds = paperActivity.getSubjectIds();
        if (CollectionUtils.isNotEmpty(subjectIds)) {
            for (Long subjectId : subjectIds) {
                List<Paper> papers = oldPaperDao.findByTypeAndSubject(subjectId.intValue(), PaperType.SMALL_ESTIMATE);
                RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
                try {
                    String smallEstimateCourseIdKey = EstimateCourseRedisKey.getSmallEstimateCourseIdKey(subjectId.intValue());
                    connection.del(smallEstimateCourseIdKey.getBytes());
                    if (CollectionUtils.isNotEmpty(papers)) {
                        connection.zAdd(smallEstimateCourseIdKey.getBytes(), time, String.valueOf(courseId).getBytes());
                        for (Paper paper : papers) {
                            if (paper instanceof EstimatePaper) {
                                long startTime = ((EstimatePaper) paper).getStartTime();
                                connection.zAdd(smallEstimateCourseIdKey.getBytes(), startTime, String.valueOf(((EstimatePaper) paper).getCourseId()).getBytes());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    connection.close();
                }
            }
        }
    }


    /**
     * 处理试卷同步公共操作
     *
     * @param paperActivity
     * @param importQuestion
     */
    public void importCommonEstimateInfo(SelectActivityReq paperActivity, Consumer<Paper> importQuestion) {
        EstimatePaper paperInfo = new EstimatePaper();
        fillPaperBaseInfo(paperInfo, paperActivity);
        fillPaperEstimateInfo(paperInfo, paperActivity);
        oldPaperDao.save(paperInfo);
        importQuestion.accept(paperInfo);
    }


    /**
     * 清除试卷缓存信息
     *
     * @param id
     */
    private void clearPaperDetailCache(Long id) {
        if (null == id || id <= 0) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder("paper-web-server.");
        stringBuilder.append(PaperRedisKeys.getPaperKey(id.intValue()));
        log.info("删除试卷缓存：{}", stringBuilder.toString());
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try {
            connection.del(stringBuilder.toString().getBytes());
        } finally {
            connection.close();
        }
    }

    /**
     * 删除试卷信息
     *
     * @param paperActivity 待移除试卷信息
     */
    public void removePaperInfo(SelectActivityReq paperActivity) {
        if (BaseStatusEnum.isNormal(paperActivity.getStatus())) {
            return;
        }
        ActivityTypeAndStatus.ActivityTypeEnum activityTypeEnum = ActivityTypeAndStatus.ActivityTypeEnum.create(paperActivity.getType());
        EstimatePaper estimatePaper = new EstimatePaper();
        fillPaperBaseInfo(estimatePaper, paperActivity);
        fillPaperEstimateInfo(estimatePaper, paperActivity);
        estimatePaper.setStatus(DELETED);
        oldPaperDao.save(estimatePaper);
        if (activityTypeEnum == MATCH) {
            Match match = new Match();
            fillMatchInfo(match, paperActivity);
            match.setStatus(MatchBackendStatus.DELETE);
            matchDao.save(match);
        }
        log.info("活动试卷信息同步完成_______删除:  {}", paperActivity.getId());
    }


    /**
     * 填充 试卷的基础信息
     *
     * @param paper         待填充试卷
     * @param paperActivity 活动卷信息
     */
    private void fillPaperBaseInfo(Paper paper, SelectActivityReq paperActivity) {
        paper.setId(paperActivity.getId().intValue());
        paper.setName(paperActivity.getName());
        paper.setYear(paperActivity.getYear());
        //区域ID
        List<Long> areaIds = paperActivity.getAreaIds();
        List<Integer> areaIdList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(areaIds)) {
            paper.setArea(areaIds.get(0).intValue());
            areaIdList = areaIds.stream().map(areaId -> areaId.intValue()).collect(Collectors.toList());
        }
        paper.setAreaIds(areaIdList);

        paper.setTime(paperActivity.getLimitTime() * 60);
        //单题算分,计算分数
        Double paperQuestionScore = paperActivityService.getScore(paperActivity);
        paper.setScore(paperQuestionScore.intValue());
        //及格线
        paper.setPassScore(paperActivity.getTotalScore().intValue() / 10 * 6);
        //TODO 难度默认给中等
        paper.setDifficulty(DifficultyLevelEnum.GENERAL.getValue());
        paper.setType(paperActivity.getType());
        if (null != paperActivity.getCreatorId()) {
            paper.setCreatedBy(paperActivity.getCreatorId().intValue());
        }
        paper.setCreateTime(paperActivity.getGmtCreate());
        List<Long> subjectIds = paperActivity.getSubjectIds();
        if (CollectionUtils.isNotEmpty(subjectIds)) {
            paper.setCatgory(subjectIds.get(0).intValue());
        }
        paper.setStatus(paperActivity.getBizStatus());
        //模块和题目ID添加
        fillModuleWithQuestions(paper, paperActivity);
        // 设置总分是否生效（即参与学员总分计算）
        paper.setScoreFlag(paperActivity.getScoreFlag());
        //阶段测试新增字段（开始时间和结束时间是否生效）
        if (paperActivity.getType() == PaperType.FORMATIVE_TEST_ESTIMATE) {
            if (paper instanceof EstimatePaper) {
                ((EstimatePaper) paper).setStartTimeIsEffective(paperActivity.getStartTimeIsEffective());
            }
        }
        //小模考新增课程信息
        if (paperActivity.getType() == PaperType.SMALL_ESTIMATE) {
            if (paper instanceof EstimatePaper) {
                Long courseId = paperActivity.getCourseId();
                if (null == courseId) {
                    courseId = 0L;
                }
                ((EstimatePaper) paper).setCourseId(courseId.intValue());
                ((EstimatePaper) paper).setCourseInfo(StringUtils.isEmpty(paperActivity.getCourseInfo()) ? "" : paperActivity.getCourseInfo());
                ((EstimatePaper) paper).setCourseName(StringUtils.isEmpty(paperActivity.getCourseName()) ? "" : paperActivity.getCourseName());
            }
        }

        //小模考,阶段测试需要添加试卷涉及到的三级知识点
        if (paperActivity.getType() == PaperType.SMALL_ESTIMATE || paperActivity.getType() == PaperType.FORMATIVE_TEST_ESTIMATE) {
            if (paper instanceof EstimatePaper) {
                ((EstimatePaper) paper).setPointsName(getQuestionPoint(paperActivity));
            }
        }

        //小模考,阶段测试需要添加试卷涉及到的三级知识点
        if (paperActivity.getType() == PaperType.SMALL_ESTIMATE || paperActivity.getType() == PaperType.FORMATIVE_TEST_ESTIMATE) {
            if (paper instanceof EstimatePaper) {
                ((EstimatePaper) paper).setPointsName(getQuestionPoint(paperActivity));
            }
        }
    }

    /**
     * 填充 活动信息
     *
     * @param estimatePaper 活动卷
     * @param paperActivity 活动-mysql 信息
     */
    private void fillPaperEstimateInfo(EstimatePaper estimatePaper, SelectActivityReq paperActivity) {
        //真题演练，无活动时间和考试时间
        if (!TRUE_PAPER.is(paperActivity.getType())) {
            if (null != paperActivity.getOnlineTime() && null != paperActivity.getOfflineTime()) {
                estimatePaper.setOnlineTime(paperActivity.getOnlineTime().getTime());
                estimatePaper.setOfflineTime(paperActivity.getOfflineTime().getTime());
            }
            if (null != paperActivity.getStartTime() && null != paperActivity.getEndTime()) {
                estimatePaper.setStartTime(paperActivity.getStartTime().getTime());
                estimatePaper.setEndTime(paperActivity.getEndTime().getTime());
            } else {
                if (null != paperActivity.getOnlineTime() && null != paperActivity.getOfflineTime()) {
                    estimatePaper.setStartTime(paperActivity.getOnlineTime().getTime());
                    estimatePaper.setEndTime(paperActivity.getOfflineTime().getTime());
                }
            }
        }
        estimatePaper.setLookParseTime(null == paperActivity.getLookParseTime() ? ActivityLookParseType.HAND_EXAM_PAPER_LOOK.getCode() : paperActivity.getLookParseTime());
        estimatePaper.setDescrp(paperActivity.getInstruction());
        //是否隐藏，默认隐藏
        estimatePaper.setHideFlag(null == paperActivity.getHideFlag() ? 0 : paperActivity.getHideFlag());
    }

    /**
     * 填充模考大赛信息
     *
     * @param match         模考大赛基础数据
     * @param paperActivity 活动-mysql 信息
     */
    private void fillMatchInfo(Match match, SelectActivityReq paperActivity) {
        match.setPaperId(paperActivity.getId().intValue());
        match.setName(paperActivity.getName());
        match.setCourseId(paperActivity.getCourseId().intValue());
        match.setCourseInfo(paperActivity.getCourseInfo());
        match.setInstruction(paperActivity.getInstruction());
        match.setInstructionPC(paperActivity.getInstruction());
        match.setTag(ActivityTagEnum.TagEnum.create(paperActivity.getTag()).getCode());
        List<Long> subjectIds = paperActivity.getSubjectIds();
        if (CollectionUtils.isNotEmpty(subjectIds)) {
            match.setSubject(subjectIds.get(0).intValue());
        }
        if (null != paperActivity.getStartTime() && null != paperActivity.getEndTime()) {
            match.setStartTime(paperActivity.getStartTime().getTime());
            match.setEndTime(paperActivity.getEndTime().getTime());
            match.setTimeInfo(HtmlConvertUtil.assertMatchTimeInfo(match.getStartTime(), match.getEndTime()));
        }
        match.setStatus(paperActivity.getBizStatus());
        if (null != paperActivity.getEssayId()) {
            match.setEssayPaperId(paperActivity.getEssayId());
            match.setEssayStartTime(paperActivity.getEssayStartTime().getTime());
            match.setEssayEndTime(paperActivity.getEssayEndTime().getTime());
        }
    }

    /**
     * 填充试卷 模块-试题信息
     *
     * @param paper         试卷信息
     * @param paperActivity 活动-mysql 信息
     */
    private void fillModuleWithQuestions(Paper paper, SelectActivityReq paperActivity) {
        Long id = paperActivity.getId();
        PaperSearchInfo paperSearchInfo = paperSearchService.entityActivityDetail(id);
        List<PaperSearchInfo.ModuleInfo> moduleInfo;
        if (null == paperSearchInfo || CollectionUtils.isEmpty(moduleInfo = paperSearchInfo.getModuleInfo())) {
            return;
        }
        List<Module> modules = Lists.newArrayList();
        List<Integer> questionIds = Lists.newArrayList();
        List<Integer> bigQuestions = Lists.newArrayList();
        List<Double> questionScores = Lists.newArrayList();
        for (PaperSearchInfo.ModuleInfo info : moduleInfo) {
            List<PaperQuestionSimpleInfo> questionSimpleInfos = info.getList();
            int qcount = 0;   //模块中子题的数量
            if (CollectionUtils.isNotEmpty(questionSimpleInfos)) {
                for (PaperQuestionSimpleInfo questionSimpleInfo : questionSimpleInfos) {
                    bigQuestions.add(questionSimpleInfo.getId().intValue());
                    List<PaperQuestionSimpleInfo> children = questionSimpleInfo.getChildren();
                    if (CollectionUtils.isNotEmpty(children)) {
                        List<Integer> collect = children.stream().map(PaperQuestionSimpleInfo::getId).map(Long::intValue).collect(Collectors.toList());
                        log.info("内容是:{}", collect);
                        questionIds.addAll(collect);
                        /**
                         * 同步每个试题的分数到mongo
                         */
                        questionScores.addAll(children.stream().map(PaperQuestionSimpleInfo::getScore).collect(Collectors.toList()));
                        qcount += children.size();
                    } else {
                        questionIds.add(questionSimpleInfo.getId().intValue());
                        /**
                         * 同步每个试题的分数到mongo
                         */
                        questionScores.add(questionSimpleInfo.getScore());
                        qcount++;
                    }
//                    //异步上传试题信息到mongo
//                    sendQuestion2Mongo(questionSimpleInfo.getId().intValue());
                }
            }
            Module module = Module.builder()
                    .category(info.getId())
                    .name(info.getName())
                    .qcount(qcount)
                    .build();
            modules.add(module);
        }
        paper.setModules(modules);
        paper.setQuestions(questionIds);
        paper.setBigQuestions(bigQuestions);
        paper.setQcount(questionIds.size());
        //log.info("所有分数是:{}", questionScores);
        paper.setScores(questionScores);
        log.info("同步活动卷到mongo,试卷ID是:{},试题数目是:{}", paperActivity.getId(), questionIds.size());
    }

    /**
     * 清除当前的缓存信息
     */
    public void clearEstimatePaperCache(String subjectId) {
        final String prefixKey = new StringBuffer()
                .append("paper-web-server.estimatePapers:")
                .append(subjectId).append(":").toString();
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try {
            Set<byte[]> members = connection.sMembers(getEstimatePaperCacheAllKeyInfo().getBytes());
            if (CollectionUtils.isNotEmpty(members)) {
                members.stream()
                        .map(String::new)
                        .filter(key -> key.indexOf(prefixKey) >= 0)
                        .forEach(key -> {
                            //1.删除缓存key
                            connection.del(key.getBytes());
                            //2.删除总记录中的key值
                            connection.sRem(getEstimatePaperCacheAllKeyInfo().getBytes(), key.getBytes());
                        });
                List<String> keys = members.stream().map(bytes -> new String(bytes))
                        .filter(key -> key.indexOf(prefixKey) >= 0).collect(Collectors.toList());
                log.info("删除列表缓存：{}", keys);
            }
        } finally {
            connection.close();
        }
    }

    private static String getEstimatePaperCacheAllKeyInfo() {
        return "estimatePapers:cache:keys";
    }

    /**
     * 阶段测试,修改活动信息,需将修改内容同步到php课程大纲
     */
    @Async
    public void syncPaperToPhp(SelectActivityReq paperActivity) {

        HashMap params = new HashMap();
        params.put("id", paperActivity.getId());
        params.put("name", paperActivity.getName());
        params.put("isLongTerm", paperActivity.getStartTimeIsEffective());
        params.put("startTime", paperActivity.getOnlineTime().toString());
        params.put("endTime", paperActivity.getOfflineTime().toString());
        RestTemplate restTemplate = new RestTemplate();
        log.info("阶段测试同步到php,同步参数是:{}", params);
        ResponseMsg responseMsg = restTemplate.postForObject(updateFormativeTestPaper, params, ResponseMsg.class);
        if (responseMsg.getCode() != 1000000) {
            log.info("阶段测试同步失败,试卷ID是:{},消息是:{}", paperActivity.getId(), responseMsg.getMsg());
        }
    }

    /**
     * 小模考,阶段测试,获取试卷内绑定的所有试题的三级知识点，同步到mongo中
     */
    public String getQuestionPoint(SelectActivityReq paperActivity) {

        //1.获取所有试题ID
        List<PaperQuestion> paperQuestionList = new ArrayList<>();
        if (paperActivity.getPaperId() != null && paperActivity.getPaperId() != 0L) {
            paperQuestionList = paperQuestionService.findByPaperIdAndType(paperActivity.getPaperId(), PaperInfoEnum.TypeInfo.ENTITY);
        } else {
            paperQuestionList = paperQuestionService.findByPaperIdAndType(paperActivity.getId(), PaperInfoEnum.TypeInfo.SIMULATION);
        }
        if (CollectionUtils.isNotEmpty(paperQuestionList)) {
            List<Long> questionIds = paperQuestionList.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());

            //1.试题绑定多个知识点只获取一个
            Example example = new Example(QuestionKnowledge.class);
            example.and().andIn("questionId", questionIds);
            List<QuestionKnowledge> knowledgeList = questionKnowledgeService.selectByExample(example);
            Map<Long, List<QuestionKnowledge>> knowledges = knowledgeList.stream().collect(Collectors.groupingBy(QuestionKnowledge::getQuestionId));
            final List<Long> knowledgeIds = knowledges.entrySet()
                    .stream().filter(knowledge -> null != knowledge.getValue())
                    .map(knowledge -> knowledge.getValue().get(0).getKnowledgeId())
                    .collect(Collectors.toList());

            //2.只获取三级知识点
            List<String> knowledgeNameByIds = knowledgeService.getKnowledgeNameByIds(knowledgeIds);
            List<String> thirdKnowledgeNames = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(knowledgeNameByIds)) {
                knowledgeNameByIds.stream().forEach(knowledgeName -> {
                    List<String> collect = Arrays.stream(knowledgeName.split("-")).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect)) {
                        if (collect.size() >= 3) {
                            thirdKnowledgeNames.add(collect.subList(2, 3).get(0));
                        }
                    }
                });
            }

            //3.拼接试卷涉及到的知识点,并去重
            String knowledgeName = thirdKnowledgeNames.stream().distinct().collect(Collectors.joining(","));
            log.info("试卷ID是:{},知识点名称为:{}", paperActivity.getId(), knowledgeName);

            return knowledgeName;
        }
        return "";
    }
}
